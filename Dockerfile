# Java 21 JDK 베이스 이미지 사용
FROM eclipse-temurin:21-jdk-alpine

# 앱 실행 디렉토리
WORKDIR /app

# Gradle로 빌드된 JAR 파일 복사
ARG JAR_FILE=build/libs/YOGIITSU-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 앱이 사용하는 포트
EXPOSE 8080

	# 앱 실행 명령어
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
