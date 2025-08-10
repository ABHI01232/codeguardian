
#!/bin/bash

echo "🚀 Starting CodeGuardian with Kafka & Redis for Learning"
echo ""

# Check prerequisites
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker to run Kafka & Redis."
    echo "💡 Alternative: Run './start-simple.sh' for embedded H2 database only."
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 21 or later."
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.8 or later."
    exit 1
fi

echo "✅ Prerequisites check passed"
echo ""

# Start only Kafka and Redis (no complex microservices)
echo "📦 Starting Kafka & Redis containers..."
cat > docker-compose-simple.yml << EOF
version: '3.8'
services:
  redis:
    image: redis:7-alpine
    container_name: codeguardian-redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    container_name: codeguardian-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    container_name: codeguardian-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'
EOF

docker-compose -f docker-compose-simple.yml up -d

echo "⏳ Waiting for services to start..."
sleep 15

# Check if services are running
if docker ps | grep -q "codeguardian-kafka"; then
    echo "✅ Kafka started on localhost:9092"
else
    echo "❌ Kafka failed to start"
fi

if docker ps | grep -q "codeguardian-redis"; then
    echo "✅ Redis started on localhost:6379"
else
    echo "❌ Redis failed to start"
fi

# Start the Spring Boot application
echo ""
echo "🌐 Starting CodeGuardian Spring Boot application..."
cd codeguardian-backend

echo "📦 Compiling application..."
mvn clean compile -q

echo ""
echo "🚀 Starting application with Kafka & Redis integration!"
echo ""
echo "📋 What you'll learn:"
echo "   • How Spring Boot integrates with Kafka"
echo "   • Async message processing with Kafka producers/consumers"
echo "   • Redis caching and data storage"
echo "   • Event-driven architecture patterns"
echo ""
echo "📡 Available Services:"
echo "   • Main API: http://localhost:8080"
echo "   • Swagger UI: http://localhost:8080/swagger-ui.html"
echo "   • H2 Database: http://localhost:8080/h2-console"
echo "   • Redis: localhost:6379"
echo "   • Kafka: localhost:9092"
echo ""
echo "🧪 Test Analysis Flow:"
echo "   1. Add repository via Swagger UI"
echo "   2. Trigger analysis - watch Kafka messages in logs!"
echo "   3. See async processing in action"
echo ""
echo "🛑 To stop everything:"
echo "   • Press Ctrl+C to stop Spring Boot"
echo "   • Run: docker-compose -f docker-compose-simple.yml down"
echo ""

# Start the application
mvn spring-boot:run