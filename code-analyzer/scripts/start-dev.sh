#!/bin/bash

# Start Code Analyzer Service in Development Mode
echo "Starting Code Analyzer Service..."

# Set environment variables
export SPRING_PROFILES_ACTIVE=dev
export DB_USERNAME=codeguardian
export DB_PASSWORD=password
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Build and run the application
mvn clean compile spring-boot:run