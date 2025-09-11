#!/bin/bash

set -e  # Exit on error

APP_NAME="fhir-mapper"
JAR_NAME="fhir.jar"
PORT=8083
COMPOSE_FILE="docker-compose-fhir.yaml"

echo "🔄 Cleaning old builds..."
chmod +x ./gradlew
./gradlew clean

echo "🔨 Building project without tests..."
./gradlew build -x test --no-daemon

echo "🐳 Building Docker image..."
docker compose -f $COMPOSE_FILE build

echo "🚀 Starting Docker container..."
docker compose -f $COMPOSE_FILE up -d --build

echo "✅ Container is running on http://localhost:${PORT}"
echo "📜 Tailing logs (Ctrl+C to stop)..."
docker compose -f $COMPOSE_FILE logs -f $APP_NAME
