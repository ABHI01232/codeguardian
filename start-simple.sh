#!/bin/bash

echo "🚀 Starting CodeGuardian - Simplified Version"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 21 or later."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.8 or later."
    exit 1
fi

echo "✅ Prerequisites check passed"
echo ""

# Start the simplified application
echo "🌐 Starting CodeGuardian API (Single Application)..."
cd codeguardian-backend

echo "📦 Compiling application..."
mvn clean compile -q

echo "🚀 Starting Spring Boot application..."
echo ""
echo "✅ CodeGuardian is starting!"
echo ""
echo "📋 Available Services:"
echo "   • Main API: http://localhost:8080"
echo "   • Swagger UI: http://localhost:8080/swagger-ui.html"
echo "   • H2 Database Console: http://localhost:8080/h2-console"
echo "   • Health Check: http://localhost:8080/actuator/health"
echo ""
echo "🧪 Test the Analysis API:"
echo "   1. Open Swagger UI: http://localhost:8080/swagger-ui.html"
echo "   2. Create a repository using POST /api/repositories"
echo "   3. Trigger analysis using POST /api/repositories/{id}/analyze"
echo "   4. Watch the console for security analysis results!"
echo ""
echo "🛑 To stop: Press Ctrl+C"
echo ""

# Start the application
mvn spring-boot:run