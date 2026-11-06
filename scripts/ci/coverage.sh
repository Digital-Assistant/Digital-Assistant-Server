#!/usr/bin/env bash
# Enforce the line-coverage floor on the last test run (jacocoTestCoverageVerification in build.gradle).
# Run it after test.sh in the same job: it reads build/jacoco-quarkus.exec and does not run the tests again.
# Locally, `./gradlew coverage` runs the tests (needs the local stack) and then does the same.
# The floor only goes up. Raising or lowering it needs the owner (AGENTS.md, "Baselines and floors").
set -euo pipefail
cd "$(dirname "$0")/../.."

[[ -f build/jacoco-quarkus.exec ]] || { echo "::error::build/jacoco-quarkus.exec not found: run scripts/ci/test.sh first" >&2; exit 2; }
echo "==> Coverage report and floor (line coverage)"
./gradlew jacocoTestReport jacocoTestCoverageVerification -x test
