#!/usr/bin/env bash
# Scan a range of commits for secrets with gitleaks.
# Usage: secrets.sh <base-sha> <head-sha>
# Scans only the commits in base..head, so old history (which has local-dev values) is not flagged.
# Downloads a pinned gitleaks release and checks its SHA-256 before running it.
set -euo pipefail
cd "$(dirname "$0")/../.."

BASE="${1:?base sha}"
HEAD="${2:?head sha}"
VERSION="8.30.1"
SHA256="551f6fc83ea457d62a0d98237cbad105af8d557003051f41f3e7ca7b3f2470eb"

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
curl -fsSL -o "$TMP/gitleaks.tar.gz" \
  "https://github.com/gitleaks/gitleaks/releases/download/v${VERSION}/gitleaks_${VERSION}_linux_x64.tar.gz"
echo "${SHA256}  $TMP/gitleaks.tar.gz" | sha256sum -c -
tar -xzf "$TMP/gitleaks.tar.gz" -C "$TMP" gitleaks

echo "==> gitleaks over ${BASE}..${HEAD}"
"$TMP/gitleaks" git --redact --no-banner --log-opts "${BASE}..${HEAD}" .
