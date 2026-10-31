#!/usr/bin/env bash
# Lint the documentation: markdownlint on the clean-path list (fails), a report on every other Markdown file
# (never fails), and a render check of every Mermaid block.
# Needs: node 22 or newer, npm. Versions are pinned. Set DOCS_LINT_SKIP_MERMAID=1 to skip the render check.
set -euo pipefail
cd "$(dirname "$0")/../.."

NODE_MAJOR="$(node -p 'process.versions.node.split(".")[0]')"
if (( NODE_MAJOR < 22 )); then
  echo "Node 22 or newer is required (found $(node -v))." >&2
  exit 1
fi

MARKDOWNLINT_VERSION="0.24.0"
MERMAID_CLI_VERSION="12.0.0"
BASELINE=273 # issues in all tracked Markdown on 2026-10-31 under .markdownlint-cli2.yaml; do not raise

mapfile -t CLEAN < <(grep -v -e '^#' -e '^$' scripts/ci/docs-lint-paths.txt)

echo "==> markdownlint (must be clean): ${#CLEAN[@]} files"
npx --yes "markdownlint-cli2@${MARKDOWNLINT_VERSION}" "${CLEAN[@]}"

echo "==> markdownlint (report only): all tracked Markdown"
mapfile -t ALL < <(git ls-files '*.md')
REPORT="$(npx --yes "markdownlint-cli2@${MARKDOWNLINT_VERSION}" "${ALL[@]}" 2>&1 || true)"
echo "$REPORT" | grep 'Summary:' || echo "Summary: 0 issues"
TOTAL="$(echo "$REPORT" | sed -n 's/^Summary: \([0-9]*\) issues.*/\1/p')"
if [[ -n "$TOTAL" && "$TOTAL" -gt "$BASELINE" ]]; then
  echo "::warning::Markdown issues (${TOTAL}) are above the baseline (${BASELINE})."
fi

if [[ "${DOCS_LINT_SKIP_MERMAID:-0}" == "1" ]]; then
  echo "==> mermaid: skipped"
  exit 0
fi
echo "==> mermaid render check"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
echo '{"args":["--no-sandbox"]}' > "$TMP/puppeteer.json"
mapfile -t WITH_MERMAID < <(git ls-files '*.md' | xargs grep -l '^```mermaid' || true)
for f in "${WITH_MERMAID[@]}"; do
  [[ -z "$f" ]] && continue
  echo "    $f"
  npx --yes -p "@mermaid-js/mermaid-cli@${MERMAID_CLI_VERSION}" mmdc \
    -p "$TMP/puppeteer.json" -i "$f" -o "$TMP/$(echo "$f" | tr / _)" >/dev/null
done
