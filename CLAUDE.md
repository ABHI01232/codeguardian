# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Build & Compilation
```bash
# Compile the backend
cd codeguardian-backend && mvn clean compile

# Full build with tests
mvn clean install
```

### Running the Application
```bash
# Start backend with Kafka & Redis infrastructure
./start-with-kafka-redis.sh

# Alternative: Simple backend-only startup  
./start-simple.sh

# Frontend development server
cd frontend && npm run dev

# Frontend build
cd frontend && npm run build
```

### Testing
```bash
# Run all backend tests
cd codeguardian-backend && mvn test

# Frontend tests (if applicable)
cd frontend && npm test
```

### Docker Operations
```bash
# Start infrastructure services only
docker-compose -f docker-compose-simple.yml up -d

# Stop infrastructure services
docker-compose -f docker-compose-simple.yml down
```

## Architecture Overview

CodeGuardian is a **real-time security analysis platform** that has evolved from a complex microservices architecture to a **simplified monolithic approach** for easier development and deployment.

### Current System State
- **Backend**: Single Spring Boot application in `codeguardian-backend/` 
- **Frontend**: React 18 application with WebSocket support
- **Infrastructure**: Kafka, Redis, H2 database (can be embedded or external)
- **Original Design**: Multiple microservices (api-gateway, git-processor, code-analyzer) - now consolidated

### Key Technologies
- **Backend**: Java 21, Spring Boot 3.2.0, Spring Cloud, Spring AI, Maven
- **Frontend**: React 18, Vite, TailwindCSS, Chart.js, STOMP WebSocket client
- **Messaging**: Apache Kafka with topics `analysis-requests` and `analysis-results`
- **Database**: H2 embedded database with JPA entities
- **Caching**: Redis for session management and performance
- **API Documentation**: Swagger/OpenAPI at `/swagger-ui.html`

### Core Components

#### Backend Structure (`/codeguardian-backend/`)
- `config/`: KafkaConfig, WebSocketConfig, CORS, Swagger configuration
- `controller/`: REST API controllers for repositories, analyses, dashboard
- `entity/`: JPA entities (RepositoryEntity, AnalysisEntity) 
- `service/`: Business logic including SecurityAnalysisService with pattern-based vulnerability detection
- `repository/`: Spring Data JPA repositories

#### Frontend Structure (`/frontend/`)  
- `components/`: React components (Dashboard, RepositoryDetailsPage, RealTimeUpdates)
- `services/`: API client and WebSocket service integration

### Data Flow Patterns

1. **Repository Management**: Frontend → REST API → JPA Repository → H2 Database
2. **Security Analysis**: Trigger → Kafka Producer → Analysis Service → Kafka Consumer → WebSocket Notification → Frontend
3. **Real-time Updates**: Backend Analysis → WebSocket STOMP → Frontend Dashboard Updates

### Security Analysis Engine

The `SecurityAnalysisService` implements pattern-based detection for:
- **JavaScript/Node.js**: XSS, hardcoded secrets, weak crypto, unsafe eval
- **Java**: SQL injection, insecure random, debug code  
- **Python**: Unsafe eval, weak hashing, hardcoded credentials

Severity levels: CRITICAL, HIGH, MEDIUM, LOW with CWE mapping and risk scoring.

### WebSocket Integration
- **Protocol**: STOMP over SockJS
- **Endpoint**: `/ws` 
- **Topics**: `/topic/notifications`
- **Use Case**: Real-time dashboard updates and security alerts

### Service URLs (Development)
- **Main API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html  
- **H2 Console**: http://localhost:8080/h2-console
- **Frontend**: http://localhost:3000
- **Kafka**: localhost:9092
- **Redis**: localhost:6379

### Important Implementation Notes

- The system uses **embedded H2 database** by default, stored in `./data/codeguardian`
- Kafka configuration includes error handling with `DefaultErrorHandler` and retry mechanisms
- WebSocket configuration supports CORS and real-time bidirectional communication
- Frontend uses proxy configuration to route API calls to backend during development
- Build system supports both embedded and external infrastructure deployment

### Development Workflow

1. Start infrastructure services if using external components
2. Compile and start the backend Spring Boot application  
3. Start the React frontend development server
4. Use Swagger UI for API testing and development
5. Monitor real-time updates through WebSocket connections in the frontend dashboard