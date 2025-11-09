# 1. Java 17 버전을 기반으로 합니다. (버전이 다르면 수정)
FROM eclipse-temurin:17-jdk

# 2. (중요!) Gradle 빌드 결과물(.jar)을 app.jar라는 이름으로 복사합니다.
COPY build/libs/AZit-0.0.1-SNAPSHOT.jar app.jar

# 3. 8080번 포트를 엽니다.
EXPOSE 8080

# 4. app.jar 파일을 실행합니다.
ENTRYPOINT ["java", "-jar", "/app.jar"]