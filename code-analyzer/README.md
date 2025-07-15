# 🔍 Code Analyzer Service

**AI-powered security analysis engine that performs comprehensive code scanning, quality assessment, and compliance checking.**

## 🎯 Purpose

The Code Analyzer service provides:

- **Security Analysis** - Detects vulnerabilities and security issues
- **Quality Assessment** - Code quality metrics and suggestions
- **Compliance Checking** - Regulatory compliance validation
- **Pattern Detection** - Identifies anti-patterns and code smells
- **Report Generation** - Detailed analysis reports

## 🏗️ Architecture

```
Kafka Consumer → Analysis Engine → Result Producer
      ↓               ↓               ↓
  Git Changes    Multi-Engine     Analysis Results
                 Analysis:
                 • Security
                 • Quality  
                 • Compliance
```

## 🚀 Quick Start

### Option 1: Development Mode
```bash
cd code-analyzer
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
cd code-analyzer
docker build -t codeguardian/code-analyzer .
docker run -p 8082:8082 codeguardian/code-analyzer
```

## ⚙️ Configuration

### Analysis Settings
```bash
# Analysis configuration
ANALYSIS_TIMEOUT=300s
MAX_FILE_SIZE=1MB
ANALYSIS_THREADS=10
```

### Database Configuration
```bash
DB_USERNAME=codeguardian
DB_PASSWORD=password
DB_URL=jdbc:postgresql://localhost:5432/codeguardian
```

### Kafka Configuration
```bash
# Input topics
KAFKA_COMMIT_ANALYSIS_TOPIC=commit-analysis
KAFKA_PR_ANALYSIS_TOPIC=pull-request-analysis
KAFKA_MR_ANALYSIS_TOPIC=merge-request-analysis

# Output topics  
KAFKA_ANALYSIS_RESULTS_TOPIC=analysis-results
```

## 🛡️ Security Analysis Features

### Vulnerability Detection
- **SQL Injection** - Detects potential SQL injection vulnerabilities
- **Cross-Site Scripting (XSS)** - Identifies XSS attack vectors
- **Hardcoded Secrets** - Finds hardcoded passwords, tokens, keys
- **Weak Cryptography** - Detects use of weak encryption algorithms
- **Insecure Random** - Identifies insecure random number generation

### Authentication & Authorization
- **Authentication Bypass** - Detects auth bypass patterns
- **Session Management** - Validates session handling
- **Access Control** - Checks authorization mechanisms
- **JWT Vulnerabilities** - Identifies JWT implementation issues

## 🔧 Code Quality Analysis

### Complexity Metrics
- **Cyclomatic Complexity** - Measures code complexity
- **Method Length** - Identifies overly long methods
- **Parameter Count** - Detects methods with too many parameters
- **Nesting Depth** - Measures code nesting levels

### Code Smells
- **Magic Numbers** - Identifies hard-coded numeric values
- **Duplicate Code** - Detects code duplication
- **Long Classes** - Finds overly large classes
- **Dead Code** - Identifies unused code sections

## 📋 Compliance Checking

### Regulatory Frameworks
- **PCI-DSS** - Payment Card Industry Data Security Standard
- **GDPR** - General Data Protection Regulation
- **HIPAA** - Health Insurance Portability and Accountability Act
- **SOX** - Sarbanes-Oxley Act
- **OWASP** - Open Web Application Security Project

### Compliance Rules
```java
// Example compliance violations detected:
- Unencrypted cardholder data (PCI-DSS)
- Personal data without consent handling (GDPR)
- Health information without access controls (HIPAA)
- Financial data without audit trails (SOX)
```

## 📊 Analysis Workflow

```
1. Kafka Message Received → Analysis Request Queued
2. File Processing → Extract changed files
3. Security Analysis → Run security rule engine
4. Quality Analysis → Calculate metrics and detect smells
5. Compliance Check → Validate against regulations
6. Result Aggregation → Combine all analysis results
7. Report Generation → Create structured analysis report
8. Kafka Message Sent → Publish results
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

### Manual Testing
```bash
# Test analysis endpoint
./scripts/test-analysis.sh

# Manual API test
curl -X POST http://localhost:8082/api/analysis/commit \
  -H "Content-Type: application/json" \
  -d '{
    "id": "test-123",
    "repositoryUrl": "https://github.com/example/repo",
    "commitHash": "abc123",
    "changes": [
      {
        "filename": "UserService.java",
        "content": "String password = \"hardcoded123\";",
        "status": "modified"
      }
    ]
  }'
```

### Performance Testing
```bash
# Load test analysis endpoint
ab -n 100 -c 10 http://localhost:8082/actuator/health
```

## 📈 Monitoring & Metrics

### Health Checks
```bash
# Service health
curl http://localhost:8082/actuator/health

# Database health
curl http://localhost:8082/actuator/health/db

# Kafka health
curl http://localhost:8082/actuator/health/kafka
```

### Custom Metrics
```bash
# Analysis queue size
curl http://localhost:8082/actuator/metrics/analysis.queue.size

# Processing time
curl http://localhost:8082/actuator/metrics/analysis.processing.time

# Success rate
curl http://localhost:8082/actuator/metrics/analysis.success.rate
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

# 3. Run application
mvn spring-boot:run -Dspring.profiles.active=local

# 4. Run tests
./scripts/run-tests.sh
```

### Adding New Analysis Rules

**1. Security Rules**
```java
// Add to SecurityAnalysisService.java
new SecurityRule("new_vulnerability", "HIGH", 
    Pattern.compile("(?i)vulnerable_pattern"),
    "CWE-XXX", "Description of vulnerability");
```

**2. Quality Rules**
```java
// Add to CodeQualityAnalysisService.java
new QualityRule("new_smell", "MEDIUM", 
    Pattern.compile("code_smell_pattern"),
    "Description of code smell");
```

**3. Compliance Rules**
```java
// Add to ComplianceAnalysisService.java
new ComplianceRule("NEW-REGULATION-1", "Data protection rule", 
    Pattern.compile("sensitive_data_pattern"), "CRITICAL");
```

## 🎯 Analysis Results

### Security Issue Format
```json
{
  "type": "hardcoded_secret",
  "severity": "CRITICAL",
  "title": "Hardcoded credentials detected",
  "description": "Hardcoded credentials found in source code",
  "filename": "UserController.java",
  "lineNumber": 42,
  "cweId": "CWE-798",
  "recommendation": "Store credentials in environment variables",
  "codeSnippet": "String password = \"hardcoded123\";"
}
```

### Quality Issue Format
```json
{
  "type": "high_complexity",
  "severity": "HIGH",
  "title": "High cyclomatic complexity",
  "description": "Method has cyclomatic complexity of 15",
  "filename": "PaymentService.java",
  "lineNumber": 128,
  "rule": "complexity",
  "suggestion": "Break method into smaller functions",
  "codeSnippet": "public void processPayment(...) { ... }"
}
```

### Compliance Report Format
```json
{
  "framework": "PCI-DSS",
  "overallScore": "GOOD",
  "rules": [
    {
      "ruleId": "PCI-DSS-1",
      "description": "Protect cardholder data",
      "status": "COMPLIANT",
      "severity": "CRITICAL"
    }
  ],
  "violations": [
    "Cardholder data not encrypted in PaymentProcessor.java:45"
  ],
  "recommendations": [
    "Implement encryption for cardholder data storage"
  ]
}
```

## 📁 Project Structure

```
code-analyzer/
├── src/
│   ├── main/
│   │   ├── java/com/codeguardian/analyzer/
│   │   │   ├── CodeAnalyzerApplication.java
│   │   │   ├── config/
│   │   │   │   └── KafkaConfig.java
│   │   │   ├── consumer/
│   │   │   │   └── AnalysisRequestConsumer.java
│   │   │   ├── controller/
│   │   │   │   └── AnalysisController.java
│   │   │   ├── model/
│   │   │   │   ├── AnalysisRequest.java
│   │   │   │   └── AnalysisResult.java
│   │   │   ├── producer/
│   │   │   │   └── AnalysisResultProducer.java
│   │   │   └── service/
│   │   │       ├── CodeAnalysisService.java
│   │   │       ├── SecurityAnalysisService.java
│   │   │       ├── CodeQualityAnalysisService.java
│   │   │       └── ComplianceAnalysisService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       └── application-test.yml
│   └── test/
│       └── java/com/codeguardian/analyzer/
│           └── service/
│               └── SecurityAnalysisServiceTest.java
├── scripts/
│   ├── start-dev.sh
│   ├── run-tests.sh
│   ├── build.sh
│   └── test-analysis.sh
├── Dockerfile
├── pom.xml
└── README.md
```

## 🐛 Troubleshooting

### Common Issues

**1. Analysis timeout**
```bash
# Check analysis timeout configuration
echo $ANALYSIS_TIMEOUT

# Increase timeout if needed
export ANALYSIS_TIMEOUT=600s
```

**2. Kafka connection issues**
```bash
# Check Kafka is running
docker logs kafka-container

# Verify topics exist
kafka-topics --bootstrap-server localhost:9092 --list
```

**3. Database connection failed**
```bash
# Check PostgreSQL is running
docker logs postgres-container

# Verify database credentials
psql -h localhost -U codeguardian -d codeguardian
```

**4. High memory usage**
```bash
# Monitor memory usage
curl http://localhost:8082/actuator/metrics/jvm.memory.used

# Adjust JVM memory settings
export JAVA_OPTS="-Xmx2g -Xms1g"
```

## 🚀 Deployment

### Docker Compose
```yaml
# Already configured in root docker-compose.yml
code-analyzer:
  build: ./code-analyzer
  ports:
    - "8082:8082"
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
  name: code-analyzer
spec:
  replicas: 3
  selector:
    matchLabels:
      app: code-analyzer
  template:
    metadata:
      labels:
        app: code-analyzer
    spec:
      containers:
      - name: code-analyzer
        image: codeguardian/code-analyzer:latest
        ports:
        - containerPort: 8082
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
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
```

## 🔗 Related Services

- **[Git Processor](../git-processor/README.md)** - Sends analysis requests to this service
- **[API Gateway](../api-gateway/README.md)** - Routes API requests to this service
- **[Root Documentation](../README.md)** - Complete system overview

---

**For more information, see the [main project README](../README.md)**