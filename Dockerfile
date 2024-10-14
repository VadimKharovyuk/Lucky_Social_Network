
FROM openjdk:21-jdk-slim


LABEL maintainer="vadim"
LABEL application="Lucky_Social_Network"


WORKDIR /app


COPY target/Lucky_Social_Network-0.0.1-SNAPSHOT.jar app.jar


EXPOSE 1050


ENTRYPOINT ["java", "-jar", "app.jar"]