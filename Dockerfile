FROM eclipse-temurin:17-jdk-alpine

COPY build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]