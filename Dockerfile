# syntax=docker/dockerfile:1.6
# Runtime-only image for Spring Boot JAR built by Gradle

FROM eclipse-temurin:21-jre

WORKDIR /app

# 필요한 유틸 (worker.sh 및 운영 점검용)
RUN apt-get update && apt-get install -y --no-install-recommends \
    bash \
    curl \
    jq \
    unzip \
    ca-certificates \
  && rm -rf /var/lib/apt/lists/*

# AWS CLI v2 (공식 설치 프로그램 사용 — Debian/Temurin 기반에서 apt 패키지 미제공 이슈 회피)
RUN curl -fsSL "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "/tmp/awscliv2.zip" \
  && unzip -q /tmp/awscliv2.zip -d /tmp \
  && /tmp/aws/install --bin-dir /usr/local/bin --install-dir /usr/local/aws-cli \
  && rm -rf /tmp/aws /tmp/awscliv2.zip

# 타임존/로케일
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# worker.sh 넣기 (옵션 스크립트)
COPY worker.sh /app/worker.sh
RUN chmod +x /app/worker.sh

# === 핵심: Gradle 산출물 경로에서 직접 복사 ===
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} /app/app.jar

# JVM 옵션/기본 리전
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV AWS_DEFAULT_REGION="ap-northeast-2"

EXPOSE 8080

# (선택) 간단 헬스체크
# HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
#   CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
