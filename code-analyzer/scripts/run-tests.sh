#!/bin/bash

# Run Code Analyzer Tests
echo "Running Code Analyzer Tests..."

# Set test environment
export SPRING_PROFILES_ACTIVE=test

# Run tests
mvn clean test

# Generate test report
mvn surefire-report:report

echo "Tests completed. Report available at target/site/surefire-report.html"