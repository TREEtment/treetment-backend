# syntax=docker/dockerfile:1.6
# Runtime-only image for Spring Boot JAR built by Gradle

FROM eclipse-temurin:21-jre

WORKDIR /app

# 필요한 유틸 (worker.sh 및 운영 점검용)
RUN apt-get update && apt-get install -y \
    bash \
    curl \
    jq \
    unzip \
    ca-certificates \
    awscli \
  && rm -rf /var/lib/apt/lists/*

# 타임존/로케일
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# worker.sh 넣기 (옵션 스크립트)
COPY worker.sh /app/worker.sh
RUN chmod +x /app/worker.sh

# === 핵심 변경: Gradle 산출물 경로에서 직접 복사 ===
# GitHub Actions에서 build context가 ./backend 라면 JAR은 build/libs/*.jar 에 생성됩니다.
# 특정 파일명만 집어넣고 싶으면 JAR_FILE 값을 오버라이드하세요.
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} /app/app.jar

# JVM 옵션/기본 리전
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV AWS_DEFAULT_REGION="ap-northeast-2"

EXPOSE 8080

# (선택) 간단 헬스체크 — 앱이 기동 후 8080에서 /actuator/health 가 200인지 확인
# 필요 없으면 아래 HEALTHCHECK 라인을 주석 처리하세요.
# HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
#   CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
