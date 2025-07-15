# 📥 Git Processor Service

**Handles webhook events from GitHub and GitLab, processes repository data, and triggers code analysis.**

## 🎯 Purpose

The Git Processor service is responsible for:

- **Webhook Processing** - Handle GitHub/GitLab push events
- **Repository Management** - Store and track repository metadata
- **Commit Processing** - Extract and analyze commit information
- **Analysis Triggering** - Send analysis requests via Kafka
- **Security Validation** - Verify webhook signatures

## 🏗️ Architecture

```
GitHub/GitLab → Webhook → Git Processor → Kafka → Code Analyzer
                   ↓            ↓
               Signature    PostgreSQL
               Validation   (Repos/Commits)
```

## 🚀 Quick Start

### Option 1: Development Mode
```bash
cd git-processor
./scripts/start-dev.sh
```

### Option 2: With Infrastructure
```bash
# Start dependencies
docker-compose up -d postgres kafka redis

# Run application
mvn spring-boot:run -Dspring.profiles.active=docker
```

### Option 3: Docker
```bash
cd git-processor
docker build -t codeguardian/git-processor .
docker run -p 8081:8081 codeguardian/git-processor
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

### Git Repository Settings
```bash
# Local path for cloning repositories
GIT_CLONE_PATH=/tmp/codeguardian/repos
```

## 📡 Webhook Endpoints

### GitHub Webhooks
```bash
POST /webhook/github
Headers:
  X-GitHub-Event: push
  X-Hub-Signature-256: sha256=...
  Content-Type: application/json
Body: GitHub webhook payload
```

### GitLab Webhooks
```bash
POST /webhook/gitlab
Headers:
  X-Gitlab-Event: Push Hook
  X-Gitlab-Token: your-token
  Content-Type: application/json
Body: GitLab webhook payload
```

## 🔗 API Endpoints

### Repository Management
```bash
GET  /api/repositories           # List all repositories
GET  /api/repositories/{id}      # Get repository details
POST /api/repositories           # Create repository
PUT  /api/repositories/{id}      # Update repository
DELETE /api/repositories/{id}    # Delete repository
```

### Commit Management
```bash
GET  /api/repositories/{id}/commits     # List commits for repository
GET  /api/commits/{id}                  # Get commit details
POST /api/commits/{id}/analyze          # Trigger manual analysis
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
  clone_url TEXT,
  default_branch VARCHAR(255),
  is_private BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
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
  author_email VARCHAR(255),
  committed_at TIMESTAMP,
  analysis_status VARCHAR(50) DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT NOW()
);
```

## 🔄 Webhook Processing Flow

```
1. Webhook Received → Signature Verification
2. Payload Validation → Event Type Check
3. Repository Processing → Database Storage
4. Commit Extraction → File Changes Analysis
5. Kafka Message → Analysis Request Sent
6. Response → Webhook Acknowledged
```

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify -Pintegration-tests
```

### Webhook Testing
```bash
# Test GitHub webhook
./scripts/test-webhooks.sh

# Manual webhook test
curl -X POST http://localhost:8081/webhook/github \
  -H "X-GitHub-Event: push" \
  -H "X-Hub-Signature-256: sha256=..." \
  -H "Content-Type: application/json" \
  -d @test-payload.json
```

### Repository API Testing
```bash
# List repositories
curl http://localhost:8081/api/repositories

# Get specific repository
curl http://localhost:8081/api/repositories/1

# Get repository commits
curl http://localhost:8081/api/repositories/1/commits
```

## 📊 Monitoring

### Health Checks
```bash
# Service health
curl http://localhost:8081/actuator/health

# Database health
curl http://localhost:8081/actuator/health/db

# Kafka health
curl http://localhost:8081/actuator/health/kafka
```

### Metrics
```bash
# Application metrics
curl http://localhost:8081/actuator/metrics

# Webhook processing metrics
curl http://localhost:8081/actuator/metrics/webhook.processed

# Repository count
curl http://localhost:8081/actuator/metrics/repositories.count
```

## 🔧 Development

### Prerequisites
```bash
# Required
- Java 17+
- Maven 3.6+
- PostgreSQL (or Docker)
- Kafka (or Docker)

# Optional
- Git (for repository cloning)
- Docker & Docker Compose
```

### Local Development Setup
```bash
# 1. Start infrastructure
docker-compose up -d postgres kafka zookeeper

# 2. Set environment variables
export DB_USERNAME=codeguardian
export DB_PASSWORD=password
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export GITHUB_WEBHOOK_SECRET=your-secret
export GITLAB_WEBHOOK_TOKEN=your-token

# 3. Run application
mvn spring-boot:run -Dspring.profiles.active=local

# 4. Run tests
./scripts/run-tests.sh
```

### Code Style
- Follow Google Java Style Guide
- Use Lombok for boilerplate reduction
- Write tests for all webhook handlers
- Document all public APIs

## 🐛 Troubleshooting

### Common Issues

**1. Webhook signature validation failed**
```bash
# Check webhook secret configuration
echo $GITHUB_WEBHOOK_SECRET
echo $GITLAB_WEBHOOK_TOKEN

# Verify signature calculation
# GitHub uses HMAC-SHA256
# GitLab uses simple token comparison
```

**2. Database connection failed**
```bash
# Check PostgreSQL is running
docker logs postgres-container

# Verify database credentials
psql -h localhost -U codeguardian -d codeguardian

# Check connection string
echo $DB_URL
```

**3. Kafka connection issues**
```bash
# Check Kafka is running
docker logs kafka-container

# Verify topics exist
kafka-topics --bootstrap-server localhost:9092 --list

# Check consumer groups
kafka-consumer-groups --bootstrap-server localhost:9092 --list
```

**4. Git repository cloning failed**
```bash
# Check git clone path permissions
ls -la /tmp/codeguardian/repos

# Verify repository URL is accessible
git clone https://github.com/user/repo.git /tmp/test-clone
```

## 📁 Project Structure

```
git-processor/
├── src/
│   ├── main/
│   │   ├── java/com/codeguardian/gitprocessor/
│   │   │   ├── GitProcessorApplication.java
│   │   │   ├── config/
│   │   │   │   ├── KafkaConfig.java
│   │   │   │   └── KafkaTopicsConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── WebhookController.java
│   │   │   │   └── RepositoryController.java
│   │   │   ├── entity/
│   │   │   │   ├── RepositoryEntity.java
│   │   │   │   └── CommitEntity.java
│   │   │   ├── model/
│   │   │   │   ├── GitHubWebhookPayload.java
│   │   │   │   └── GitLabWebhookPayload.java
│   │   │   ├── repository/
│   │   │   │   ├── RepositoryRepository.java
│   │   │   │   └── CommitRepository.java
│   │   │   ├── service/
│   │   │   │   ├── WebhookProcessorService.java
│   │   │   │   └── GitService.java
│   │   │   └── security/
│   │   │       └── WebhookSecurityValidator.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       └── application-test.yml
│   └── test/
│       └── java/com/codeguardian/gitprocessor/
│           ├── integration/
│           │   └── WebhookIntegrationTest.java
│           └── service/
│               └── WebhookProcessorServiceTest.java
├── scripts/
│   ├── start-dev.sh
│   ├── run-tests.sh
│   └── test-webhooks.sh
├── Dockerfile
├── pom.xml
└── README.md
```

## 🚀 Deployment

### Docker Compose
```yaml
# Already configured in root docker-compose.yml
git-processor:
  build: ./git-processor
  ports:
    - "8081:8081"
  depends_on:
    - postgres
    - kafka
  environment:
    - DB_USERNAME=codeguardian
    - DB_PASSWORD=password
    - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

### Kubernetes
```yaml
# Example Kubernetes deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: git-processor
spec:
  replicas: 2
  selector:
    matchLabels:
      app: git-processor
  template:
    metadata:
      labels:
        app: git-processor
    spec:
      containers:
      - name: git-processor
        image: codeguardian/git-processor:latest
        ports:
        - containerPort: 8081
        env:
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
```

## 📋 Webhook Setup Guide

### GitHub Repository Setup
1. Go to repository Settings → Webhooks
2. Click "Add webhook"
3. Payload URL: `http://your-domain:8080/api/git/webhook/github`
4. Content type: `application/json`
5. Secret: Your GitHub webhook secret
6. Events: Select "Push events" and "Pull requests"

### GitLab Repository Setup
1. Go to project Settings → Webhooks
2. URL: `http://your-domain:8080/api/git/webhook/gitlab`
3. Secret Token: Your GitLab webhook token
4. Trigger: Select "Push events" and "Merge request events"
5. Enable SSL verification if using HTTPS

## 🔗 Related Services

- **[API Gateway](../api-gateway/README.md)** - Routes webhooks to this service
- **[Code Analyzer](../code-analyzer/README.md)** - Receives analysis requests
- **[Root Documentation](../README.md)** - Complete system overview

---

**For more information, see the [main project README](../README.md)**