#!/bin/bash

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

PROJECT_1="$SCRIPT_DIR/shared"
PROJECT_2="$SCRIPT_DIR/api"
PROJECT_3="$SCRIPT_DIR/worker"
PROJECT_4="$SCRIPT_DIR/admin"
DOCKER_DIR="$SCRIPT_DIR/deploy"

echo "SCRIPT_DIR: [$SCRIPT_DIR]"
echo "PROJECT_1: [$PROJECT_1]"
echo "PROJECT_2: [$PROJECT_2]"
echo "PROJECT_3: [$PROJECT_3]"
build_project() {
  local dir="$1"
  echo "=============================="
  echo "Building project in: $dir"
  echo "=============================="
  cd "$dir"
  mvn clean install -DskipTests
  echo "✅ Build completed for: $dir"
}
build_project "$PROJECT_1"
build_project "$PROJECT_2"
build_project "$PROJECT_3"
build_project "$PROJECT_4"

echo "🚀 Starting Docker containers from: $DOCKER_DIR"
cd "$DOCKER_DIR"
docker compose build --no-cache
docker compose up -d