# CodeGuardian Docker Management
# Simple commands for common Docker operations

.PHONY: help dev-up dev-down dev-rebuild dev-logs dev-clean prod-deploy prod-update prod-status prod-backup

# Default target
help:
	@echo "CodeGuardian Docker Management"
	@echo "============================="
	@echo "Development Commands:"
	@echo "  make dev-up       - Start development environment"
	@echo "  make dev-down     - Stop development environment"
	@echo "  make dev-rebuild  - Rebuild with latest changes"
	@echo "  make dev-logs     - Show development logs"
	@echo "  make dev-clean    - Clean up development environment"
	@echo ""
	@echo "Production Commands:"
	@echo "  make prod-deploy  - Deploy to production"
	@echo "  make prod-update  - Update production"
	@echo "  make prod-status  - Check production status"
	@echo "  make prod-backup  - Create production backup"
	@echo ""
	@echo "Quick Commands:"
	@echo "  make up           - Alias for dev-up"
	@echo "  make down         - Alias for dev-down"
	@echo "  make logs         - Alias for dev-logs"

# Development aliases
up: dev-up
down: dev-down
logs: dev-logs

# Development commands
dev-up:
	@echo "🚀 Starting CodeGuardian development environment..."
	@./scripts/docker-dev.sh up

dev-down:
	@echo "🛑 Stopping CodeGuardian development environment..."
	@./scripts/docker-dev.sh down

dev-rebuild:
	@echo "🔄 Rebuilding CodeGuardian with latest changes..."
	@./scripts/docker-dev.sh rebuild

dev-logs:
	@echo "📋 Showing CodeGuardian development logs..."
	@./scripts/docker-dev.sh logs

dev-clean:
	@echo "🧹 Cleaning up CodeGuardian development environment..."
	@./scripts/docker-dev.sh clean

# Production commands
prod-deploy:
	@echo "🚀 Deploying CodeGuardian to production..."
	@./scripts/docker-prod.sh deploy

prod-update:
	@echo "🔄 Updating CodeGuardian production..."
	@./scripts/docker-prod.sh update

prod-status:
	@echo "📊 Checking CodeGuardian production status..."
	@./scripts/docker-prod.sh status

prod-backup:
	@echo "💾 Creating CodeGuardian production backup..."
	@./scripts/docker-prod.sh backup

# Infrastructure only (useful for local development)
infra-up:
	@echo "🏗️ Starting infrastructure services only..."
	@docker-compose -f docker-compose.dev.yml up -d

infra-down:
	@echo "🛑 Stopping infrastructure services..."
	@docker-compose -f docker-compose.dev.yml down

# Quick development workflow
quick-restart:
	@echo "⚡ Quick restart of application services..."
	@docker-compose restart api-gateway git-processor code-analyzer frontend

# Build only (no restart)
build:
	@echo "🔧 Building latest code..."
	@mvn clean package -DskipTests

# Database operations
db-reset:
	@echo "🗄️ Resetting database..."
	@docker-compose down postgres
	@docker volume rm codeguardian_postgres_data || true
	@docker-compose up -d postgres

# System information
info:
	@echo "📊 CodeGuardian System Information"
	@echo "================================"
	@echo "Docker version:"
	@docker --version
	@echo ""
	@echo "Docker Compose version:"
	@docker-compose --version
	@echo ""
	@echo "Running containers:"
	@docker ps --filter "name=codeguardian" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
	@echo ""
	@echo "System resources:"
	@docker system df