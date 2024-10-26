FROM openjdk:21-jdk-slim

WORKDIR /app
COPY target/Lucky_Social_Network-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=1050
EXPOSE 1050

ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]