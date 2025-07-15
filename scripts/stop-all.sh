#!/bin/bash

# Stop all CodeGuardian services
echo "Stopping CodeGuardian Services..."

# Stop all services
docker-compose down

# Remove volumes if requested
if [ "$1" == "--clean" ]; then
    echo "Cleaning up volumes..."
    docker-compose down -v
    docker system prune -f
fi

echo "All services stopped successfully!"