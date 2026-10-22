#!/usr/bin/env bash
# Build the Docker image. Does not push anything.
# The Dockerfile compiles the code itself, so build.sh is not needed first.
# Optional: IMAGE_NAME (default udan-server), IMAGE_TAG (default local).
# CI mode: set IMAGE_TAR to write the image to that tar file instead of the local Docker
# store, and CACHE_DIR to reuse build layers between runs (uses a buildx builder).
set -euo pipefail
cd "$(dirname "$0")/../.."

name="${IMAGE_NAME:-udan-server}"
tag="${IMAGE_TAG:-local}"

if [[ -z "${IMAGE_TAR:-}" ]]; then
  echo "==> Building Docker image ${name}:${tag}"
  docker build -f src/main/docker/Dockerfile.jvm -t "${name}:${tag}" .
  echo "==> Built ${name}:${tag} (not pushed)"
  exit 0
fi

# The default Docker builder cannot export a layer cache; a docker-container builder can.
docker buildx inspect udan-ci >/dev/null 2>&1 || docker buildx create --name udan-ci --driver docker-container >/dev/null
cache_args=()
if [[ -n "${CACHE_DIR:-}" ]]; then
  mkdir -p "${CACHE_DIR}"
  cache_args=(--cache-from "type=local,src=${CACHE_DIR}" --cache-to "type=local,dest=${CACHE_DIR}.new,mode=max")
fi

echo "==> Building ${name}:${tag} into ${IMAGE_TAR}"
docker buildx build --builder udan-ci -f src/main/docker/Dockerfile.jvm \
  -t "${name}:${tag}" --output "type=docker,dest=${IMAGE_TAR}" "${cache_args[@]}" .

# Swap the cache so it does not grow with every run.
if [[ -n "${CACHE_DIR:-}" && -d "${CACHE_DIR}.new" ]]; then
  rm -rf "${CACHE_DIR}"
  mv "${CACHE_DIR}.new" "${CACHE_DIR}"
fi
echo "==> Built ${IMAGE_TAR} (not pushed)"
