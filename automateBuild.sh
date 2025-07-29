#!/bin/bash

set -e  # Exit on error

APP_NAME="fhir-mapper"
JAR_NAME="fhir.jar"
PORT=8083

echo "🔄 Cleaning old builds..."
./gradlew clean

echo "🔨 Building project without tests..."
./gradlew build -x test --no-daemon

echo "🐳 Building Docker image..."
docker-compose build

echo "🚀 Starting Docker container..."
docker-compose up -d

echo "✅ Container is running on http://localhost:${PORT}"
echo "📜 Tailing logs (Ctrl+C to stop)..."
docker-compose logs -f $APP_NAME
