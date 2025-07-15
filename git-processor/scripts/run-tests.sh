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