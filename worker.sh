#!/bin/bash
set -euo pipefail

########################################
# 0. 인자 / 설정
########################################

if [ "$#" -ne 3 ]; then
  echo "usage: worker.sh <TREE_ID> <USER_ID> <SCORE>"
  exit 1
fi

TREE_ID="$1"
USER_ID="$2"
SCORE="$3"

# 고정 설정 (필요하면 .env로 뺄 수 있음)
AWS_REGION="ap-northeast-2"                      # GPU 리전 (현재 서울로 맞춘 상태)
AMI_ID="ami-06b628dbe39a5b5b9"                   # Blender 미리 세팅된 AMI
INSTANCE_TYPE="g4dn.xlarge"                      # GPU 타입
KEY_NAME="treetment-blender-server-key-pair"     # 계정 B의 EC2 키 페어 이름
SECURITY_GROUP_ID="sg-0d3cdd04c8b6b09e7"         # 계정 B SG (9000 허용된 애)
SUBNET_ID="subnet-06ad0e5f8e3610975"             # 퍼블릭 서브넷 (퍼블릭 IP 붙는 곳)

# 백엔드 접근 URL
# 이 스크립트는 backend 컨테이너 안에서 실행되니까 컨테이너 내부 포트 8080이 맞음
BACKEND_LOCAL_URL="http://localhost:8080"

# 내부 인증 토큰 (백엔드 /api/trees/internal/complete 헤더 검증용)
INTERNAL_SECRET="${INTERNAL_SECRET:-super-secret-token}"

echo "[worker] ===== Start render job ====="
echo "[worker] treeId=$TREE_ID userId=$USER_ID score=$SCORE"
echo "[worker] REGION=$AWS_REGION AMI_ID=$AMI_ID TYPE=$INSTANCE_TYPE"
echo "[worker] USING INTERNAL_SECRET? $( [ -n "$INTERNAL_SECRET" ] && echo yes || echo no )"

########################################
# 1. cleanup 트랩 (무조건 GPU 종료 보장)
########################################

INSTANCE_ID=""
cleanup() {
  if [ -n "${INSTANCE_ID:-}" ]; then
    echo "[worker] Cleaning up instance $INSTANCE_ID (terminate)"
    aws ec2 terminate-instances \
      --region "$AWS_REGION" \
      --instance-ids "$INSTANCE_ID" >/dev/null 2>&1 || true

    # 완전히 내려갈 때까지 대기 (굳이 기다리지 않아도 되지만 깔끔하게 마무리)
    aws ec2 wait instance-terminated \
      --region "$AWS_REGION" \
      --instance-ids "$INSTANCE_ID" >/dev/null 2>&1 || true
    echo "[worker] Instance $INSTANCE_ID terminated."
  fi
}
trap cleanup EXIT

########################################
# 2. GPU 인스턴스 생성
########################################

echo "[worker] Launching GPU instance..."

# 인스턴스 만들면서 태그도 같이 넣는다.
# Name / TreeId / UserId 붙여서 콘솔에서 추적 가능하게.
INSTANCE_ID=$(aws ec2 run-instances \
  --region "$AWS_REGION" \
  --image-id "$AMI_ID" \
  --instance-type "$INSTANCE_TYPE" \
  --key-name "$KEY_NAME" \
  --security-group-ids "$SECURITY_GROUP_ID" \
  --subnet-id "$SUBNET_ID" \
  --associate-public-ip-address \
  --count 1 \
  --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=treetment-blender-worker},{Key=TreeId,Value=$TREE_ID},{Key=UserId,Value=$USER_ID}]" \
  --query 'Instances[0].InstanceId' \
  --output text)

echo "[worker] Instance launched: $INSTANCE_ID"
echo "[worker] Waiting until running..."

aws ec2 wait instance-running \
  --region "$AWS_REGION" \
  --instance-ids "$INSTANCE_ID"

echo "[worker] Instance is running. Fetching public IP..."

PUBLIC_IP=$(aws ec2 describe-instances \
  --region "$AWS_REGION" \
  --instance-ids "$INSTANCE_ID" \
  --query 'Reservations[0].Instances[0].PublicIpAddress' \
  --output text)

if [ -z "$PUBLIC_IP" ] || [ "$PUBLIC_IP" = "None" ]; then
  echo "[worker] ERROR: Failed to get public IP."
  exit 1
fi

echo "[worker] Public IP acquired: $PUBLIC_IP"

########################################
# 3. blender-api 기동 대기 (최대 ~90초)
########################################

echo "[worker] Waiting blender-api (on $PUBLIC_IP:9000)..."

READY="false"
for i in {1..30}; do
  # /health 엔드포인트나 /docs 중 하나만 살아있으면 OK로 본다
  if curl -s "http://$PUBLIC_IP:9000/health" >/dev/null 2>&1 || \
     curl -s "http://$PUBLIC_IP:9000/docs" >/dev/null 2>&1; then
    READY="true"
    echo "[worker] blender-api is ready (try #$i)"
    break
  fi
  echo "[worker] blender-api not ready yet (try #$i), sleep..."
  sleep 3
done

if [ "$READY" != "true" ]; then
  echo "[worker] ERROR: blender-api did not become ready in time."
  # 여기서 exit하면 trap이 돌면서 EC2는 terminate된다.
  exit 1
fi

########################################
# 4. 렌더 요청
########################################

echo "[worker] Calling grow-tree..."
RESPONSE=$(curl -s -X POST "http://$PUBLIC_IP:9000/grow-tree" \
  -H "Content-Type: application/json" \
  -d "{\"score\": ${SCORE}, \"userId\": \"${USER_ID}\", \"treeId\": ${TREE_ID}}")

echo "[worker] Blender response raw:"
echo "$RESPONSE"

IMAGE_URL=$(echo "$RESPONSE"    | jq -r '.image_url')
RETURNCODE=$(echo "$RESPONSE"   | jq -r '.returncode')

########################################
# 5. 백엔드 콜백 (/api/trees/internal/complete)
########################################

if [ "$RETURNCODE" = "0" ] && [ -n "$IMAGE_URL" ] && [ "$IMAGE_URL" != "null" ]; then
  echo "[worker] Rendering succeeded. Sending completion callback..."

  CALLBACK_BODY=$(cat <<EOF
{"treeId": ${TREE_ID}, "imageUrl": "${IMAGE_URL}"}
EOF
)

  CALLBACK_RESP=$(curl -s -X POST "$BACKEND_LOCAL_URL/api/trees/internal/complete" \
    -H "Content-Type: application/json" \
    -H "X-Internal-Token: $INTERNAL_SECRET" \
    -d "$CALLBACK_BODY")

  echo "[worker] Callback response:"
  echo "$CALLBACK_RESP"
else
  echo "[worker] Render failed or no image_url. Skipping success callback."
  echo "[worker] TODO: we could POST /api/trees/internal/fail here to markFailed() if you add that endpoint."
fi

########################################
# 6. 끝
########################################

echo "[worker] Job finished for treeId=$TREE_ID. Instance will be terminated via trap."
