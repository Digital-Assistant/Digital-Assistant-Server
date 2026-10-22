#!/usr/bin/env bash
# Build the Docker image. Does not push anything.
# The Dockerfile compiles the code itself, so build.sh is not needed first.
# Optional: IMAGE_NAME (default udan-server), IMAGE_TAG (default local).
set -euo pipefail
cd "$(dirname "$0")/../.."

name="${IMAGE_NAME:-udan-server}"
tag="${IMAGE_TAG:-local}"

echo "==> Building Docker image ${name}:${tag}"
docker build -f src/main/docker/Dockerfile.jvm -t "${name}:${tag}" .
echo "==> Built ${name}:${tag} (not pushed)"
