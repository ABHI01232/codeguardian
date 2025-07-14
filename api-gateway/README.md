# API Gateway Service

Central entry point for the CodeGuardian platform, handling authentication, routing, and rate limiting.

## 🎯 Purpose

- **Authentication & Authorization** - JWT-based security
- **Request Routing** - Route requests to appropriate microservices
- **Rate Limiting** - Redis-based request throttling
- **CORS Management** - Cross-origin request handling
- **Load Balancing** - Distribute requests across service instances

## 🚀 Quick Start

### Run Standalone
```bash
cd api-gateway
mvn spring-boot:run
```

### Run with Docker
```bash
docker build -t codeguardian/api-gateway .
docker run -p 8080:8080 codeguardian/api-gateway
```

## ⚙️ Configuration

### Environment Variables
```bash
# JWT Configuration
JWT_SECRET=your-256-bit-secret-key

# Redis Configuration  
REDIS_HOST=localhost
REDIS_PORT=6379

# Service URLs
GIT_PROCESSOR_URL=http://localhost:8081
CODE_ANALYZER_URL=http://localhost:8082
```

### Application Profiles
- `default` - Local development
- `docker` - Docker environment
- `production` - Production settings

## 🛡️ Security Features

- **JWT Authentication** - Stateless token-based auth
- **Rate Limiting** - 100 requests/minute per IP
- **CORS Protection** - Configurable origin policies
- **Request Validation** - Input sanitization

## 📡 API Endpoints

### Public Endpoints
- `POST /auth/login` - User authentication
- `GET /health` - Service health check

### Protected Endpoints
- `GET /api/git/**` - Git processor routes
- `GET /api/analyze/**` - Code analyzer routes

## 🧪 Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify -Pintegration-tests

# Load testing
curl -X GET http://localhost:8080/health
```

## 📊 Monitoring

- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

## 🔧 Development

### Prerequisites
- Java 21
- Redis (for rate limiting)
- Maven

### Local Setup
```bash
# Start Redis
docker run -d -p 6379:6379 redis:alpine

# Run application
mvn spring-boot:run -Dspring.profiles.active=local
```

### Code Style
- Follow Google Java Style Guide
- Use Lombok for boilerplate reduction
- Write tests for all endpoints

## 🐛 Troubleshooting

### Common Issues

1. **JWT token invalid**
   ```bash
   # Check JWT secret configuration
   echo $JWT_SECRET
   ```

2. **Redis connection failed**
   ```bash
   # Verify Redis is running
   redis-cli ping
   ```

3. **Service routing errors**
   ```bash
   # Check service discovery
   curl http://localhost:8081/actuator/health
   ```
```