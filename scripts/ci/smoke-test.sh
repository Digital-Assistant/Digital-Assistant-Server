#!/usr/bin/env bash
# Post-deploy sanity check of the Server, from outside, through nginx. Unauthenticated calls only:
# no token, no secret, read-only. Runs after every TEST deploy (deploy-test.yml, image.yml) and works
# on a laptop too. A first step; deeper QA (authenticated, agentic) can follow as later jobs.
#   scripts/ci/smoke-test.sh <base-url>        e.g. https://<test host>  (no trailing /api)
# Checks:
#   1. GET /api/user/getsessionkey (public) -> 200 and a session key: nginx and the Server answer.
#   2. GET /api/search/all without a token -> 401 with WWW-Authenticate: Bearer (the Server's OIDC).
#   3. The same with a malformed bearer token and realm UDAN -> 401.
#   4. GET /api/search/links without a token -> 401 (every /api/ path needs a token since PK02).
#   5. Browser origins: GET /api/user/getsessionkey with an Origin header (https://example.com and the
#      extension's chrome-extension:// origin) must answer 200, and an OPTIONS preflight on an API path
#      must answer 200/204 with Access-Control-Allow-Origin. Browsers always send Origin and checks 1-4
#      do not; on 2026-10-30/31 PROD answered 403 "CORS Rejected - Invalid origin" to every request
#      with an Origin for ~12 h (its config lacked the quarkus.http.cors.* keys) and nothing noticed.
#      A 403 is reported as "CORS rejected". Read-only, no credential.
# Optional env: SMOKE_ATTEMPTS (default 3), SMOKE_WAIT (seconds between attempts, default 5),
# SMOKE_EXTENSION_ID (32 chars a-p; default the published extension id, TEST pins
# lcnkfnijlogkgifbfhkcnmndhabnloco).
# Needs: curl.
set -euo pipefail

base="${1:?usage: smoke-test.sh <base-url>}"
base="${base%/}"
[[ "${base}" =~ ^https?://[A-Za-z0-9.-]+(:[0-9]+)?$ ]] || { echo "base-url must be http(s)://host[:port], got ${base}" >&2; exit 2; }
ATTEMPTS="${SMOKE_ATTEMPTS:-3}"
WAIT="${SMOKE_WAIT:-5}"
EXT_ID="${SMOKE_EXTENSION_ID:-jejdickefnhgkhdcjjcmoiiljlkanpln}"
[[ "${EXT_ID}" =~ ^[a-p]{32}$ ]] || { echo "SMOKE_EXTENSION_ID must be 32 chars a-p, got ${EXT_ID}" >&2; exit 2; }

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
    [[ "${code}" =~ ^(${want})$ ]] || why="status ${code:-none}, want ${want}"
    [[ "${code}" == "403" && "${want}" != "403" ]] && why="CORS rejected (403), want ${want}"
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
check "public session key answers (nginx, Server)" 200 - '^[A-Za-z0-9]{20}$' \
  -H "UDAN-Realm: UDAN" \
  "${base}/api/user/getsessionkey"
check "protected endpoint without token is refused" 401 '^www-authenticate: Bearer' - \
  -H "UDAN-Realm: UDAN" \
  "${base}/api/search/all?domain=sanity-check.invalid&userSessionId=sanitycheck"
check "malformed token is refused" 401 - - \
  -H "UDAN-Realm: UDAN" -H "Authorization: Bearer sanity.check.invalid" \
  "${base}/api/search/all?domain=sanity-check.invalid&userSessionId=sanitycheck"
check "former public search needs a token" 401 - - \
  -H "UDAN-Realm: UDAN" \
  "${base}/api/search/links?query=sanitycheck&domain=sanity-check.invalid&size=1"
for origin in "https://example.com" "chrome-extension://${EXT_ID}"; do
  check "session key with Origin ${origin%%://*} is not refused" 200 - - \
    -H "UDAN-Realm: UDAN" -H "Origin: ${origin}" \
    "${base}/api/user/getsessionkey"
  check "preflight with Origin ${origin%%://*} is allowed" '200|204' '^access-control-allow-origin: ' - \
    -X OPTIONS -H "Origin: ${origin}" -H "Access-Control-Request-Method: GET" \
    -H "Access-Control-Request-Headers: content-type,authorization,udan-realm" \
    "${base}/api/user/getsessionkey"
done

if [[ "${failures}" -gt 0 ]]; then
  echo "Sanity check FAILED: ${failures} check(s). TEST may be broken; roll back with Deploy to TEST and the previous digest." >&2
  exit 1
fi
echo "Sanity check passed"
