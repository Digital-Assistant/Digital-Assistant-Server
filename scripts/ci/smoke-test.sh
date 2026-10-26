#!/usr/bin/env bash
# Post-deploy sanity check of the Server, from outside, through nginx. Unauthenticated calls only:
# no token, no secret, read-only. Runs after every TEST deploy (deploy-test.yml, image.yml) and works
# on a laptop too. A first step; deeper QA (authenticated, agentic) can follow as later jobs.
#   scripts/ci/smoke-test.sh <base-url>        e.g. https://<test host>  (no trailing /api)
# Checks:
#   1. GET /api/search/links (public) -> 200 and a JSON array: nginx, Server and Elasticsearch answer.
#   2. GET /api/search/all without a token -> 401 with WWW-Authenticate: Bearer (the Server's OIDC).
#   3. The same with a malformed bearer token and realm UDAN -> 401.
# Optional env: SMOKE_ATTEMPTS (default 3), SMOKE_WAIT (seconds between attempts, default 5).
# Needs: curl.
set -euo pipefail

base="${1:?usage: smoke-test.sh <base-url>}"
base="${base%/}"
[[ "${base}" =~ ^https?://[A-Za-z0-9.-]+(:[0-9]+)?$ ]] || { echo "base-url must be http(s)://host[:port], got ${base}" >&2; exit 2; }
ATTEMPTS="${SMOKE_ATTEMPTS:-3}"
WAIT="${SMOKE_WAIT:-5}"

tmp="$(mktemp -d)"
trap 'rm -rf "${tmp}"' EXIT
failures=0

# check <name> <expected status> <header regex or -> <body regex or -> <curl args...>
# Retries a few times so a short nginx hiccup right after the container swap is not a failure.
check() {
  local name="$1" want="$2" header_re="$3" body_re="$4"
  shift 4
  local attempt code why
  for ((attempt = 1; attempt <= ATTEMPTS; attempt++)); do
    code="$(curl -sS -m 15 -o "${tmp}/body" -D "${tmp}/headers" -w '%{http_code}' "$@" 2>"${tmp}/err" || true)"
    why=""
    [[ "${code}" == "${want}" ]] || why="status ${code:-none}, want ${want}"
    [[ -z "${why}" && "${header_re}" != "-" ]] && ! grep -qiE "${header_re}" "${tmp}/headers" && why="header /${header_re}/ missing"
    [[ -z "${why}" && "${body_re}" != "-" ]] && ! grep -qE "${body_re}" "${tmp}/body" && why="body does not match /${body_re}/"
    if [[ -z "${why}" ]]; then
      echo "PASS ${name}"
      return 0
    fi
    [[ "${attempt}" -lt "${ATTEMPTS}" ]] && sleep "${WAIT}"
  done
  echo "FAIL ${name}: ${why}$( [[ -s "${tmp}/err" ]] && echo " ($(head -c 200 "${tmp}/err"))")"
  echo "     body: $(head -c 200 "${tmp}/body" 2>/dev/null | tr -d '\n')"
  failures=$((failures + 1))
}

echo "Sanity check of the Server at ${base}"
check "public search answers (nginx, Server, Elasticsearch)" 200 '^content-type: application/json' '^\[' \
  "${base}/api/search/links?query=sanitycheck&domain=sanity-check.invalid&size=1"
check "protected endpoint without token is refused" 401 '^www-authenticate: Bearer' - \
  "${base}/api/search/all?domain=sanity-check.invalid&userSessionId=sanitycheck"
check "malformed token is refused" 401 - - \
  -H "UDAN-Realm: UDAN" -H "Authorization: Bearer sanity.check.invalid" \
  "${base}/api/search/all?domain=sanity-check.invalid&userSessionId=sanitycheck"

if [[ "${failures}" -gt 0 ]]; then
  echo "Sanity check FAILED: ${failures} check(s). TEST may be broken; roll back with Deploy to TEST and the previous digest." >&2
  exit 1
fi
echo "Sanity check passed"
