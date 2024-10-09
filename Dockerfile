#FROM openjdk:21
#ARG JAR_FILE=build/libs/*.jar
#COPY ${JAR_FILE} app.jar
#
#EXPOSE 9000
#ENTRYPOINT ["java","-jar","/app.jar"]
FROM openjdk:21-slim AS build
WORKDIR /app
COPY . .
RUN ./gradlew build # Maven을 사용하는 경우 'mvn package'로 변경
# Stage 2: 런타임 이미지 생성
FROM openjdk:21
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 9000