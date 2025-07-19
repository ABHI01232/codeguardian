FROM openjdk:21-jdk-slim

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy entire project for build
COPY . .

# Build only the api-gateway module
RUN mvn clean package -DskipTests -pl api-gateway -am

# Expose port
EXPOSE 8080

# Run application
CMD ["java", "-jar", "api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar", "--spring.profiles.active=docker"]