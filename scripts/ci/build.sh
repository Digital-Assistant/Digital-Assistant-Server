#!/usr/bin/env bash
# Build the Server jar. Tests are skipped here; test.sh runs them.
set -euo pipefail
cd "$(dirname "$0")/../.."

echo "==> Building the Server (Gradle, tests skipped)"
./gradlew clean build -x test
echo "==> Done. Output is in build/quarkus-app/"
