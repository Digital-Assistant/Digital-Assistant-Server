#!/usr/bin/env bash
# Dependency scan placeholder. The OWASP Dependency-Check scan needs the NVD vulnerability
# database, which is not used for now (see docs/TODO.md in the UDAN workspace).
# This script only says so and exits 0. Put the real scan here when one is chosen.
set -euo pipefail
cd "$(dirname "$0")/../.."

echo "==> Dependency scan skipped: no scanner is set up for the Server yet."
