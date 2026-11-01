#!/usr/bin/env bash
# Build the Server jar. Tests are skipped here; test.sh runs them.
# build.sh + test.sh together equal `./gradlew check` (formatCheck, PMD and typecheck run inside `build`).
set -euo pipefail
cd "$(dirname "$0")/../.."

echo "==> Building the Server (Gradle, tests skipped)"
./gradlew clean build -x test
echo "==> Done. Output is in build/quarkus-app/"
