# 🌐 API Gateway Service

**Central entry point for the CodeGuardian platform, handling authentication, routing, and rate limiting.**

## 🎯 Purpose

The API Gateway serves as the single entry point for all external requests, providing:

- **Authentication & Authorization** - JWT-based security
- **Request Routing** - Route requests to appropriate microservices
- **Rate Limiting** - Redis-based request throttling
- **CORS Management** - Cross-origin request handling
- **Load Balancing** - Distribute requests across service instances

## 🏗️ Architecture

```
External Requests → API Gateway → Internal Services
                        ↓
                   ┌─────────────┐
                   │ JWT Auth    │
                   │ Rate Limit  │
                   │ CORS        │
                   └─────────────┘
                        ↓
         ┌──────────────┼──────────────┐
         ▼              ▼              ▼
   Git Processor   Code Analyzer   Other Services
     (Port 8081)    (Port 8082)
```

## 🚀 Quick Start

### Option 1: Development Mode
```bash
cd api-gateway
./scripts/start-dev.sh
```

### Option 2: Maven
```bash
cd api-gateway
mvn spring-boot:run
```

### Option 3: Docker
```bash
cd api-gateway
docker build -t codeguardian/api-gateway .
docker run -p 8080:8080 codeguardian/api-gateway
```

## ⚙️ Configuration

### Environment Variables
```bash
# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-for-jwt-token-generation

# Redis Configuration  
REDIS_HOST=localhost
REDIS_PORT=6379

# Service URLs (automatically configured in Docker)
GIT_PROCESSOR_URL=http://git-processor:8081
CODE_ANALYZER_URL=http://code-analyzer:8082
```

### Application Profiles
- `default` - Local development
- `docker` - Docker environment
- `production` - Production settings

## 🛡️ Security Features

### JWT Authentication
- Stateless token-based authentication
- Configurable token expiration
- Secure token signing

### Rate Limiting
- 100 requests/minute per IP (configurable)
- Redis-backed rate limiting
- Burst protection

### CORS Protection
- Configurable origin policies
- Preflight request handling
- Credential support

## 📡 API Endpoints

### Public Endpoints
```bash
GET  /actuator/health         # Service health check
GET  /actuator/prometheus     # Prometheus metrics
```

### Routed Endpoints
```bash
# Git Processor Routes
POST /api/git/webhook/github        # GitHub webhook
POST /api/git/webhook/gitlab        # GitLab webhook
GET  /api/git/repositories          # List repositories
GET  /api/git/repositories/{id}     # Get repository details

# Code Analyzer Routes
POST /api/analyze/commit             # Analyze commit
POST /api/analyze/pull-request       # Analyze pull request
GET  /api/analyze/results/{id}       # Get analysis results
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

### Load Testing
```bash
# Basic health check
curl -X GET http://localhost:8080/actuator/health

# Test rate limiting
for i in {1..200}; do curl -X GET http://localhost:8080/actuator/health; done
```

## 📊 Monitoring

### Health Checks
```bash
# Service health
curl http://localhost:8080/actuator/health

# Detailed health with components
curl http://localhost:8080/actuator/health/detailed
```

### Metrics
```bash
# Application metrics
curl http://localhost:8080/actuator/metrics

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus
```

## 🔧 Development

### Prerequisites
```bash
# Required
- Java 17+
- Maven 3.6+
- Redis (for rate limiting)

# Optional
- Docker & Docker Compose
```

### Local Setup
```bash
# 1. Start Redis
docker run -d -p 6379:6379 redis:alpine

# 2. Set environment variables
export JWT_SECRET=your-256-bit-secret-key
export REDIS_HOST=localhost
export REDIS_PORT=6379

# 3. Run application
mvn spring-boot:run -Dspring.profiles.active=local
```

### Code Style
- Follow Google Java Style Guide
- Use Lombok for boilerplate reduction
- Write tests for all endpoints
- Document all public APIs

## 🐛 Troubleshooting

### Common Issues

**1. JWT token invalid**
```bash
# Check JWT secret configuration
echo $JWT_SECRET

# Verify token format
curl -H "Authorization: Bearer your-token" http://localhost:8080/actuator/health
```

**2. Redis connection failed**
```bash
# Verify Redis is running
redis-cli ping

# Check Redis logs
docker logs redis-container
```

**3. Service routing errors**
```bash
# Check downstream services
curl http://localhost:8081/actuator/health  # Git Processor
curl http://localhost:8082/actuator/health  # Code Analyzer

# Check gateway routes
curl http://localhost:8080/actuator/gateway/routes
```

**4. Rate limiting issues**
```bash
# Check rate limit status
curl -I http://localhost:8080/actuator/health

# Reset rate limits (Redis)
redis-cli FLUSHALL
```

## 🔄 Request Flow

```
1. Client Request → API Gateway
2. CORS Check → Allow/Deny
3. JWT Validation → Authentication
4. Rate Limit Check → Throttle if needed
5. Route Resolution → Target service
6. Request Forwarding → Downstream service
7. Response Processing → Client
```

## 📁 Project Structure

```
api-gateway/
├── src/
│   ├── main/
│   │   ├── java/com/codeguardian/
│   │   │   ├── gateway/
│   │   │   │   └── ApiGatewayApplication.java
│   │   │   ├── config/
│   │   │   │   ├── GatewayConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   └── filter/
│   │   │       ├── JwtAuthenticationFilter.java
│   │   │       └── RateLimitingFilter.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
├── scripts/
│   └── start-dev.sh
├── Dockerfile
├── pom.xml
└── README.md
```

## 🚀 Deployment

### Docker Compose
```yaml
# Already configured in root docker-compose.yml
api-gateway:
  build: ./api-gateway
  ports:
    - "8080:8080"
  depends_on:
    - redis
  environment:
    - JWT_SECRET=your-secret
```

### Kubernetes
```yaml
# Example Kubernetes deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: codeguardian/api-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: token
```

## 🔗 Related Services

- **[Git Processor](../git-processor/README.md)** - Webhook processing service
- **[Code Analyzer](../code-analyzer/README.md)** - Security analysis service
- **[Root Documentation](../README.md)** - Complete system overview

---

**For more information, see the [main project README](../README.md)**