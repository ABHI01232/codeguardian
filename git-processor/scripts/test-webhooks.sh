API_BASE="http://localhost:8081"

echo "🧪 Testing Git Processor Webhook Endpoints..."

# Test health endpoint
echo "🔍 Testing health endpoint..."
curl -X GET $API_BASE/webhooks/health | jq '.'

echo -e "\n"

# Test GitHub webhook
echo "📡 Testing GitHub webhook..."
curl -X POST \
  -H "Content-Type: application/json" \
  -H "X-GitHub-Event: push" \
  -H "X-GitHub-Delivery: test-delivery-123" \
  -d '{
    "ref": "refs/heads/main",
    "before": "oldsha",
    "after": "newsha",
    "repository": {
      "id": 12345,
      "name": "test-repo",
      "full_name": "test-org/test-repo",
      "html_url": "https://github.com/test-org/test-repo",
      "clone_url": "https://github.com/test-org/test-repo.git",
      "default_branch": "main"
    },
    "pusher": {
      "name": "test-user",
      "email": "test@example.com"
    },
    "commits": [{
      "id": "abc123",
      "message": "Test commit",
      "timestamp": "2024-01-01T12:00:00Z",
      "url": "https://github.com/test-org/test-repo/commit/abc123",
      "author": {
        "name": "test-author",
        "email": "author@example.com",
        "username": "test-author"
      },
      "added": ["new-file.java"],
      "modified": ["existing-file.java"],
      "removed": ["old-file.java"]
    }]
  }' \
  $API_BASE/webhooks/github | jq '.'

echo -e "\n"

# Test GitLab webhook
echo "📡 Testing GitLab webhook..."
curl -X POST \
  -H "Content-Type: application/json" \
  -H "X-Gitlab-Event: Push Hook" \
  -H "X-Gitlab-Event-UUID: test-event-456" \
  -d '{
    "object_kind": "push",
    "ref": "refs/heads/main",
    "checkout_sha": "newsha",
    "before": "oldsha",
    "after": "newsha",
    "project": {
      "id": 67890,
      "name": "test-gitlab-repo",
      "web_url": "https://gitlab.com/test-org/test-gitlab-repo",
      "git_http_url": "https://gitlab.com/test-org/test-gitlab-repo.git",
      "default_branch": "main"
    },
    "commits": [{
      "id": "def456",
      "message": "GitLab test commit",
      "timestamp": "2024-01-01T12:00:00Z",
      "url": "https://gitlab.com/test-org/test-gitlab-repo/commit/def456",
      "author": {
        "name": "gitlab-author",
        "email": "gitlab@example.com"
      },
      "added": ["new-gitlab-file.java"],
      "modified": ["existing-gitlab-file.java"],
      "removed": ["old-gitlab-file.java"]
    }]
  }' \
  $API_BASE/webhooks/gitlab | jq '.'

echo -e "\n"

# Test repository endpoints
echo "📚 Testing repository endpoints..."
curl -X GET $API_BASE/api/repositories | jq '.'

echo -e "\n✅ Webhook tests completed"

# ==================================
# 16. Application Properties for different environments
# git-processor/src/main/resources/application-docker.yml
# ==================================
server:
  port: 8081

spring:
  application:
    name: git-processor
  datasource:
    url: jdbc:postgresql://postgres:5432/codeguardian
    username: ${DB_USERNAME:codeguardian}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    show-sql: false
  kafka:
    bootstrap-servers: kafka:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: git-processor-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer

webhook:
  github:
    secret: ${GITHUB_WEBHOOK_SECRET:}
  gitlab:
    token: ${GITLAB_WEBHOOK_TOKEN:}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

logging:
  level:
    com.codeguardian: INFO
    org.springframework.kafka: WARN
    org.hibernate: WARN