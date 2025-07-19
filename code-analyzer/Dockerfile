FROM openjdk:21-jdk-slim

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy entire project for build
COPY . .

# Build only the code-analyzer module
RUN mvn clean package -DskipTests -pl code-analyzer -am

# Run application
EXPOSE 8082
CMD ["java", "-jar", "code-analyzer/target/code-analyzer-1.0.0-SNAPSHOT.jar", "--spring.profiles.active=docker"]