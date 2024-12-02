FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/user-profile-information-store-0.0.1-SNAPSHOT.jar /app/user-profile-information-store-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/app/user-profile-information-store-0.0.1-SNAPSHOT.jar"]
