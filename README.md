# CodeGuardian - AI-Powered Code Analysis Platform

> Enterprise-grade code analysis platform with real-time security scanning and quality assessment.

## 🏗️ Architecture Overview

CodeGuardian is a microservices-based platform consisting of:

- **[API Gateway](./api-gateway/README.md)** - Entry point, authentication, routing
- **[Git Processor](./git-processor/README.md)** - Webhook processing, repository management
- **[Code Analyzer](./code-analyzer/README.md)** - AI-powered code analysis engine

## 🚀 Quick Start

### Prerequisites
- Java 21
- Docker & Docker Compose
- Maven 3.9+

### 1. Clone and Start
```bash
git clone https://github.com/your-org/codeguardian.git
cd codeguardian
docker-compose up -d
```

### 2. Build All Modules
```bash
mvn clean install
```

### 3. Access Services
- API Gateway: http://localhost:8080
- Git Processor: http://localhost:8081
- Code Analyzer: http://localhost:8082
- Kafka UI: http://localhost:8090

## 📊 Service Communication

```
GitHub/GitLab → API Gateway → Git Processor → Kafka → Code Analyzer
                     ↓              ↓               ↓
                  Redis Cache   PostgreSQL    AI Analysis
```

## 🔧 Configuration

See individual module READMEs for detailed configuration:
- [API Gateway Configuration](./api-gateway/README.md#configuration)
- [Git Processor Configuration](./git-processor/README.md#configuration)
- [Code Analyzer Configuration](./code-analyzer/README.md#configuration)

## 🧪 Testing

```bash
# Test all modules
mvn clean verify

# Test specific module
cd git-processor
mvn test
```

## 📚 Documentation

- [API Documentation](./docs/api.md)
- [Deployment Guide](./docs/deployment.md)
- [Contributing Guide](./CONTRIBUTING.md)

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

