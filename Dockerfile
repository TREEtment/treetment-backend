# syntax=docker/dockerfile:1.6

FROM eclipse-temurin:21-jre

WORKDIR /app

# 필요한 툴 설치:
# - bash: worker.sh 는 #!/bin/bash 로 시작하니까 필요
# - curl, jq, unzip: worker.sh와 AWS CLI 설치 과정에서 사용
# - ca-certificates: https 요청용
RUN apt-get update && apt-get install -y \
    bash \
    curl \
    jq \
    unzip \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# AWS CLI v2 설치 (Debian 계열에서는 이 방식이 정상 동작함)
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "/tmp/awscliv2.zip" && \
    unzip /tmp/awscliv2.zip -d /tmp && \
    /tmp/aws/install && \
    rm -rf /tmp/aws /tmp/awscliv2.zip

# worker.sh 넣기
COPY worker.sh /app/worker.sh
RUN chmod +x /app/worker.sh

# 애플리케이션 JAR 넣기
COPY app.jar /app/app.jar

# 자바 옵션 & 리전 기본값 등
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV AWS_DEFAULT_REGION="ap-northeast-2"

EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
