# Git Processor Service

Handles webhook events from GitHub and GitLab, processes repository data, and triggers code analysis.

## 🎯 Purpose

- **Webhook Processing** - Handle GitHub/GitLab push events
- **Repository Management** - Store and track repository metadata
- **Commit Processing** - Extract and analyze commit information
- **Analysis Triggering** - Send analysis requests via Kafka
- **Security Validation** - Verify webhook signatures

## 🚀 Quick Start

### Run Standalone
```bash
cd git-processor
./mvnw spring-boot:run
```

### Run with Infrastructure
```bash
# Start dependencies
docker-compose up -d postgres kafka redis

# Run application
./mvnw spring-boot:run -Dspring.profiles.active=docker
```

## ⚙️ Configuration

### Webhook Security
```bash
# GitHub webhook secret
GITHUB_WEBHOOK_SECRET=your-github-secret

# GitLab webhook token
GITLAB_WEBHOOK_TOKEN=your-gitlab-token
```

### Database Configuration
```bash
DB_USERNAME=codeguardian
DB_PASSWORD=password
DB_URL=jdbc:postgresql://localhost:5432/codeguardian
```

### Kafka Configuration
```bash
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## 📡 Webhook Endpoints

### GitHub Webhooks
```bash
POST /webhooks/github
Headers:
  X-GitHub-Event: push
  X-Hub-Signature-256: sha256=...
  Content-Type: application/json
```

### GitLab Webhooks
```bash
POST /webhooks/gitlab
Headers:
  X-Gitlab-Event: Push Hook
  X-Gitlab-Token: your-token
  Content-Type: application/json
```

## 🗄️ Database Schema

### Repositories Table
```sql
CREATE TABLE repositories (
  id BIGSERIAL PRIMARY KEY,
  external_id VARCHAR(255) UNIQUE NOT NULL,
  name VARCHAR(255) NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  platform VARCHAR(50) NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);
```

### Commits Table
```sql
CREATE TABLE commits (
  id BIGSERIAL PRIMARY KEY,
  commit_id VARCHAR(255) UNIQUE NOT NULL,
  repository_id BIGINT REFERENCES repositories(id),
  message TEXT,
  author_name VARCHAR(255),
  analysis_status VARCHAR(50) DEFAULT 'PENDING'
);
```

## 🧪 Testing

### Webhook Testing
```bash
# Test GitHub webhook
curl -X POST http://localhost:8081/webhooks/github \
  -H "X-GitHub-Event: push" \
  -H "Content-Type: application/json" \
  -d @test-payload.json
```

### Repository API Testing
```bash
# List repositories
curl http://localhost:8081/api/repositories

# Get specific repository
curl http://localhost:8081/api/repositories/1
```

## 📊 Monitoring

- **Webhook Health**: `/webhooks/health`
- **Metrics**: `/actuator/metrics`
- **Repository Count**: Check via API endpoints

## 🔧 Development

### Local Development Setup
```bash
# Start infrastructure
cd scripts
./start-dev.sh

# Run tests
./mvnw test

# Run with live reload
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local"
```

### Adding New Webhook Handlers
1. Create payload model in `model/` package
2. Add handler method in `WebhookProcessorService`
3. Update `WebhookController` with new endpoint
4. Add tests and documentation
```