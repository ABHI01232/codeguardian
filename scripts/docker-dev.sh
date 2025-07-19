#!/bin/bash

# CodeGuardian Development Docker Script
# Usage: ./scripts/docker-dev.sh [up|down|rebuild|logs|clean]

set -e

case "$1" in
  "up")
    echo "🚀 Starting CodeGuardian in development mode..."
    echo "📦 This will build latest changes and start all services"
    
    # Build latest changes first
    echo "🔧 Building latest code..."
    mvn clean package -DskipTests
    
    # Start infrastructure first
    echo "🏗️ Starting infrastructure services..."
    docker-compose up -d postgres redis zookeeper kafka
    
    # Wait for infrastructure to be ready
    echo "⏳ Waiting for infrastructure to be ready..."
    sleep 15
    
    # Start application services with rebuild
    echo "🚀 Starting application services..."
    docker-compose up -d --build api-gateway git-processor code-analyzer frontend
    
    echo "✅ CodeGuardian development environment is starting up!"
    echo "🌐 Frontend: http://localhost:3000"
    echo "🔌 API Gateway: http://localhost:8080"
    echo "📊 To view logs: ./scripts/docker-dev.sh logs"
    ;;
    
  "down")
    echo "🛑 Stopping CodeGuardian development environment..."
    docker-compose down
    echo "✅ All services stopped"
    ;;
    
  "rebuild")
    echo "🔄 Rebuilding CodeGuardian with latest changes..."
    
    # Build latest code
    echo "🔧 Building latest code..."
    mvn clean package -DskipTests
    
    # Rebuild and restart services
    echo "🏗️ Rebuilding Docker images..."
    docker-compose build --no-cache api-gateway git-processor code-analyzer frontend
    
    echo "🚀 Restarting services..."
    docker-compose up -d api-gateway git-processor code-analyzer frontend
    
    echo "✅ Rebuild complete!"
    ;;
    
  "logs")
    echo "📋 Showing CodeGuardian logs (Ctrl+C to exit)..."
    docker-compose logs -f
    ;;
    
  "clean")
    echo "🧹 Cleaning up CodeGuardian environment..."
    echo "⚠️  This will remove all containers, volumes, and images"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
      docker-compose down -v --remove-orphans
      docker system prune -f
      docker volume prune -f
      echo "✅ Cleanup complete!"
    else
      echo "❌ Cleanup cancelled"
    fi
    ;;
    
  *)
    echo "CodeGuardian Docker Development Helper"
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  up       - Build latest changes and start all services"
    echo "  down     - Stop all services"
    echo "  rebuild  - Rebuild images with latest changes and restart"
    echo "  logs     - Show logs from all services"
    echo "  clean    - Remove all containers, volumes, and images"
    echo ""
    echo "Examples:"
    echo "  $0 up      # Start development environment"
    echo "  $0 rebuild # Apply latest code changes"
    echo "  $0 logs    # Monitor application logs"
    ;;
esac