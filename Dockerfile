# Java 21 JDK 베이스 이미지 사용
FROM eclipse-temurin:21-jdk-alpine

# 앱 실행 디렉토리
WORKDIR /app

# Gradle로 빌드된 JAR 파일 복사
ARG JAR_FILE=YOGIITSU-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 로그 디렉토리 생성 및 권한 설정
RUN mkdir -p /app/logs /var/log/yogiitsu/dev /var/log/yogiitsu/prod && \
    chmod 755 /app/logs && \
    chmod 775 /var/log/yogiitsu/dev /var/log/yogiitsu/prod

# 앱이 사용하는 포트
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-Dspring.config.additional-location=classpath:/", "-jar", "/app/app.jar"]