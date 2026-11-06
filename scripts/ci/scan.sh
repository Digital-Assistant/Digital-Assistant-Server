#!/usr/bin/env bash
# Dependency scan: OSV-Scanner over the committed gradle.lockfile. This is the `security` verb
# (`./gradlew security` calls this script, and so does the `Dependency Scan (report-only)` job).
# Downloads a pinned OSV-Scanner release and checks its SHA-256 before running it (same pattern as secrets.sh).
# Needs: curl, sha256sum, python3 (to count findings), network access to osv.dev.
#
# Mode (env SCAN_MODE):
#   report  (default) prints the finding count, writes it to the job summary and exits 0 on findings.
#           The existing findings are recorded debt; CI shows this job as report-only.
#   block   exits 1 when there is any finding.
# In both modes the count is compared with docs/agentic-readiness/digital-assistant-server-quality-baseline.json
# (scripts/ci/check-baseline.sh osv): the script fails when the count goes up, never when it goes down.
# A scanner failure (network, parse, no lockfile) always fails the script: it is never a silent pass.
set -euo pipefail
cd "$(dirname "$0")/../.."

VERSION="2.7.0"
SHA256="160c6253e2922e9e0a6a9771c5c4c40a4b3c6090b34a3cdd5e80c96cc2e4218b"
MODE="${SCAN_MODE:-report}"
LOCKFILE="gradle.lockfile"

[[ -f "$LOCKFILE" ]] || { echo "::error::$LOCKFILE not found; regenerate it with ./gradlew dependencies --write-locks" >&2; exit 2; }

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
curl -fsSL -o "$TMP/osv-scanner" \
  "https://github.com/google/osv-scanner/releases/download/v${VERSION}/osv-scanner_linux_amd64"
echo "${SHA256}  $TMP/osv-scanner" | sha256sum -c -
chmod +x "$TMP/osv-scanner"

echo "==> OSV-Scanner ${VERSION} over ${LOCKFILE} (mode: ${MODE})"
# Exit codes: 0 no findings, 1 findings, anything else is a scanner error.
rc=0
"$TMP/osv-scanner" scan source --lockfile="gradle.lockfile:${LOCKFILE}" --format json --output-file "$TMP/osv.json" || rc=$?
if (( rc != 0 && rc != 1 )); then
  echo "::error::OSV-Scanner failed with exit code ${rc}" >&2
  exit "$rc"
fi

COUNTS="$(python3 -I - "$TMP/osv.json" <<'PY'
import json, sys
data = json.load(open(sys.argv[1]))
ids, pkgs = set(), set()
for res in data.get("results", []):
    for pkg in res.get("packages", []):
        vulns = pkg.get("vulnerabilities", [])
        if vulns:
            pkgs.add((pkg["package"]["name"], pkg["package"]["version"]))
        ids.update(v["id"] for v in vulns)
print(len(ids), len(pkgs))
PY
)"
read -r ADVISORIES PACKAGES <<<"$COUNTS"
echo "==> ${ADVISORIES} advisories in ${PACKAGES} packages"
"$TMP/osv-scanner" scan source --lockfile="gradle.lockfile:${LOCKFILE}" --format table 2>/dev/null | grep -E '^\| https' || true

if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  {
    echo "### Dependency scan (OSV-Scanner ${VERSION}, ${LOCKFILE})"
    echo
    echo "- Mode: **${MODE}**"
    echo "- Findings: **${ADVISORIES}** advisories in **${PACKAGES}** packages"
    [[ "$MODE" == "report" ]] && echo "- Report-only: existing findings are recorded debt and do not block the merge. A rise in this number is a regression."
  } >> "$GITHUB_STEP_SUMMARY"
fi

# No new advisories: the count may fall but never rise above the committed quality baseline.
scripts/ci/check-baseline.sh osv "$ADVISORIES" "$PACKAGES"

if [[ "$MODE" == "block" && "$ADVISORIES" != "0" ]]; then
  echo "::error::${ADVISORIES} known advisories in ${LOCKFILE}" >&2
  exit 1
fi
