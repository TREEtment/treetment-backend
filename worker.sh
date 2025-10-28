#!/bin/bash
set -euo pipefail

if [ "$#" -ne 3 ]; then
  echo "usage: worker.sh <TREE_ID> <USER_ID> <SCORE>"
  exit 1
fi

TREE_ID="$1"
USER_ID="$2"
SCORE="$3"

AWS_REGION="ap-northeast-2"            # 실제 리전
AMI_ID="ami-0f8c3fcf70ac1ae95"         # GPU AMI ID
INSTANCE_TYPE="g4dn.xlarge"
KEY_NAME="treetment-blender-server-key-pair"           # GPU 계정 B 키페어
SECURITY_GROUP_ID="sg-0d3cdd04c8b6b09e7"    # GPU SG
SUBNET_ID="subnet-06ad0e5f8e3610975"        # GPU 퍼블릭 서브넷

BACKEND_LOCAL_URL="http://localhost:8080"
INTERNAL_SECRET="${INTERNAL_SECRET:-super-secret-token}"

echo "[worker] Start render job treeId=$TREE_ID userId=$USER_ID score=$SCORE"

# 1. GPU 인스턴스 생성
INSTANCE_ID=$(aws ec2 run-instances \
  --region "$AWS_REGION" \
  --image-id "$AMI_ID" \
  --instance-type "$INSTANCE_TYPE" \
  --key-name "$KEY_NAME" \
  --security-group-ids "$SECURITY_GROUP_ID" \
  --subnet-id "$SUBNET_ID" \
  --associate-public-ip-address \
  --query 'Instances[0].InstanceId' \
  --output text)

aws ec2 wait instance-running --region "$AWS_REGION" --instance-ids "$INSTANCE_ID"

PUBLIC_IP=$(aws ec2 describe-instances \
  --region "$AWS_REGION" \
  --instance-ids "$INSTANCE_ID" \
  --query 'Reservations[0].Instances[0].PublicIpAddress' \
  --output text)

# 2. blender-api 준비 대기
for i in {1..30}; do
  if curl -s "http://$PUBLIC_IP:9000/docs" > /dev/null; then
    break
  fi
  sleep 3
done

# 3. 렌더 실행
RESPONSE=$(curl -s -X POST "http://$PUBLIC_IP:9000/grow-tree" \
  -H "Content-Type: application/json" \
  -d "{\"score\": ${SCORE}, \"userId\": \"${USER_ID}\", \"treeId\": ${TREE_ID}}")

IMAGE_URL=$(echo "$RESPONSE" | jq -r '.image_url')
RETURNCODE=$(echo "$RESPONSE" | jq -r '.returncode')

# 4. 완료 콜백
if [ "$RETURNCODE" = "0" ] && [ "$IMAGE_URL" != "null" ] && [ -n "$IMAGE_URL" ]; then
  curl -s -X POST "$BACKEND_LOCAL_URL/api/trees/internal/complete" \
    -H "Content-Type: application/json" \
    -H "X-Internal-Token: $INTERNAL_SECRET" \
    -d "{\"treeId\": ${TREE_ID}, \"imageUrl\": \"${IMAGE_URL}\"}"
else
  echo "[worker] Render failed; TODO: optional fail callback"
fi

# 5. GPU 인스턴스 종료
aws ec2 terminate-instances --region "$AWS_REGION" --instance-ids "$INSTANCE_ID" >/dev/null
aws ec2 wait instance-terminated --region "$AWS_REGION" --instance-ids "$INSTANCE_ID"

echo "[worker] Done. GPU terminated."
