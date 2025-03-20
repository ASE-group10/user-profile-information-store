# ===== STAGE 1: Build the application =====
FROM maven:3.9.5-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and resolve dependencies (caching)
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ===== STAGE 2: Create the final runtime image =====
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/user-profile-information-store-0.0.1-SNAPSHOT.jar /app/user-profile-information-store-0.0.1-SNAPSHOT.jar

# Verify the copied JAR file
RUN ls -l /app

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "/app/user-profile-information-store-0.0.1-SNAPSHOT.jar"]
