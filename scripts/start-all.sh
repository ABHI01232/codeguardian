#!/bin/bash

# Start all CodeGuardian services
echo "Starting CodeGuardian Services..."

# Build all services
echo "Building services..."
docker-compose build

# Start all services
echo "Starting services..."
docker-compose up -d

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 30

# Check service health
echo "Checking service health..."
echo "API Gateway: $(curl -s http://localhost:8080/actuator/health | jq -r .status)"
echo "Git Processor: $(curl -s http://localhost:8081/actuator/health | jq -r .status)"
echo "Code Analyzer: $(curl -s http://localhost:8082/actuator/health | jq -r .status)"

echo "All services started successfully!"
echo "API Gateway: http://localhost:8080"
echo "Git Processor: http://localhost:8081"
echo "Code Analyzer: http://localhost:8082"