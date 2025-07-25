#!/bin/bash

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DOCKER_DIR="$SCRIPT_DIR/deploy"

echo "🛑 Stopping Docker containers from: $DOCKER_DIR"
cd "$DOCKER_DIR"
docker compose down