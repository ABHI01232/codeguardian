FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY mvnw .
COPY .mvn ./.mvn

# Make mvnw executable and build
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

# Install git (required for git operations)
RUN apk add --no-cache git curl

# Create app user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy the built jar
COPY --from=builder /app/target/*.jar app.jar

# Create directories and set permissions
RUN mkdir -p /tmp/codeguardian/repos && \
    chown -R appuser:appgroup /app /tmp/codeguardian

# Switch to non-root user
USER appuser

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]