# 🛡️ CodeGuardian - AI-Powered Security Analysis Platform

**Enterprise-grade security analysis platform for banking and financial applications**

CodeGuardian is a comprehensive, microservices-based security analysis platform that automatically scans Git repositories for security vulnerabilities, code quality issues, and compliance violations. Built specifically for banking and financial applications with strict security requirements.

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   GitHub/GitLab │    │   Developer     │    │   Security Team │
│                 │    │                 │    │                 │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          ▼                      ▼                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                    🌐 API Gateway (Port 8080)                   │
│         JWT Auth • Rate Limiting • CORS • Routing              │
└─────────────────┬───────────────────────┬───────────────────────┘
                  │                       │
                  ▼                       ▼
    ┌─────────────────────────────┐    ┌─────────────────────────────┐
    │  📥 Git Processor (8081)    │    │  🔍 Code Analyzer (8082)   │
    │  • Webhook Processing       │    │  • Security Analysis       │
    │  • Repository Management    │    │  • Quality Assessment      │
    │  • Git Operations           │    │  • Compliance Checking     │
    └─────────────────────────────┘    └─────────────────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- Git

### Option 1: Docker Compose (Recommended - No PostgreSQL Installation Required)
```bash
# Clone repository
git clone <repository-url>
cd CodeGuardian

# Start all services (includes PostgreSQL, Kafka, Redis)
./scripts/start-all.sh

# Verify services
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # Git Processor  
curl http://localhost:8082/actuator/health  # Code Analyzer

# Test the system
cd git-processor && ./scripts/test-webhooks.sh
cd code-analyzer && ./scripts/test-analysis.sh
```

### Option 2: Local Development (Manual Setup)
```bash
# 1. Install PostgreSQL (optional if using Docker)
brew install postgresql  # macOS
sudo apt-get install postgresql  # Ubuntu

# 2. Start infrastructure services
docker-compose up -d postgres redis kafka zookeeper

# 3. Start each service in separate terminals
cd api-gateway && ./scripts/start-dev.sh
cd git-processor && ./scripts/start-dev.sh
cd code-analyzer && ./scripts/start-dev.sh
```

## 🔄 System Flow

### Background Process Flow
```
1. Code Push → GitHub/GitLab sends webhook
2. API Gateway → Routes to Git Processor
3. Git Processor → Validates, clones repo, extracts changes
4. Kafka Message → Sends analysis request to Code Analyzer
5. Code Analyzer → Analyzes security, quality, compliance
6. Results → Stores in database, sends notifications
```

### Detailed Flow Diagram
```
GitHub/GitLab Push → API Gateway → Git Processor → Kafka → Code Analyzer
                         ↓              ↓               ↓
                     Redis Cache    PostgreSQL    Analysis Engine
                         ↓              ↓               ↓
                     JWT/Sessions   Repo/Commits   Security Reports
```

## 🛠️ Build & Test Commands

### Build All Modules
```bash
mvn clean compile
```

### Run All Tests
```bash
mvn test
```

### Run Individual Module Tests
```bash
cd git-processor && ./scripts/run-tests.sh
cd code-analyzer && ./scripts/run-tests.sh
```

### Build Docker Images
```bash
./scripts/build.sh
```

## 🐳 Docker Usage

### What Dockerfiles Do
- **Containerization**: Each service runs in isolated containers
- **Environment Consistency**: Same environment across dev/prod
- **Dependency Management**: All dependencies bundled (Java, Maven, etc.)
- **Easy Deployment**: No need to install dependencies on host machine

### When Dockerfiles Are Used
- `docker-compose up` - Builds and runs all services
- `./scripts/build.sh` - Builds Docker images
- Production deployment

### Stop Services
```bash
./scripts/stop-all.sh

# Stop and clean volumes
./scripts/stop-all.sh --clean
```

## 📁 Project Structure

```
CodeGuardian/
├── api-gateway/           # API Gateway service
│   ├── src/main/java/     # Spring Cloud Gateway
│   ├── Dockerfile         # Container configuration
│   └── scripts/           # Development scripts
├── git-processor/         # Git webhook processor
│   ├── src/main/java/     # Webhook handling & Git ops
│   ├── src/test/java/     # Unit & integration tests
│   └── scripts/           # Development scripts
├── code-analyzer/         # Security analysis engine
│   ├── src/main/java/     # Analysis algorithms
│   ├── src/test/java/     # Unit tests
│   └── scripts/           # Development scripts
├── docker-compose.yml     # Multi-service setup
├── scripts/               # Root-level scripts
└── README.md             # This file
```

## 🔧 Configuration

### Service URLs
- **API Gateway**: http://localhost:8080
- **Git Processor**: http://localhost:8081
- **Code Analyzer**: http://localhost:8082

### Environment Variables
```bash
# Database
DB_USERNAME=codeguardian
DB_PASSWORD=password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Webhooks
GITHUB_WEBHOOK_SECRET=your-github-webhook-secret
GITLAB_WEBHOOK_TOKEN=your-gitlab-webhook-token

# JWT
JWT_SECRET=your-256-bit-secret-key
```

## 🛡️ Security Features

### Security Analysis
- **Vulnerability Detection**: SQL injection, XSS, hardcoded secrets
- **Crypto Analysis**: Weak encryption, insecure random generation
- **Authentication Issues**: JWT vulnerabilities, session management
- **Input Validation**: Parameter tampering, injection attacks

### Code Quality
- **Complexity Analysis**: Cyclomatic complexity, method length
- **Code Smells**: Magic numbers, long parameter lists
- **Best Practices**: Naming conventions, error handling
- **Performance**: Memory leaks, inefficient algorithms

### Compliance Frameworks
- **PCI-DSS**: Payment card industry standards
- **GDPR**: Data privacy regulations
- **HIPAA**: Healthcare information protection
- **SOX**: Financial reporting compliance
- **OWASP**: Web application security

## 🔄 Webhook Integration

### GitHub Setup
```bash
# Repository Settings → Webhooks → Add webhook
URL: http://your-domain:8080/api/git/webhook/github
Content type: application/json
Secret: your-github-webhook-secret
Events: Push, Pull requests
```

### GitLab Setup
```bash
# Project Settings → Webhooks → Add webhook
URL: http://your-domain:8080/api/git/webhook/gitlab
Secret Token: your-gitlab-webhook-token
Trigger: Push events, Merge request events
```

## 📈 Monitoring & Health Checks

### Health Checks
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

### Metrics (Prometheus)
```bash
curl http://localhost:8080/actuator/prometheus
curl http://localhost:8081/actuator/prometheus
curl http://localhost:8082/actuator/prometheus
```

## 📚 Module Documentation

- **[API Gateway](./api-gateway/README.md)** - Entry point, authentication, routing
- **[Git Processor](./git-processor/README.md)** - Webhook processing, repository management
- **[Code Analyzer](./code-analyzer/README.md)** - AI-powered code analysis engine

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support and questions:
- Create an issue on GitHub
- Contact: support@codeguardian.com
- Documentation: https://docs.codeguardian.com

---

**Built with ❤️ for secure banking applications**