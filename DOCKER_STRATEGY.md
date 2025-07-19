# CodeGuardian Docker Strategy

## Overview

CodeGuardian uses a sophisticated Docker strategy that supports both development and production workflows with automatic builds, health checks, and easy management scripts.

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │    │  Git Processor  │
│   (React/Nginx) │    │  (Spring Boot)  │    │  (Spring Boot)  │
│   Port: 3000    │    │   Port: 8080    │    │   Port: 8081    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Code Analyzer   │    │   PostgreSQL    │    │      Redis      │
│ (Spring Boot)   │    │   Database      │    │     Cache       │
│  Port: 8082     │    │   Port: 5432    │    │   Port: 6379    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
         ┌─────────────────┐    ┌─────────────────┐
         │    Kafka        │    │   Zookeeper     │
         │  Port: 9092     │    │   Port: 2181    │
         └─────────────────┘    └─────────────────┘
```

## Docker Compose Files

### 1. `docker-compose.yml` (Base Configuration)
- **Purpose**: Main production configuration
- **Contains**: All service definitions, networks, volumes
- **Environment**: Production-ready settings

### 2. `docker-compose.override.yml` (Development Overrides)
- **Purpose**: Development-specific configurations
- **Features**: 
  - Auto-rebuild on changes
  - Volume mounts for hot reloading
  - Health checks for proper startup order
  - Development environment variables
- **Auto-loaded**: Automatically used by docker-compose in development

### 3. `docker-compose.dev.yml` (Infrastructure Only)
- **Purpose**: Start only infrastructure services
- **Use case**: When developing services locally outside Docker

## Management Scripts

### Development Script: `./scripts/docker-dev.sh`

```bash
# Start development environment with latest changes
./scripts/docker-dev.sh up

# Rebuild and apply latest code changes
./scripts/docker-dev.sh rebuild

# View logs from all services
./scripts/docker-dev.sh logs

# Stop all services
./scripts/docker-dev.sh down

# Complete cleanup (removes containers, volumes, images)
./scripts/docker-dev.sh clean
```

**Features:**
- ✅ **Auto-build**: Automatically builds Maven artifacts before starting
- ✅ **Smart startup**: Starts infrastructure first, then applications
- ✅ **Health checks**: Waits for services to be ready
- ✅ **Hot reload**: Supports code changes without full restart

### Production Script: `./scripts/docker-prod.sh`

```bash
# Deploy to production
./scripts/docker-prod.sh deploy

# Update production with latest changes
./scripts/docker-prod.sh update

# Check system health and status
./scripts/docker-prod.sh status

# Create backup
./scripts/docker-prod.sh backup

# Restore from backup
./scripts/docker-prod.sh restore 20241219_143022
```

**Features:**
- ✅ **Automatic backups**: Creates backups before deployments
- ✅ **Health monitoring**: Comprehensive health checks
- ✅ **Rolling updates**: Zero-downtime deployments
- ✅ **Backup/Restore**: Database and configuration management

## Build Strategy

### 1. Multi-Stage Builds (Current)
```dockerfile
# Each service builds from source in container
FROM openjdk:21-jdk-slim
RUN apt-get update && apt-get install -y maven
COPY . .
RUN mvn clean package -DskipTests -pl service-name -am
CMD ["java", "-jar", "target/service.jar"]
```

**Pros:**
- ✅ Consistent build environment
- ✅ No local dependencies needed
- ✅ Reproducible builds

**Cons:**
- ❌ Slower builds (downloads dependencies each time)
- ❌ Larger images during build

### 2. Pre-Built Artifacts (Recommended for CI/CD)
```dockerfile
# Build artifacts locally/CI, copy to container
FROM openjdk:21-jre-slim
COPY target/service.jar app.jar
CMD ["java", "-jar", "app.jar"]
```

**Pros:**
- ✅ Faster container builds
- ✅ Smaller final images
- ✅ Better for CI/CD pipelines

## Development Workflow

### Quick Start
```bash
# Clone and start development environment
git clone <repository>
cd CodeGuardian
./scripts/docker-dev.sh up
```

### Making Changes
```bash
# After making code changes
./scripts/docker-dev.sh rebuild

# Or for infrastructure-only changes
docker-compose restart api-gateway
```

### Debugging
```bash
# View specific service logs
docker-compose logs -f api-gateway

# Access container shell
docker-compose exec api-gateway bash

# View all logs
./scripts/docker-dev.sh logs
```

## Production Deployment

### Initial Deployment
```bash
./scripts/docker-prod.sh deploy
```

### Updating Production
```bash
# Update with latest changes
./scripts/docker-prod.sh update

# Check health after update
./scripts/docker-prod.sh status
```

### Monitoring
```bash
# Health check
./scripts/docker-prod.sh status

# Container resource usage
docker stats

# System resource usage
docker system df
```

## Environment Configuration

### Development Environment Variables
```yaml
# Automatically set in docker-compose.override.yml
SPRING_PROFILES_ACTIVE: docker
JWT_SECRET: development-secret
DB_USERNAME: codeguardian
DB_PASSWORD: codeguardian123
```

### Production Environment Variables
```yaml
# Set in docker-compose.yml
SPRING_PROFILES_ACTIVE: docker
JWT_SECRET: ${JWT_SECRET:-secure-production-secret}
DB_USERNAME: ${DB_USERNAME:-codeguardian}
DB_PASSWORD: ${DB_PASSWORD:-secure-password}
```

## Volume Management

### Persistent Volumes
- **postgres_data**: Database data persistence
- **git_repos**: Git repository storage

### Development Volumes
- **Source code mounts**: Enable hot reloading
- **Configuration mounts**: Override settings

## Network Configuration

### Internal Network: `codeguardian-network`
- **Type**: Bridge network
- **Purpose**: Service-to-service communication
- **Isolation**: Services only accessible within network

### Port Mapping
- **3000**: Frontend (React/Nginx)
- **8080**: API Gateway
- **8081**: Git Processor
- **8082**: Code Analyzer
- **5432**: PostgreSQL (for external access)
- **6379**: Redis (for external access)
- **9092**: Kafka (for external access)

## Best Practices

### ✅ Do's
- Use `./scripts/docker-dev.sh up` for development
- Run `./scripts/docker-prod.sh status` to monitor production
- Create backups before major updates
- Use `docker-compose.override.yml` for local customizations
- Monitor logs regularly with `./scripts/docker-dev.sh logs`

### ❌ Don'ts
- Don't modify `docker-compose.yml` for local development
- Don't skip backups in production
- Don't run production commands in development
- Don't commit sensitive environment variables

## Troubleshooting

### Common Issues

#### Services Won't Start
```bash
# Check service dependencies
docker-compose ps

# View startup logs
docker-compose logs service-name

# Restart specific service
docker-compose restart service-name
```

#### Database Connection Issues
```bash
# Check database health
docker-compose exec postgres pg_isready -U codeguardian

# View database logs
docker-compose logs postgres

# Reset database
docker-compose down -v && docker-compose up -d postgres
```

#### Build Failures
```bash
# Clean build
./scripts/docker-dev.sh clean
./scripts/docker-dev.sh up

# Manual build with no cache
docker-compose build --no-cache service-name
```

#### Port Conflicts
```bash
# Check port usage
lsof -i :8080

# Stop conflicting services
./scripts/docker-dev.sh down
```

## CI/CD Integration

### Example GitHub Actions
```yaml
name: Deploy
on:
  push:
    branches: [main]
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Deploy to Production
        run: ./scripts/docker-prod.sh deploy
```

### Example Jenkins Pipeline
```groovy
pipeline {
    agent any
    stages {
        stage('Deploy') {
            steps {
                sh './scripts/docker-prod.sh update'
                sh './scripts/docker-prod.sh status'
            }
        }
    }
}
```

## Performance Optimization

### Build Optimization
- Use Docker BuildKit for faster builds
- Implement multi-stage builds with dependency caching
- Use `.dockerignore` to exclude unnecessary files

### Runtime Optimization
- Set appropriate JVM heap sizes
- Use health checks for proper load balancer integration
- Implement resource limits in production

### Monitoring
- Prometheus metrics available at `/actuator/prometheus`
- Health checks at `/actuator/health`
- Custom metrics for business logic

This strategy provides a robust, scalable, and maintainable Docker setup that supports both development velocity and production reliability.