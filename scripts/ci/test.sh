#!/usr/bin/env bash
# Run the Server tests.
# The tests need MariaDB and Elasticsearch reachable on 127.0.0.1 (see README.md).
# The Flyway baseline settings below are needed when the database already has tables
# (as in the local stack). Override them by exporting the variables before you run.
set -euo pipefail
cd "$(dirname "$0")/../.."

export QUARKUS_FLYWAY_BASELINE_ON_MIGRATE="${QUARKUS_FLYWAY_BASELINE_ON_MIGRATE:-true}"
export QUARKUS_FLYWAY_BASELINE_VERSION="${QUARKUS_FLYWAY_BASELINE_VERSION:-0}"

echo "==> Running Server tests (Flyway baseline-on-migrate=${QUARKUS_FLYWAY_BASELINE_ON_MIGRATE})"
./gradlew test
