#!/usr/bin/env bash
# Scan the dependencies for known vulnerabilities (OWASP Dependency-Check).
# Optional: export NVD_API_KEY in your shell for faster database downloads.
# The key is never stored in this repo.
set -euo pipefail
cd "$(dirname "$0")/../.."

echo "==> Scanning dependencies (OWASP Dependency-Check)"
./gradlew dependencyCheckAnalyze
echo "==> Done. Report is in build/reports/"
