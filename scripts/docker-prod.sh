#!/bin/bash

# CodeGuardian Production Docker Script
# Usage: ./scripts/docker-prod.sh [deploy|stop|update|status|backup|restore]

set -e

BACKUP_DIR="./backups"
BACKUP_DATE=$(date +%Y%m%d_%H%M%S)

case "$1" in
  "deploy")
    echo "🚀 Deploying CodeGuardian to production..."
    
    # Create backup before deployment
    if [ -d "$BACKUP_DIR" ]; then
      echo "💾 Creating backup before deployment..."
      ./scripts/docker-prod.sh backup
    fi
    
    # Build optimized production images
    echo "🔧 Building production images..."
    mvn clean package -DskipTests -Dmaven.test.skip=true
    docker-compose -f docker-compose.yml build --no-cache
    
    # Deploy with production settings
    echo "🏗️ Deploying to production..."
    docker-compose -f docker-compose.yml up -d
    
    # Health check
    echo "🔍 Performing health checks..."
    sleep 30
    ./scripts/docker-prod.sh status
    
    echo "✅ Production deployment complete!"
    echo "🌐 Application: http://localhost:3000"
    ;;
    
  "stop")
    echo "🛑 Stopping production environment..."
    docker-compose -f docker-compose.yml down
    echo "✅ Production services stopped"
    ;;
    
  "update")
    echo "🔄 Updating production deployment..."
    
    # Backup current state
    echo "💾 Creating backup..."
    ./scripts/docker-prod.sh backup
    
    # Build latest changes
    echo "🔧 Building latest changes..."
    mvn clean package -DskipTests
    
    # Rolling update strategy
    echo "🚀 Performing rolling update..."
    docker-compose -f docker-compose.yml build
    docker-compose -f docker-compose.yml up -d --force-recreate
    
    echo "✅ Production update complete!"
    ;;
    
  "status")
    echo "📊 CodeGuardian Production Status"
    echo "=================================="
    
    # Container status
    echo "🐳 Container Status:"
    docker-compose -f docker-compose.yml ps
    
    echo ""
    echo "🔍 Health Checks:"
    
    # API Gateway health
    if curl -f -s http://localhost:8080/actuator/health > /dev/null; then
      echo "✅ API Gateway: Healthy"
    else
      echo "❌ API Gateway: Unhealthy"
    fi
    
    # Database connectivity
    if docker-compose -f docker-compose.yml exec -T postgres pg_isready -U codeguardian > /dev/null 2>&1; then
      echo "✅ Database: Connected"
    else
      echo "❌ Database: Connection failed"
    fi
    
    # Frontend
    if curl -f -s http://localhost:3000 > /dev/null; then
      echo "✅ Frontend: Accessible"
    else
      echo "❌ Frontend: Inaccessible"
    fi
    
    echo ""
    echo "💾 Storage Usage:"
    docker system df
    ;;
    
  "backup")
    echo "💾 Creating backup..."
    mkdir -p "$BACKUP_DIR"
    
    # Database backup
    echo "🗄️ Backing up database..."
    docker-compose -f docker-compose.yml exec -T postgres pg_dump -U codeguardian codeguardian > "$BACKUP_DIR/database_$BACKUP_DATE.sql"
    
    # Configuration backup
    echo "⚙️ Backing up configuration..."
    tar -czf "$BACKUP_DIR/config_$BACKUP_DATE.tar.gz" docker-compose.yml api-gateway/src/main/resources/application*.yml
    
    echo "✅ Backup created: $BACKUP_DIR/database_$BACKUP_DATE.sql"
    echo "✅ Config backup: $BACKUP_DIR/config_$BACKUP_DATE.tar.gz"
    ;;
    
  "restore")
    if [ -z "$2" ]; then
      echo "❌ Please specify backup date"
      echo "Usage: $0 restore YYYYMMDD_HHMMSS"
      echo "Available backups:"
      ls -la "$BACKUP_DIR"/ | grep database_ || echo "No backups found"
      exit 1
    fi
    
    RESTORE_DATE="$2"
    BACKUP_FILE="$BACKUP_DIR/database_$RESTORE_DATE.sql"
    
    if [ ! -f "$BACKUP_FILE" ]; then
      echo "❌ Backup file not found: $BACKUP_FILE"
      exit 1
    fi
    
    echo "🔄 Restoring from backup: $RESTORE_DATE"
    echo "⚠️  This will overwrite current data!"
    read -p "Are you sure? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
      docker-compose -f docker-compose.yml exec -T postgres psql -U codeguardian -c "DROP DATABASE IF EXISTS codeguardian;"
      docker-compose -f docker-compose.yml exec -T postgres psql -U codeguardian -c "CREATE DATABASE codeguardian;"
      docker-compose -f docker-compose.yml exec -T postgres psql -U codeguardian -d codeguardian < "$BACKUP_FILE"
      echo "✅ Database restored successfully"
    else
      echo "❌ Restore cancelled"
    fi
    ;;
    
  *)
    echo "CodeGuardian Production Management"
    echo "=================================="
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  deploy   - Deploy to production with health checks"
    echo "  stop     - Stop production services"
    echo "  update   - Update production with rolling deployment"
    echo "  status   - Check system health and status"
    echo "  backup   - Create database and config backup"
    echo "  restore  - Restore from backup (specify date)"
    echo ""
    echo "Examples:"
    echo "  $0 deploy           # Initial production deployment"
    echo "  $0 update           # Update with latest changes"
    echo "  $0 status           # Check health status"
    echo "  $0 backup           # Create backup"
    echo "  $0 restore 20241219_143022  # Restore from specific backup"
    ;;
esac