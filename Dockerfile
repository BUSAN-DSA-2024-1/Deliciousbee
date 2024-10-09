FROM openjdk:21

# 애플리케이션 JAR 파일을 컨테이너 내부로 복사
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 애플리케이션 포트 개방
EXPOSE 9000

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
