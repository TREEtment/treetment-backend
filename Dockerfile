# syntax=docker/dockerfile:1.6
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# (헬스 체크 쓰면 필요)
RUN apk add --no-cache curl
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
# CI에서 만든 app.jar을 복사
COPY app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
