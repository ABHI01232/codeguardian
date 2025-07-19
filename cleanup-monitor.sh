#!/bin/bash

# CodeGuardian Mock Data Cleanup Monitor
# This script continuously monitors and removes any mock repositories

echo "🔥 Starting CodeGuardian Cleanup Monitor..."
echo "📍 Monitoring API: http://localhost:8080/api/repositories"
echo "🎯 Target mock repositories: secure-banking-app, payment-service"
echo ""

# Function to clean mock repositories
cleanup_mock_repos() {
    local repos=$(curl -s http://localhost:8080/api/repositories)
    local count=$(echo "$repos" | jq length)
    
    if [ "$count" -gt 0 ]; then
        echo "🚨 $(date): Found $count repositories - checking for mock data..."
        
        # Check each repository and delete if it's mock data
        for id in $(echo "$repos" | jq -r '.[].id'); do
            local name=$(echo "$repos" | jq -r ".[] | select(.id == $id) | .name")
            
            if [[ "$name" == "secure-banking-app" || "$name" == "payment-service" || "$name" == "mssc-beer-service" ]]; then
                echo "🚮 Deleting mock repository: $name (ID: $id)"
                curl -X DELETE -s "http://localhost:8080/api/repositories/$id"
            else
                echo "✅ Keeping legitimate repository: $name (ID: $id)"
            fi
        done
    else
        echo "✅ $(date): Database clean - no repositories found"
    fi
}

# Function to clean mock analyses
cleanup_mock_analyses() {
    local analyses=$(curl -s http://localhost:8080/api/analyses)
    local count=$(echo "$analyses" | jq length)
    
    if [ "$count" -gt 0 ]; then
        echo "🔍 $(date): Found $count analyses - checking for mock data..."
        
        for analysis_id in $(echo "$analyses" | jq -r '.[].id'); do
            local repo_name=$(echo "$analyses" | jq -r ".[] | select(.id == \"$analysis_id\") | .repository // .repositoryName // \"unknown\"")
            
            if [[ "$repo_name" == "secure-banking-app" || "$repo_name" == "payment-service" || "$repo_name" == "mssc-beer-service" ]]; then
                echo "🚮 Deleting mock analysis: $analysis_id (repo: $repo_name)"
                curl -X DELETE -s "http://localhost:8080/api/analyses/$analysis_id"
            fi
        done
    fi
}

# Main monitoring loop
while true; do
    # Check if API is available
    if curl -s --connect-timeout 5 http://localhost:8080/api/repositories > /dev/null 2>&1; then
        cleanup_mock_repos
        cleanup_mock_analyses
    else
        echo "⚠️ $(date): API not available, waiting..."
    fi
    
    # Wait 5 seconds before next check
    sleep 5
done