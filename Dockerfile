# syntax=docker/dockerfile:1.6

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 1) 런타임 유틸 설치: bash, curl, jq, unzip
#    - bash: worker.sh에서 set -euo pipefail 같은 bash 옵션 쓰기 편하려고
#    - curl: 이미 쓰고 있긴 한데 어차피 같이 깔자
#    - jq: worker.sh가 blender-api 응답 파싱할 때 필요
#    - unzip: awscli v2 설치할 때 필요
RUN apk add --no-cache bash curl jq unzip

# 2) AWS CLI v2 설치
#    Alpine은 glibc가 아니라 musl이라서 공식 리눅스 x86_64 바이너리 그대로는 안 될 때가 있는데,
#    새 인스턴스 대부분은 musl 호환 빌드로도 동작 가능하다.
#    일반적으로 다음 방식으로 시도 가능 (표준 절차):
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "/tmp/awscliv2.zip" \
    && unzip /tmp/awscliv2.zip -d /tmp \
    && /tmp/aws/install --bin-dir /usr/local/bin --install-dir /usr/local/aws-cli --update \
    && rm -rf /tmp/aws /tmp/awscliv2.zip

# 3) worker.sh를 컨테이너 안에 복사
#    -> 나중에 실제 파일 경로에 맞춰서 COPY 수정해
COPY worker.sh /app/worker.sh
RUN chmod +x /app/worker.sh

# 4) JAR 복사
COPY app.jar /app/app.jar

# 5) Spring이 쓸 기본 ENV들
#    JAVA_OPTS 그대로 유지
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

#    AWS creds, INTERNAL_SECRET 등은 docker run 할 때 -e로 주입할 거라
#    여기서는 기본값만 깔아둠 (실제 값은 절대 이미지 안에 하드코딩하지 마!)
ENV AWS_DEFAULT_REGION="us-east-1"
ENV INTERNAL_SECRET="super-secret-token"

EXPOSE 8080

# 6) 컨테이너 엔트리포인트: Spring 부트 앱 실행 (자바)
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
