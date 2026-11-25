#!/bin/bash

# ==============================================================================
# SYNC-CREDENTIALS.SH
# ==============================================================================
# DESCRIPTION:
#   Writes local-secrets.properties (untracked) from the dev credentials of the
#   local infrastructure in the udan-workspace checkout.
#
# DEPENDENCIES:
#   1. A udan-workspace checkout (https://github.com/Digital-Assistant/udan-workspace,
#      branch dev, then ./udan bootstrap). Its path is $UDAN_WORKSPACE, default ".."
#      (the parent directory of this repository). A relative $UDAN_WORKSPACE is
#      resolved against the directory you run the script from.
#   2. In that checkout:
#      - infra/.env (created by scripts/infra-env.sh); infra/.env.example is used
#        when .env is missing (dev defaults, valid only on fresh data volumes);
#      - infra/keycloak/realms/udan-realm.json (backend-service client secret).
#   3. local-secrets.properties.example in src/main/resources/ (the template).
#   4. jq, for reading the backend-service client secret from the realm file.
#
# USAGE:
#   ./sync-credentials.sh            write src/main/resources/local-secrets.properties
#   ./sync-credentials.sh --dry-run  show which keys would be set (no values), write nothing
#   UDAN_WORKSPACE=/path/to/udan-workspace ./sync-credentials.sh
#
# EXIT CODES:
#   0 - All keys written (or, with --dry-run, all values found).
#   1 - A value is missing or the file could not be written; nothing is written then.
#   2 - Unknown option.
# ==============================================================================

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

DRY_RUN=0
case "${1:-}" in
    "") ;;
    --dry-run) DRY_RUN=1 ;;
    -h|--help) sed -n '3,30p' "$0"; exit 0 ;;
    *) echo -e "${RED}❌ Unknown option: $1 (use --dry-run or --help)${NC}"; exit 2 ;;
esac

# A relative UDAN_WORKSPACE means "relative to where I am", so resolve it before the cd.
WS="${UDAN_WORKSPACE:-}"
case "$WS" in
    "" | /*) ;;
    *) WS="$PWD/$WS" ;;
esac

# Run from the repository root, so relative paths work from any directory.
cd "$(dirname "${BASH_SOURCE[0]}")" || exit 1

WS="${WS:-..}"
INFRA_DIR="$WS/infra"
PROP_FILE="src/main/resources/local-secrets.properties"
TEMPLATE_FILE="src/main/resources/local-secrets.properties.example"

# The temp file holds secrets: it lives outside the source tree and is removed on every exit path.
TMP_FILE=""
# shellcheck disable=SC2329 # called by the EXIT trap
cleanup() { if [ -n "$TMP_FILE" ]; then rm -f "$TMP_FILE"; fi; }
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM HUP

# Keys and values to write, collected first and written in one pass at the end.
KEYS=()
VALS=()
MISSING=0

# read_env NAME FILE: prints the value of NAME from a dotenv file. The file is never sourced.
# Supported subset of the Docker Compose .env format (enough for infra/.env):
#   - NAME=value lines; leading whitespace and an "export " prefix are allowed;
#   - the last line for NAME wins; CR line endings are ignored;
#   - 'single quotes': the text up to the closing quote, taken literally;
#   - "double quotes": the text up to the closing quote; \" and \\ give " and \;
#   - unquoted: an inline comment (whitespace, then #) is cut off; outer whitespace is trimmed.
# Not supported: ${VAR} interpolation, other backslash escapes in double quotes, multi-line values.
# A line with an unterminated quote is ignored.
read_env() {
    NAME="$1" awk '
        BEGIN { n = ENVIRON["NAME"]; q = sprintf("%c", 39); dq = "\""; found = 0 }
        {
            line = $0
            sub(/\r$/, "", line)
            sub(/^[ \t]+/, "", line)
            sub(/^export[ \t]+/, "", line)
            if (index(line, n "=") != 1) next
            v = substr(line, length(n) + 2)
            sub(/^[ \t]+/, "", v)
            first = substr(v, 1, 1)
            if (first == q) {
                rest = substr(v, 2); e = index(rest, q)
                if (e == 0) next
                v = substr(rest, 1, e - 1)
            } else if (first == dq) {
                out = ""; closed = 0
                for (i = 2; i <= length(v); i++) {
                    c = substr(v, i, 1)
                    if (c == "\\" && i < length(v) && (substr(v, i + 1, 1) == dq || substr(v, i + 1, 1) == "\\")) {
                        i++; out = out substr(v, i, 1)
                    } else if (c == dq) {
                        closed = 1; break
                    } else {
                        out = out c
                    }
                }
                if (!closed) next
                v = out
            } else {
                if (match(v, /[ \t]#/)) v = substr(v, 1, RSTART - 1)
                sub(/[ \t]+$/, "", v)
            }
            val = v; found = 1
        }
        END { if (found) printf "%s", val }
    ' "$2"
}

# set_prop KEY VALUE SOURCE: queues KEY=VALUE for writing. A missing value is an error.
set_prop() {
    local key=$1 val=$2 source=$3
    if [ -z "$val" ]; then
        echo -e "${RED}   ❌ No value found for $key in $source${NC}"
        MISSING=$((MISSING+1))
        return
    fi
    if [ "$DRY_RUN" -eq 1 ]; then
        echo "   would set $key (from $source)"
    fi
    KEYS+=("$key")
    VALS+=("$val")
}

# realm_secret FILE: the backend-service client secret of a Keycloak realm file (first match).
realm_secret() {
    jq -r 'first(.clients[] | select(.clientId == "backend-service") | .secret) // empty' "$1"
}

require_jq() {
    if ! command -v jq > /dev/null; then
        echo -e "${RED}❌ Error: jq is required to read the Keycloak client secret from the realm file${NC}"
        exit 1
    fi
}

# Writes the queued keys into PROP_FILE: an existing key line is replaced, a new key is appended.
# Values are escaped for the two parsers that read the file:
#   - java.util.Properties: "\" is an escape character and leading whitespace is dropped,
#     so "\" is written as "\\" and leading spaces/tabs as "\ " and "\t";
#   - SmallRye Config (Quarkus): "${...}" is an expression and "$$" collapses to "$",
#     so every "$" is written as "\\$" (Properties gives "\$", SmallRye gives "$").
# "#", "!", "=" and ":" need no escape inside a value. Values never reach a sed expression
# or the terminal. pre-flight-check.sh reverses this escaping when it reads the file.
write_props() {
    local i
    if [ -e "$PROP_FILE" ] && [ ! -f "$PROP_FILE" ]; then
        echo -e "${RED}❌ Error: $PROP_FILE exists but is not a regular file${NC}"
        return 1
    fi
    if [ ! -f "$PROP_FILE" ]; then
        # The secrets file is untracked: create it from the template on first run
        if ! cp "$TEMPLATE_FILE" "$PROP_FILE"; then
            echo -e "${RED}❌ Error: could not create $PROP_FILE from $TEMPLATE_FILE${NC}"
            return 1
        fi
    fi
    chmod 600 "$PROP_FILE" || return 1
    TMP_FILE=$(mktemp "${TMPDIR:-/tmp}/udan-local-secrets.XXXXXX") || return 1
    {
        for i in "${!KEYS[@]}"; do
            printf '%s\0%s\0' "${KEYS[$i]}" "${VALS[$i]}"
        done
    } | awk -v RS='\0' '
        function esc(v,   out, i, c, lead) {
            out = ""; lead = 1
            for (i = 1; i <= length(v); i++) {
                c = substr(v, i, 1)
                if (lead && c == " ") { out = out "\\ "; continue }
                if (lead && c == "\t") { out = out "\\t"; continue }
                lead = 0
                if (c == "\\") out = out "\\\\"
                else if (c == "$") out = out "\\\\$"
                else out = out c
            }
            return out
        }
        # First input: NUL-separated key/value pairs. Second input: the current file, line by line.
        FILENAME == "-" { if (NR % 2) k = $0; else { val[k] = esc($0); order[++nk] = k }; next }
        {
            for (j = 1; j <= nk; j++) {
                if (index($0, order[j] "=") == 1) { print order[j] "=" val[order[j]]; done[order[j]] = 1; next }
            }
            print
        }
        END { for (j = 1; j <= nk; j++) if (!(order[j] in done)) print order[j] "=" val[order[j]] }
    ' - RS='\n' "$PROP_FILE" > "$TMP_FILE" || return 1
    # Copy the content into the existing file, so it keeps its mode (600) and no temp file is left
    # in the source tree.
    cat "$TMP_FILE" > "$PROP_FILE" || return 1
}

# Prints the result and exits: 1 when a value is missing or the write fails, else 0.
finish() {
    if [ "$MISSING" -gt 0 ]; then
        echo -e "${RED}❌ $MISSING required value(s) missing (see above). Nothing was written to $PROP_FILE.${NC}"
        exit 1
    fi
    if [ "$DRY_RUN" -eq 1 ]; then
        echo -e "${GREEN}Dry run done: all values found, nothing written.${NC}"
        exit 0
    fi
    if ! write_props; then
        echo -e "${RED}❌ Error: writing $PROP_FILE failed. Check that the file and its directory are writable.${NC}"
        exit 1
    fi
    echo -e "✅ ${GREEN}Credentials synchronized into $PROP_FILE.${NC}"
    exit 0
}

if [ "$DRY_RUN" -eq 1 ]; then
    echo -e "🔄 ${YELLOW}Dry run: nothing is written.${NC}"
fi

if [ ! -d "$INFRA_DIR" ]; then
    echo -e "${RED}❌ Error: workspace infra not found at $INFRA_DIR${NC}"
    echo -e "${YELLOW}Requirement: a udan-workspace checkout. Clone https://github.com/Digital-Assistant/udan-workspace"
    echo -e "(branch dev), run ./udan bootstrap there, then run this script with"
    echo -e "UDAN_WORKSPACE=/path/to/udan-workspace (default: the parent directory).${NC}"
    exit 1
fi

require_jq

ENV_FILE="$INFRA_DIR/.env"
ENV_EXAMPLE="$INFRA_DIR/.env.example"
REALM_FILE="$INFRA_DIR/keycloak/realms/udan-realm.json"

if [ -f "$ENV_FILE" ]; then
    SRC="$ENV_FILE"
elif [ -f "$ENV_EXAMPLE" ]; then
    SRC="$ENV_EXAMPLE"
    echo -e "${YELLOW}⚠️  $ENV_FILE not found; using the dev defaults in $ENV_EXAMPLE.${NC}"
    echo -e "${YELLOW}   They match only data volumes created with them. Run scripts/infra-env.sh in the workspace to create .env.${NC}"
else
    echo -e "${RED}❌ Error: neither $ENV_FILE nor $ENV_EXAMPLE exists.${NC}"
    exit 1
fi
echo -e "🔄 ${YELLOW}Syncing credentials from $SRC and $REALM_FILE...${NC}"

# MariaDB application user: the workspace init script creates the fixed user `udan`.
# Quarkus dev mode takes the username from %dev.quarkus.datasource.username in
# application.properties (higher ordinal), so this key matters to pre-flight-check.sh only.
set_prop "quarkus.datasource.username" "udan" "workspace MariaDB init"
set_prop "quarkus.datasource.password" "$(read_env UDAN_DB_PASSWORD "$SRC")" "$SRC (UDAN_DB_PASSWORD)"
# Postgres superuser, used only by pre-flight-check.sh
set_prop "preflight.postgres.username" "$(read_env POSTGRES_USER "$SRC")" "$SRC (POSTGRES_USER)"
set_prop "preflight.postgres.password" "$(read_env POSTGRES_PASSWORD "$SRC")" "$SRC (POSTGRES_PASSWORD)"

# Keycloak client secret of backend-service, read from the realm file Keycloak imports
if [ -f "$REALM_FILE" ]; then
    KC_SECRET=$(realm_secret "$REALM_FILE")
    set_prop "quarkus.oidc.credentials.secret" "$KC_SECRET" "$REALM_FILE"
    ENV_SECRET=$(read_env KC_BACKEND_SERVICE_SECRET "$SRC")
    if [ -n "$KC_SECRET" ] && [ -n "$ENV_SECRET" ] && [ "$ENV_SECRET" != "$KC_SECRET" ]; then
        echo -e "${YELLOW}   ⚠️  Warning: KC_BACKEND_SERVICE_SECRET in $SRC differs from $REALM_FILE; the realm file wins.${NC}"
    fi
else
    set_prop "quarkus.oidc.credentials.secret" "" "$REALM_FILE (file not found)"
fi

finish
