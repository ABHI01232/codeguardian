# 🛡️ CodeGuardian - Real-Time Security Analysis Platform

**Enterprise-grade real-time security analysis platform for banking and financial applications**

CodeGuardian is a comprehensive, microservices-based security analysis platform that automatically scans Git repositories for security vulnerabilities, code quality issues, and compliance violations in real-time. Built specifically for banking and financial applications with strict security requirements, featuring live dashboards, WebSocket notifications, and AI-powered analysis.

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   GitHub/GitLab │    │   Developer     │    │   Security Team │
│                 │    │                 │    │                 │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          ▼                      ▼                      ▼
┌─────────────────────────────────────────────────────────────────┐
│              🌐 API Gateway (Port 8080)                         │
│    JWT Auth • Rate Limiting • CORS • Routing • WebSocket       │
└─────────────────┬───────────────────────┬───────────────────────┘
                  │                       │
                  ▼                       ▼
    ┌─────────────────────────────┐    ┌─────────────────────────────┐
    │  📥 Git Processor (8081)    │    │  🔍 Code Analyzer (8082)   │
    │  • Webhook Processing       │    │  • Security Analysis       │
    │  • Repository Management    │    │  • Quality Assessment      │
    │  • Git Operations           │    │  • Compliance Checking     │
    └─────────────────────────────┘    └─────────────────────────────┘
                  │                                      │
                  ▼                                      ▼
    ┌─────────────────────────────────────────────────────────────────┐
    │                    📱 React Frontend (Port 3000)                │
    │          Live Dashboard • Real-time Updates • WebSocket         │
    └─────────────────────────────────────────────────────────────────┘
```

## ✨ Key Features

### 🔴 Real-Time Analysis
- **Live Vulnerability Detection**: Instant security analysis on code commits
- **WebSocket Notifications**: Real-time alerts to connected dashboards
- **Stream Processing**: Kafka-based event streaming for scalability

### 📊 Interactive Dashboard
- **React Frontend**: Modern, responsive web interface
- **Live Updates**: Real-time display of analysis results
- **Security Metrics**: Visual charts and trend analysis
- **Repository Overview**: Comprehensive repository health monitoring

### 🛡️ Advanced Security Detection
- **Pattern-Based Analysis**: 6+ security vulnerability types
- **CWE Mapping**: Common Weakness Enumeration classification
- **Severity Scoring**: CRITICAL, HIGH, MEDIUM, LOW prioritization
- **Detailed Remediation**: Specific fix recommendations

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Maven 3.8+
- Docker & Docker Compose

### Option 1: Backend Only (Simplified - API Testing)
```bash
# Start the backend API with Swagger documentation
./start-backend.sh

# Access Swagger UI for API testing
open http://localhost:8080/swagger-ui.html

# Test APIs using the interactive documentation
# - Add repositories
# - Trigger security analysis  
# - View results
```

### Option 2: Full Stack (Backend + Frontend)
```bash
# Start all services (includes PostgreSQL, Kafka, Redis, Frontend)
docker-compose -f docker-compose.yml up -d

# Verify services
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # Git Processor  
curl http://localhost:8082/actuator/health  # Code Analyzer

# Access the dashboard
open http://localhost:3000  # React Frontend Dashboard
```

### Option 3: Local Development
```bash
# 1. Start infrastructure services
docker-compose up -d postgres redis kafka zookeeper

# 2. Start API Gateway (main service)
cd api-gateway && mvn spring-boot:run

# 3. Optional: Start other microservices in separate terminals
cd git-processor && mvn spring-boot:run
cd code-analyzer && mvn spring-boot:run
```

## 🔄 System Flow

### Real-Time Analysis Flow
```
1. Code Push → GitHub/GitLab sends webhook
2. API Gateway → Routes to Git Processor
3. Git Processor → Validates, saves commit, extracts file changes
4. Kafka Message → Sends analysis request to Code Analyzer
5. Code Analyzer → Performs real security analysis on code content
6. Results → Stores in database, sends to WebSocket notifications
7. Frontend → Receives real-time updates via WebSocket
```

### Detailed Flow Diagram
```
GitHub/GitLab Push → API Gateway → Git Processor → Kafka → Code Analyzer
                         ↓              ↓               ↓           ↓
                     Redis Cache    PostgreSQL    Analysis Engine  Kafka
                         ↓              ↓               ↓           ↓
                     JWT/Sessions   Repo/Commits   Security Reports  WebSocket
                                                                     ↓
                                                             React Frontend
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
│   ├── src/main/java/     # Spring Cloud Gateway + WebSocket
│   ├── Dockerfile         # Container configuration
│   └── scripts/           # Development scripts
├── git-processor/         # Git webhook processor
│   ├── src/main/java/     # Webhook handling & Git ops
│   ├── src/test/java/     # Unit & integration tests
│   └── scripts/           # Development scripts
├── code-analyzer/         # Security analysis engine
│   ├── src/main/java/     # Analysis algorithms + Git integration
│   ├── src/test/java/     # Unit tests
│   └── scripts/           # Development scripts
├── frontend/              # React frontend dashboard
│   ├── src/components/    # React components
│   ├── src/services/      # API & WebSocket services
│   ├── Dockerfile         # Container configuration
│   └── package.json       # Node.js dependencies
├── docker-compose.final.yml # Complete multi-service setup
├── RUN_INSTRUCTIONS.md   # Detailed running instructions
├── scripts/               # Root-level scripts
└── README.md             # This file
```

## 🔧 Configuration

### Service URLs
- **Swagger API Documentation**: http://localhost:8080/swagger-ui.html
- **API Gateway**: http://localhost:8080
- **Frontend Dashboard**: http://localhost:3000
- **Git Processor**: http://localhost:8081
- **Code Analyzer**: http://localhost:8082
- **WebSocket Endpoint**: ws://localhost:8080/ws

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
- **Hardcoded Secrets**: Detects hardcoded passwords, API keys, tokens (CRITICAL)
- **SQL Injection**: Identifies unsafe SQL query construction (HIGH)
- **XSS Vulnerabilities**: Finds Cross-Site Scripting risks (HIGH)
- **Weak Cryptography**: Detects MD5, SHA1 usage (MEDIUM)
- **Insecure Random**: Identifies Math.random() usage (MEDIUM)
- **Debug Exposure**: Finds console.log, print statements (LOW)

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

- **[API Gateway](./api-gateway/README.md)** - Entry point, authentication, routing, WebSocket
- **[Git Processor](./git-processor/README.md)** - Webhook processing, repository management
- **[Code Analyzer](./code-analyzer/README.md)** - AI-powered code analysis engine
- **[Frontend](./frontend/README.md)** - React dashboard with real-time updates

## 🆕 Latest Features

### Real-Time Security Analysis
- **Live Vulnerability Detection**: Detects 6 types of security vulnerabilities in real-time
- **Pattern-Based Analysis**: Uses regex patterns to identify hardcoded secrets, SQL injection, XSS, weak crypto, insecure random, and debug exposure
- **CWE Classification**: Maps vulnerabilities to Common Weakness Enumeration standards
- **Severity Scoring**: Categorizes issues as CRITICAL, HIGH, MEDIUM, or LOW

### WebSocket Integration
- **Real-Time Notifications**: Instant alerts when vulnerabilities are detected
- **Live Dashboard Updates**: Frontend receives updates without page refresh
- **STOMP Protocol**: Uses STOMP over WebSocket for reliable messaging
- **Connection Management**: Automatic reconnection and error handling

### Interactive Frontend
- **React 18**: Modern React components with hooks
- **TailwindCSS**: Responsive, modern UI design
- **Real-Time Updates**: Live display of security analysis results
- **WebSocket Client**: Connects to backend for real-time notifications
- **Chart.js Integration**: Visual representation of security metrics

### Enhanced Architecture
- **Microservices**: Distributed architecture with independent services
- **Kafka Streaming**: Event-driven architecture for scalability
- **Docker Containerization**: Easy deployment and scaling
- **PostgreSQL**: Robust data persistence
- **Redis Caching**: Performance optimization

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