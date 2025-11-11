#!/bin/bash
set -Eeuo pipefail

########################################
# 0) 필수 도구 / 환경 점검
########################################
need() { command -v "$1" >/dev/null 2>&1 || { echo "[worker] FATAL: '$1' not found"; exit 127; }; }
need aws
need curl
need jq

# AWS 리전 (필수). 컨테이너/호스트에 설정되어 있지 않으면 이 값 사용
: "${AWS_REGION:=ap-northeast-2}"
export AWS_DEFAULT_REGION="$AWS_REGION"
export AWS_PAGER=""

# 내부 콜백용 시크릿 (tree.py에서 사용)
: "${INTERNAL_SECRET:=internal_secret_internal_secret}"
export INTERNAL_SECRET

# 백엔드 URL (tree.py에서 사용)
# GPU 인스턴스에서 실행되므로 실제 백엔드의 Public IP 사용
: "${BACKEND_URL:=http://3.34.146.140}"
export BACKEND_URL

########################################
# 1) 인자 파싱
########################################
if [ "$#" -ne 3 ]; then
  echo "usage: worker.sh <TREE_ID> <USER_ID> <SCORE>"
  exit 1
fi

TREE_ID="$1"
USER_ID="$2"
SCORE="$3"

########################################
# 2) 런타임 설정 (필요 시 .env로 분리 가능)
########################################
# GPU 측(계정 B)의 리소스
# 실제 Blender GPU AMI ID (available 상태여야 함)
: "${AMI_ID:=ami-0caa0e6efe7cdcacd}"          # Blender GPU AMI - 상태 확인 필요!
: "${INSTANCE_TYPE:=g4dn.xlarge}"
: "${KEY_NAME:=treetment-blender-server-key-pair}"
: "${SECURITY_GROUP_ID:=sg-0d3cdd04c8b6b09e7}"
: "${SUBNET_ID:=subnet-06ad0e5f8e3610975}"

# 타임아웃/재시도 설정
: "${READY_MAX_TRIES:=30}"          # blender-api 준비 대기(최대 30회 x 3초 = ~90s)
: "${READY_SLEEP_SEC:=3}"
: "${GROWTREE_TIMEOUT_SEC:=180}"    # grow-tree 호출 타임아웃(초)
: "${GROWTREE_RETRIES:=3}"          # 실패시 재시도 횟수
: "${GROWTREE_BACKOFF_BASE:=2}"     # 지수 백오프 배수

echo "[worker] ===== Start render job ====="
echo "[worker] treeId=$TREE_ID userId=$USER_ID score=$SCORE"
echo "[worker] REGION=$AWS_REGION AMI_ID=$AMI_ID TYPE=$INSTANCE_TYPE"
echo "[worker] INTERNAL_SECRET set? $([ -n "$INTERNAL_SECRET" ] && echo yes || echo no)"
echo "[worker] BACKEND_URL=$BACKEND_URL"

########################################
# 3) 종료 트랩 (항상 인스턴스 정리)
########################################
INSTANCE_ID=""
cleanup() {
  if [ -n "${INSTANCE_ID:-}" ]; then
    echo "[worker] Terminating instance $INSTANCE_ID ..."
    aws ec2 terminate-instances --instance-ids "$INSTANCE_ID" >/dev/null 2>&1 || true
    aws ec2 wait instance-terminated --instance-ids "$INSTANCE_ID" >/dev/null 2>&1 || true
    echo "[worker] Instance $INSTANCE_ID terminated."
  fi
}
trap cleanup EXIT

########################################
# 4) GPU 인스턴스 생성 & 실행 대기
########################################
echo "[worker] Launching GPU instance..."
INSTANCE_ID="$(aws ec2 run-instances \
  --image-id "$AMI_ID" \
  --instance-type "$INSTANCE_TYPE" \
  --key-name "$KEY_NAME" \
  --security-group-ids "$SECURITY_GROUP_ID" \
  --subnet-id "$SUBNET_ID" \
  --associate-public-ip-address \
  --count 1 \
  --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=treetment-blender-worker},{Key=TreeId,Value=$TREE_ID},{Key=UserId,Value=$USER_ID}]" \
  --query 'Instances[0].InstanceId' --output text)"

echo "[worker] Instance launched: $INSTANCE_ID"
echo "[worker] Waiting until running..."
aws ec2 wait instance-running --instance-ids "$INSTANCE_ID"

PUBLIC_IP="$(aws ec2 describe-instances \
  --instance-ids "$INSTANCE_ID" \
  --query 'Reservations[0].Instances[0].PublicIpAddress' \
  --output text)"
if [ -z "$PUBLIC_IP" ] || [ "$PUBLIC_IP" = "None" ]; then
  echo "[worker] ERROR: Could not obtain public IP"
  exit 1
fi
echo "[worker] Public IP: $PUBLIC_IP"

########################################
# 5) blender-api 준비 대기
########################################
echo "[worker] Waiting blender-api readiness on $PUBLIC_IP:9000 ..."
READY="false"
for ((i=1;i<=READY_MAX_TRIES;i++)); do
  if curl -s --max-time 3 "http://$PUBLIC_IP:9000/health" >/dev/null 2>&1 || \
     curl -s --max-time 3 "http://$PUBLIC_IP:9000/docs"   >/dev/null 2>&1; then
    READY="true"
    echo "[worker] blender-api is ready (try #$i)"
    break
  fi
  echo "[worker] not ready yet (try #$i) ... sleep $READY_SLEEP_SEC"
  sleep "$READY_SLEEP_SEC"
done
if [ "$READY" != "true" ]; then
  echo "[worker] ERROR: blender-api did not become ready in time"
  exit 1
fi

########################################
# 6) grow-tree 호출 (지수 백오프 재시도)
########################################
call_grow() {
  curl -sS -X POST "http://$PUBLIC_IP:9000/grow-tree" \
    -H "Content-Type: application/json" \
    --max-time "$GROWTREE_TIMEOUT_SEC" \
    -d "{\"score\": ${SCORE}, \"userId\": \"${USER_ID}\", \"treeId\": ${TREE_ID}}"
}

RESPONSE=""
attempt=0
while :; do
  attempt=$((attempt+1))
  echo "[worker] Calling grow-tree (attempt $attempt/$GROWTREE_RETRIES) ..."
  set +e
  RESPONSE="$(call_grow)"
  rc=$?
  set -e

  # 네트워크/타임아웃 에러
  if [ $rc -ne 0 ]; then
    echo "[worker] grow-tree request failed (rc=$rc)"
  else
    # 형식상 성공이면 break 시도 (JSON 파싱)
    break
  fi

  if [ $attempt -ge "$GROWTREE_RETRIES" ]; then
    echo "[worker] ERROR: grow-tree failed after $GROWTREE_RETRIES attempts"
    RESPONSE=""  # 명확히 비움
    break
  fi
  sleep_sec=$(( GROWTREE_BACKOFF_BASE ** attempt ))
  echo "[worker] retry in ${sleep_sec}s ..."
  sleep "$sleep_sec"
done

echo "[worker] Blender response raw:"
echo "$RESPONSE"

########################################
# 7) 응답 파싱 (model_url / data_url)
########################################
# returncode, model_url(data_url는 필요시 활용)
RETURNCODE="$(echo "$RESPONSE" | jq -r '.returncode // empty' 2>/dev/null || true)"
MODEL_URL="$(echo "$RESPONSE"   | jq -r '.model_url  // .image_url // empty' 2>/dev/null || true)"
DATA_URL="$(echo "$RESPONSE"    | jq -r '.data_url   // empty' 2>/dev/null || true)"

# 일부 이미지가 stdout에서만 노출될 때 대비(FINAL_* 라인 파싱)
if [ -z "$MODEL_URL" ] || [ "$MODEL_URL" = "null" ]; then
  MODEL_URL="$(echo "$RESPONSE" | jq -r '.stdout // ""' | grep -Eo 'FINAL_MODEL_URL=.*' | head -n1 | sed 's/FINAL_MODEL_URL=//')"
fi

echo "[worker] Parsed: returncode=${RETURNCODE:-<empty>} model_url=${MODEL_URL:-<empty>}"

########################################
# 8) 결과 확인
#    중요: complete 콜백은 이미 tree.py에서 호출되었음!
########################################
if [ "${RETURNCODE:-1}" = "0" ] && [ -n "${MODEL_URL:-}" ] && [ "${MODEL_URL:-null}" != "null" ]; then
  echo "[worker] ===== Rendering SUCCEEDED ====="
  echo "[worker] Model URL: $MODEL_URL"
  echo "[worker] Data URL: ${DATA_URL:-<empty>}"
  echo "[worker]"
  echo "[worker] NOTE: Complete callback was already sent by tree.py during S3 upload!"
  echo "[worker] No need to call /api/trees/internal/complete from worker.sh"
else
  echo "[worker] ===== Rendering FAILED ====="
  echo "[worker] Render failed or model_url missing."
  echo "[worker] (Optional) add a fail-callback endpoint to markFailed() if needed."
fi

########################################
# 9) 끝 (trap으로 인스턴스 정리)
########################################
echo "[worker] Job finished for treeId=$TREE_ID"