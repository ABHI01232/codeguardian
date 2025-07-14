
# Code Analyzer Service

AI-powered code analysis engine that performs security scanning, quality assessment, and generates insights.

## 🎯 Purpose

- **AI Code Analysis** - Leverages Spring AI with Ollama
- **Security Scanning** - Detects vulnerabilities and security issues
- **Quality Assessment** - Code quality metrics and suggestions
- **Pattern Detection** - Identifies anti-patterns and code smells
- **Report Generation** - Detailed analysis reports

## 🚀 Quick Start

### Prerequisites
```bash
# Install Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# Pull required models
ollama pull codellama:7b
ollama pull llama2:7b
```

### Run Service
```bash
cd code-analyzer
./mvnw spring-boot:run
```

## ⚙️ Configuration

### AI Model Configuration
```bash
# Ollama configuration
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=codellama:7b

# Analysis settings
ANALYSIS_TIMEOUT=300s
MAX_FILE_SIZE=1MB
```

### Kafka Configuration
```bash
# Input topics
KAFKA_COMMIT_ANALYSIS_TOPIC=commit-analysis
KAFKA_PR_ANALYSIS_TOPIC=pull-request-analysis

# Output topics  
KAFKA_ANALYSIS_RESULTS_TOPIC=code-analysis-results
```

## 🤖 AI Analysis Features

### Security Analysis
- SQL injection detection
- XSS vulnerability scanning
- Authentication bypass patterns
- Sensitive data exposure

### Quality Analysis
- Code complexity metrics
- Maintainability index
- Test coverage analysis
- Documentation quality

### Performance Analysis
- Memory leak detection
- Performance anti-patterns
- Resource usage optimization
- Async/await best practices

## 📊 Analysis Workflow

1. **Receive Request** - Via Kafka from Git Processor
2. **Clone Repository** - Fetch code for analysis
3. **AI Processing** - Send to Ollama for analysis
4. **Result Aggregation** - Combine multiple analysis results
5. **Report Generation** - Create structured analysis report
6. **Notification** - Send results back via Kafka

## 🧪 Testing

### Manual Testing
```bash
# Test analysis endpoint
curl -X POST http://localhost:8082/api/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "repositoryUrl": "https://github.com/example/repo",
    "commitId": "abc123",
    "analysisType": "SECURITY"
  }'
```

### Performance Testing
```bash
# Load test with multiple requests
ab -n 100 -c 10 http://localhost:8082/actuator/health
```

## 📈 Monitoring & Metrics

- **Analysis Queue Size** - Pending analysis requests
- **Processing Time** - Average analysis duration
- **Success Rate** - Percentage of successful analyses
- **AI Model Response Time** - Ollama response metrics

## 🔧 Development

### Adding New Analysis Types
1. Create analysis strategy in `analyzer/` package
2. Implement `AnalysisStrategy` interface
3. Register strategy in `AnalysisEngine`
4. Add configuration properties
5. Write tests and update documentation

### Custom AI Prompts
```java
@Component
public class SecurityAnalysisStrategy implements AnalysisStrategy {
    
    private final String SECURITY_PROMPT = """
        Analyze this code for security vulnerabilities:
        1. SQL injection risks
        2. XSS vulnerabilities  
        3. Authentication issues
        4. Data exposure risks
        
        Code: {code}
        """;
}