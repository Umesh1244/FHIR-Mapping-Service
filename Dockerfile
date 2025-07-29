# Build Stage
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Copy Gradle files first to leverage caching
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon || true

# Copy full project and build
COPY . .
RUN gradle build --no-daemon -x test

# Runtime Stage
FROM eclipse-temurin:17-jdk-focal
WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar fhir.jar

# Expose port
EXPOSE 8083

# Run the Spring Boot JAR
ENTRYPOINT ["java", "-jar", "fhir.jar"]
