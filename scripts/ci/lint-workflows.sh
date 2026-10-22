#!/usr/bin/env bash
# Lint the CI workflow with actionlint and zizmor.
# Checks ci.yml, release.yml and pr-title.yml.
# Needs: curl, sha256sum, and pip (for zizmor).
set -euo pipefail
cd "$(dirname "$0")/../.."

ACTIONLINT_VERSION="1.7.12"
ACTIONLINT_SHA256="8aca8db96f1b94770f1b0d72b6dddcb1ebb8123cb3712530b08cc387b349a3d8"
ZIZMOR_VERSION="1.31.1"
FILES=(.github/workflows/ci.yml .github/workflows/release.yml .github/workflows/pr-title.yml)

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
curl -fsSL -o "$TMP/actionlint.tar.gz" \
  "https://github.com/rhysd/actionlint/releases/download/v${ACTIONLINT_VERSION}/actionlint_${ACTIONLINT_VERSION}_linux_amd64.tar.gz"
echo "${ACTIONLINT_SHA256}  $TMP/actionlint.tar.gz" | sha256sum -c -
tar -xzf "$TMP/actionlint.tar.gz" -C "$TMP" actionlint

echo "==> actionlint"
"$TMP/actionlint" "${FILES[@]}"

echo "==> zizmor"
python3 -m venv "$TMP/venv"
"$TMP/venv/bin/pip" install --quiet "zizmor==${ZIZMOR_VERSION}"
# Offline: no GitHub token is passed, so online-only audits are skipped.
"$TMP/venv/bin/zizmor" --offline "${FILES[@]}"
