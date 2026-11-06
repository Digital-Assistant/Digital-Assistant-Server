#!/usr/bin/env bash
# No-new-violations check. Compares today's counts with the committed quality baseline
# (docs/agentic-readiness/digital-assistant-server-quality-baseline.json) and fails when a count went UP
# or the coverage floor went DOWN. A baseline only goes down, a floor only goes up; raising or lowering
# either needs the owner (AGENTS.md, "Baselines and floors"). The script never rewrites the baseline file.
#
# Usage:
#   scripts/ci/check-baseline.sh            offline checks (run by `./gradlew formatCheck`, so by CI `build.sh`):
#                                           PMD exclude list, Spotless full-tree count, skip markers, coverage floor
#   scripts/ci/check-baseline.sh osv <advisories> <packages>
#                                           compare the OSV-Scanner count (called by scan.sh in the CI scan job)
# Set BASELINE_SKIP_SPOTLESS=1 to skip the Spotless full-tree count (about 10 s) on a quick local run; CI never skips it.
# Needs: bash, python3, grep, git. The Spotless count also needs Java 21 and the Gradle wrapper.
set -euo pipefail
cd "$(dirname "$0")/../.."

BASELINE_FILE="docs/agentic-readiness/digital-assistant-server-quality-baseline.json"
[[ -f "$BASELINE_FILE" ]] || { echo "::error::$BASELINE_FILE not found" >&2; exit 2; }

# base <key> <field>: read one number from the baseline file.
base() {
  python3 -I - "$BASELINE_FILE" "$1" "$2" <<'PY'
import json, sys
print(json.load(open(sys.argv[1]))["entries"][sys.argv[2]][sys.argv[3]])
PY
}

FAIL=0
# compare <name> <today> <baseline>: fail when the count went up, ask for the file to be lowered when it went down.
compare() {
  local name="$1" today="$2" baseline="$3"
  if (( today > baseline )); then
    echo "::error::baseline: ${name} is ${today}, committed baseline is ${baseline}. A count only goes down; fix the new violations (the owner approves any raise)." >&2
    FAIL=1
  elif (( today < baseline )); then
    echo "baseline: ${name} is ${today}, committed baseline is ${baseline}. Lower the count in ${BASELINE_FILE} in this change."
  else
    echo "baseline: ${name} = ${today} (ok)"
  fi
}

if [[ "${1:-}" == "osv" ]]; then
  [[ $# -eq 3 ]] || { echo "usage: $0 osv <advisories> <packages>" >&2; exit 2; }
  compare "OSV advisories" "$2" "$(base osv_advisories count)"
  compare "OSV packages with advisories" "$3" "$(base osv_advisories packages)"
  exit "$FAIL"
fi

# 1. PMD: the exclude list in the ruleset is the lint baseline; it only shrinks.
PMD_EXCLUDES="$(grep -c '<exclude-pattern>' config/pmd/ruleset.xml || true)"
compare "PMD exclude patterns" "$PMD_EXCLUDES" "$(base pmd_exclude_patterns count)"

# 2. Skip markers in the tests (skipped or conditionally run tests).
SKIPS="$({ grep -rEc '@Disabled|@Ignore|@EnabledIf|@DisabledIf|@EnabledOn|@DisabledOn|@EnabledFor|assumeTrue|assumeFalse' src/test || true; } | awk -F: '{s+=$NF} END {print s+0}')"
compare "test skip markers" "$SKIPS" "$(base skip_markers count)"

# 3. Coverage floor: build.gradle must not hold a lower floor than the baseline file.
FLOOR="$(sed -nE 's/^[[:space:]]*minimum = 0\.([0-9]{2}).*/\1/p' build.gradle | head -n 1)"
[[ -n "$FLOOR" ]] || { echo "::error::no coverage floor ('minimum = 0.NN') found in build.gradle" >&2; exit 2; }
FLOOR=$((10#$FLOOR))
FLOOR_BASE="$(base coverage_floor_line_percent count)"
if (( FLOOR < FLOOR_BASE )); then
  echo "::error::baseline: coverage floor in build.gradle is ${FLOOR}%, committed floor is ${FLOOR_BASE}%. A floor only goes up (the owner approves any lowering)." >&2
  FAIL=1
elif (( FLOOR > FLOOR_BASE )); then
  echo "baseline: coverage floor in build.gradle is ${FLOOR}%, committed floor is ${FLOOR_BASE}%. Raise the count in ${BASELINE_FILE} in this change."
else
  echo "baseline: coverage floor = ${FLOOR}% (ok)"
fi

# 4. Spotless full-tree count: files that a full (non-ratchet) format run would change. Counted in a scratch copy so
#    nothing in the working tree is touched.
if [[ "${BASELINE_SKIP_SPOTLESS:-0}" == "1" ]]; then
  echo "baseline: Spotless full-tree count skipped (BASELINE_SKIP_SPOTLESS=1)"
else
  TMP="$(mktemp -d)"
  trap 'rm -rf "$TMP"' EXIT
  mkdir "$TMP/tree"
  for f in build.gradle settings.gradle gradle.properties gradle.lockfile gradlew; do
    [[ -e "$f" ]] && cp "$f" "$TMP/tree/"
  done
  cp -r gradle config src "$TMP/tree/"
  cp -r src "$TMP/before"
  ( cd "$TMP/tree" && ./gradlew spotlessApply -PspotlessFull --no-daemon -q >"$TMP/spotless.log" 2>&1 ) || {
    cat "$TMP/spotless.log" >&2
    echo "::error::Spotless full-tree run failed" >&2
    exit 2
  }
  UNFORMATTED="$(diff -rq "$TMP/before" "$TMP/tree/src" | grep -c '^Files ' || true)"
  compare "Spotless unformatted files (full run)" "$UNFORMATTED" "$(base spotless_unformatted_files count)"
fi

exit "$FAIL"
