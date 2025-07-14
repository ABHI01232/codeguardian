set -e

echo "🚀 Starting Git Processor Development Environment..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start infrastructure services
echo "🐘 Starting PostgreSQL..."
docker-compose -f ../docker-compose.yaml up -d postgres

echo "📦 Starting Redis..."
docker-compose -f ../docker-compose.yaml up -d redis

echo "📊 Starting Kafka and Zookeeper..."
docker-compose -f ../docker-compose.yaml up -d zookeeper kafka

echo "🎛️ Starting Kafka UI..."
docker-compose -f ../docker-compose.yaml up -d kafka-ui

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

echo "✅ Infrastructure is ready!"
echo ""
echo "📍 Service URLs:"
echo "  - PostgreSQL: localhost:5432"
echo "  - Redis: localhost:6379"
echo "  - Kafka: localhost:9092"
echo "  - Kafka UI: http://localhost:8090"
echo ""
echo "🚀 You can now start the Git Processor application:"
echo "  mvn spring-boot:run"

# ==================================
# 14. Test Scripts
# git-processor/scripts/run-tests.sh
# ==================================
#!/bin/bash

echo "🧪 Running Git Processor Tests..."

# Run unit tests
echo "📝 Running unit tests..."
mvn clean test

# Run integration tests
echo "🔗 Running integration tests..."
mvn clean verify -Pintegration-tests

# Generate test report
echo "📊 Generating test coverage report..."
mvn jacoco:report

echo "✅ All tests completed!"
echo ""
echo "📊 Coverage report: target/site/jacoco/index.html"