#!/bin/bash

# Build Code Analyzer Service
echo "Building Code Analyzer Service..."

# Clean and build
mvn clean package -DskipTests

# Build Docker image
docker build -t codeguardian/code-analyzer:latest .

echo "Build completed successfully!"