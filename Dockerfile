# Build stage
FROM gradle:9.8.0-jdk25 AS build
WORKDIR /build

# Copy gradle files for dependency caching
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./

# Copy source code
COPY src/ src/

# Build the application (skip tests for faster build)
RUN gradle build --no-daemon -x test

# Run stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S app && adduser -S app -G app
USER app

# Copy jar from build stage
COPY --from=build /build/build/libs/*.jar app.jar

# Expose port
EXPOSE 9090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]