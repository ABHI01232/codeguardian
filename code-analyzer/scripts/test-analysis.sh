#!/bin/bash

# Test Code Analysis Endpoint
echo "Testing Code Analysis API..."

# Health check
echo "1. Health check..."
curl -X GET http://localhost:8082/api/analysis/health

echo -e "\n\n2. Testing commit analysis..."
curl -X POST http://localhost:8082/api/analysis/commit \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-commit-123",
    "repositoryUrl": "https://github.com/test/repo.git",
    "branch": "main",
    "commitHash": "abc123",
    "commitMessage": "Test commit",
    "author": "test@example.com",
    "timestamp": "2023-01-01T12:00:00",
    "changes": [
      {
        "filename": "test.java",
        "status": "modified",
        "content": "public class Test { private String password = \"hardcoded123\"; }",
        "additions": 1,
        "deletions": 0
      }
    ]
  }'

echo -e "\n\nAnalysis test completed!"