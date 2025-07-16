# 🚀 CodeGuardian - Running the Project

## Prerequisites
- **Docker & Docker Compose** (required)
- **Java 21** (if running individual services)
- **Node.js 18+** (if running frontend separately)
- **Maven 3.8+** (if building from source)

## Quick Start (Recommended)

### 1. Clone & Navigate
```bash
cd "/Users/abhishekdutt/Desktop/Java Interview/Banking AI project/CodeGuardian"
```

### 2. Start All Services
```bash
# Start the complete system
docker-compose -f docker-compose.final.yml up -d

# Check all services are running
docker ps
```

### 3. Access the Application
- **Frontend Dashboard**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Git Processor**: http://localhost:8081
- **Code Analyzer**: http://localhost:8082

### 4. Verify Everything is Working
```bash
# Check service health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

## Service Status Verification

### Check All Containers
```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

### Expected Output:
```
NAMES                        STATUS                    PORTS
codeguardian-frontend        Up                        0.0.0.0:3000->80/tcp
codeguardian-code-analyzer   Up                        0.0.0.0:8082->8082/tcp
codeguardian-git-processor   Up                        0.0.0.0:8081->8081/tcp
codeguardian-api-gateway     Up                        0.0.0.0:8080->8080/tcp
codeguardian-kafka           Up (healthy)              0.0.0.0:9092->9092/tcp
codeguardian-postgres        Up (healthy)              0.0.0.0:5432->5432/tcp
codeguardian-redis           Up (healthy)              0.0.0.0:6379->6379/tcp
codeguardian-zookeeper       Up (healthy)              2181/tcp, 2888/tcp, 3888/tcp
```

## Alternative Running Methods

### Option 1: Infrastructure Only
```bash
# Start only databases and message queues
docker-compose -f docker-compose.simple.yml up -d

# Then run services individually with IDE or:
cd api-gateway && mvn spring-boot:run -Dspring-boot.run.profiles=docker
cd git-processor && mvn spring-boot:run -Dspring-boot.run.profiles=docker
cd code-analyzer && mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

### Option 2: Development Mode
```bash
# Start infrastructure
docker-compose -f docker-compose.dev.yml up -d

# Run frontend in dev mode
cd frontend && npm install && npm run dev
```

## Stopping the System
```bash
# Stop all services
docker-compose -f docker-compose.final.yml down

# Remove volumes (optional - resets database)
docker-compose -f docker-compose.final.yml down -v
```

## Troubleshooting

### If containers fail to start:
```bash
# Check logs
docker logs codeguardian-api-gateway
docker logs codeguardian-git-processor
docker logs codeguardian-code-analyzer

# Rebuild if needed
docker-compose -f docker-compose.final.yml build --no-cache
```

### If frontend shows old version after code changes:
```bash
# Stop frontend container
docker-compose -f docker-compose.final.yml stop frontend

# Rebuild frontend with new code (no cache)
docker-compose -f docker-compose.final.yml build --no-cache frontend

# Start frontend container
docker-compose -f docker-compose.final.yml up -d frontend
```

> **Note**: Frontend changes require container rebuild since the React app is built into the Docker image during build time.

### If ports are already in use:
```bash
# Check what's using the ports
lsof -i :3000  # Frontend
lsof -i :8080  # API Gateway
lsof -i :8081  # Git Processor
lsof -i :8082  # Code Analyzer
```

## Available Endpoints

### Frontend Dashboard
- **Main**: http://localhost:3000

### API Gateway Routes
- **Health**: http://localhost:8080/actuator/health
- **Git API**: http://localhost:8080/api/git/*
- **Analysis API**: http://localhost:8080/api/analyze/*

### Webhook Endpoints
- **GitHub**: http://localhost:8080/api/git/webhook/github
- **GitLab**: http://localhost:8080/api/git/webhook/gitlab

### Direct Service Access
- **Git Processor**: http://localhost:8081/actuator/health
- **Code Analyzer**: http://localhost:8082/actuator/health

## Demo Features

1. **Dashboard**: Security metrics, vulnerability tracking
2. **Repository Management**: Add/monitor repositories via modal
3. **Real-time Analysis**: Simulated security scanning with detailed results
4. **Webhook Integration**: GitHub/GitLab webhook processing
5. **Health Monitoring**: Service status indicators
6. **Navigation**: Full React Router implementation with detailed views
7. **Analysis Details**: Comprehensive vulnerability reports with filtering

## System Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │    │  Git Processor  │
│   (React)       │────│   (Spring)      │────│   (Spring)      │
│   Port: 3000    │    │   Port: 8080    │    │   Port: 8081    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                │
                       ┌─────────────────┐
                       │  Code Analyzer  │
                       │   (Spring)      │
                       │   Port: 8082    │
                       └─────────────────┘
                                │
                    ┌───────────┼───────────┐
                    │           │           │
            ┌───────────┐ ┌───────────┐ ┌───────────┐
            │PostgreSQL │ │   Redis   │ │   Kafka   │
            │Port: 5432 │ │Port: 6379 │ │Port: 9092 │
            └───────────┘ └───────────┘ └───────────┘
```

## Next Steps

1. **Test the dashboard**: Visit http://localhost:3000
2. **Check service health**: Verify all APIs are responding
3. **Add repositories**: Use the "Add Repository" button in the header
4. **Navigate pages**: Click "View Details" buttons to explore repository and analysis pages
5. **Test filtering**: Use search and filter features on analysis results
6. **Set up webhooks**: Configure GitHub/GitLab integrations
7. **Monitor analysis**: Watch real-time security scanning

---

🎯 **The system is now ready for demonstration and further development!**