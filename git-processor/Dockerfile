FROM openjdk:21-jdk-slim

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy entire project for build
COPY . .

# Build only the git-processor module
RUN mvn clean package -DskipTests -pl git-processor -am

# Expose port
EXPOSE 8081

# Run application
CMD ["java", "-jar", "git-processor/target/git-processor-1.0.0-SNAPSHOT.jar", "--spring.profiles.active=docker"]