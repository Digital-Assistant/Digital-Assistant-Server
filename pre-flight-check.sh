#!/bin/bash

# ==============================================================================
# PRE-FLIGHT-CHECK.SH
# ==============================================================================
# DESCRIPTION:
#   Validates that the local development environment is ready for the UDAN
#   backend by checking containers, SSL certificates, and credentials.
#
# DEPENDENCIES:
#   1. local.properties and local-secrets.properties in src/main/resources/
#      (the second holds the credentials; ./sync-credentials.sh creates it).
#   2. Docker Desktop/Engine running.
#   3. The local infrastructure of the udan-workspace checkout is up (./udan up infra
#      there). Its path is $UDAN_WORKSPACE, default ".." (the parent directory of
#      this repository); a relative path is resolved against the directory you run
#      the script from. Without the workspace the script stops with a pointer.
#   4. Tools: curl, docker, openssl (optional: mariadb/mysql client, psql client).
#
# EXIT CODES:
#   0 - All checks passed.
#   1 - One or more checks failed.
# ==============================================================================

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

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

if [ ! -d "$INFRA_DIR" ]; then
    echo -e "${RED}❌ Error: workspace infra not found at $INFRA_DIR${NC}"
    echo -e "${YELLOW}Requirement: a udan-workspace checkout. Clone https://github.com/Digital-Assistant/udan-workspace"
    echo -e "(branch dev), run ./udan bootstrap there, then run this script with"
    echo -e "UDAN_WORKSPACE=/path/to/udan-workspace (default: the parent directory).${NC}"
    exit 1
fi

SSL_CERT="$INFRA_DIR/nginx/localhost/localhost.crt"
REALM_FILE="$INFRA_DIR/keycloak/realms/udan-realm.json"

# File path for local configuration
PROP_FILE="src/main/resources/local.properties"
SECRETS_FILE="src/main/resources/local-secrets.properties"

echo -e "🔍 ${YELLOW}Starting Comprehensive Pre-flight Check for UDAN...${NC}"

# Dependency Check: local.properties must exist
if [ ! -f "$PROP_FILE" ]; then
    echo -e "${RED}❌ Error: local.properties not found at $PROP_FILE${NC}"
    echo -e "${YELLOW}Hint: Run ./sync-credentials.sh if it's missing or needs update.${NC}"
    exit 1
fi
if [ ! -f "$SECRETS_FILE" ]; then
    echo -e "${RED}❌ Error: local-secrets.properties not found at $SECRETS_FILE${NC}"
    echo -e "${YELLOW}Hint: Run ./sync-credentials.sh to create it from the workspace infra ($WS/infra).${NC}"
    exit 1
fi

# prop_value KEY FILE...: the value of KEY as Quarkus reads it, for single-line entries.
# Within one file the last entry wins; across files the first file with a non-empty value wins.
# It undoes the escaping sync-credentials.sh writes: java.util.Properties escapes ("\\" -> "\",
# "\ " -> " ", "\t" -> tab, leading whitespace dropped), then SmallRye's "\$" -> "$" and "$$" -> "$".
# ${...} expressions are not expanded.
prop_value() {
    KEY="$1" awk '
        BEGIN { k = ENVIRON["KEY"]; nf = 0 }
        FNR == 1 { order[++nf] = FILENAME }
        {
            line = $0
            sub(/\r$/, "", line)
            sub(/^[ \t\f]+/, "", line)
            if (index(line, k) != 1) next
            rest = substr(line, length(k) + 1)
            if (!match(rest, /^[ \t\f]*[=:][ \t\f]*/)) next
            last[FILENAME] = substr(rest, RLENGTH + 1)
        }
        function unescape(v,   out, i, c) {
            out = ""
            for (i = 1; i <= length(v); i++) {
                c = substr(v, i, 1)
                if (c == "\\" && i < length(v)) {
                    c = substr(v, ++i, 1)
                    if (c == "t") c = "\t"; else if (c == "n") c = "\n"; else if (c == "r") c = "\r"; else if (c == "f") c = "\f"
                }
                out = out c
            }
            return out
        }
        function smallrye(v,   out, i, c) {
            out = ""
            for (i = 1; i <= length(v); i++) {
                c = substr(v, i, 1)
                if ((c == "\\" || c == "$") && substr(v, i + 1, 1) == "$") { out = out "$"; i++; continue }
                out = out c
            }
            return out
        }
        END {
            for (j = 1; j <= nf; j++) {
                if ((order[j] in last) && last[order[j]] != "") { printf "%s", smallrye(unescape(last[order[j]])); exit }
            }
        }
    ' "${@:2}"
}

# Logic: Extracts value for key from the properties files (prioritizes %dev. prefix).
# local-secrets.properties is read first, so the values sync-credentials.sh writes win
# for this script. (Quarkus itself may prefer a %dev. key in application.properties.)
extract_prop() {
    local key=$1 val
    val=$(prop_value "%dev.$key" "$SECRETS_FILE" "$PROP_FILE")
    if [ -z "$val" ]; then
        val=$(prop_value "$key" "$SECRETS_FILE" "$PROP_FILE")
    fi
    printf '%s' "$val"
}

# Pull settings and credentials from local.properties and local-secrets.properties
DB_USER=$(extract_prop "quarkus.datasource.username")
DB_PASS=$(extract_prop "quarkus.datasource.password")
DB_URL=$(extract_prop "quarkus.datasource.jdbc.url")
PG_USER=$(extract_prop "preflight.postgres.username")
PG_PASS=$(extract_prop "preflight.postgres.password")

# Keycloak OIDC Configuration
KC_URL=$(extract_prop "quarkus.oidc.auth-server-url")
KC_CLIENT_ID=$(extract_prop "quarkus.oidc.client-id")
KC_CLIENT_SECRET=$(extract_prop "quarkus.oidc.credentials.secret")

# Every value below is required: an empty one is a failure, not a later "AUTH FAILED".
MISSING_KEYS=()
[ -n "$DB_USER" ] || MISSING_KEYS+=("quarkus.datasource.username")
[ -n "$DB_PASS" ] || MISSING_KEYS+=("quarkus.datasource.password")
[ -n "$PG_USER" ] || MISSING_KEYS+=("preflight.postgres.username")
[ -n "$PG_PASS" ] || MISSING_KEYS+=("preflight.postgres.password")
[ -n "$KC_URL" ] || MISSING_KEYS+=("quarkus.oidc.auth-server-url")
[ -n "$KC_CLIENT_ID" ] || MISSING_KEYS+=("quarkus.oidc.client-id")
[ -n "$KC_CLIENT_SECRET" ] || MISSING_KEYS+=("quarkus.oidc.credentials.secret")
if [ ${#MISSING_KEYS[@]} -gt 0 ]; then
    echo -e "${RED}❌ Missing value(s) in $SECRETS_FILE / $PROP_FILE: ${MISSING_KEYS[*]}${NC}"
    echo -e "${YELLOW}Hint: run ./sync-credentials.sh to fill them from the workspace infra ($WS/infra).${NC}"
    exit 1
fi

# Adjust URL for localhost testing (127.0.0.1 for stability)
KC_URL_TEST=${KC_URL//localhost/127.0.0.1}

# Logic: Checks if a docker container by the given name is running
check_container() {
    local name=$1
    echo -n "Checking Container [$name]... "
    if [ "$(docker ps -q -f "name=^/${name}$")" ]; then
        echo -e "${GREEN}RUNNING${NC}"
    else
        echo -e "${RED}NOT RUNNING${NC}"
        return 1
    fi
}

# 1. Container Status Checks
CONTAINERS=("mariadb-local" "postgres-local" "elasticsearch-local" "keycloak-local" "nginx-local")
FAILED_CONTAINERS=0
for container in "${CONTAINERS[@]}"; do
    check_container "$container" || FAILED_CONTAINERS=$((FAILED_CONTAINERS+1))
done

if [ $FAILED_CONTAINERS -gt 0 ]; then
    echo -e "${RED}❌ Some containers are not running. In the workspace ($WS) run ./udan up infra, then ./udan doctor.${NC}"
    exit 1
fi

# 1.5. SSL Certificate Check (generated by the workspace infra)
# The same files back Nginx, Keycloak and MariaDB's --ssl-cert, so an expired cert
# breaks TLS verification for every host client (mariadb CLI, curl, browsers) while
# Quarkus keeps working, because its JDBC URL sets trustServerCertificate=true.
# Expiry is therefore a WARNING here, not a failure: it must not block a stack that
# Quarkus can use. It is reported because it silently breaks everything else.
echo -n "Checking SSL Certificate... "
if [ ! -f "$SSL_CERT" ]; then
    echo -e "${RED}MISSING at $SSL_CERT (in the workspace run ./udan up infra, which generates it)${NC}"
    exit 1
fi

WARNINGS=0
# The status of a pipeline is the status of `cut`, so test the output: empty means openssl failed.
CERT_END=$(openssl x509 -in "$SSL_CERT" -noout -enddate 2>/dev/null | cut -d'=' -f2)
if [ -n "$CERT_END" ]; then
    # openssl x509 -checkend <seconds> exits non-zero once the cert expires inside that window.
    if ! openssl x509 -in "$SSL_CERT" -noout -checkend 0 &>/dev/null; then
        echo -e "${RED}EXPIRED${NC}"
        echo -e "${YELLOW}   ⚠️  NotAfter=$CERT_END. MariaDB/Nginx/Keycloak serve this cert; host clients that verify it fail with"
        echo -e "      'ERROR 2026 (HY000): TLS/SSL error: certificate has expired'. Quarkus still connects"
        echo -e "      (trustServerCertificate=true), so this does not block the server.${NC}"
        echo -e "${YELLOW}   Fix: in the workspace run scripts/infra-certs.sh --force (regenerates infra/nginx/localhost/"
        echo -e "      and the Keycloak copy), then restart the infra with ./udan.${NC}"
        WARNINGS=$((WARNINGS+1))
    elif ! openssl x509 -in "$SSL_CERT" -noout -checkend 604800 &>/dev/null; then
        echo -e "${YELLOW}EXPIRING WITHIN 7 DAYS (NotAfter=$CERT_END)${NC}"
        echo -e "${YELLOW}   Fix: in the workspace run scripts/infra-certs.sh --force, then restart the infra with ./udan.${NC}"
        WARNINGS=$((WARNINGS+1))
    else
        echo -e "${GREEN}VALID (NotAfter=$CERT_END)${NC}"
    fi
else
    echo -e "${YELLOW}UNREADABLE (NotAfter unknown; is openssl installed and the file a PEM certificate?)${NC}"
    WARNINGS=$((WARNINGS+1))
fi

# What the RUNNING MariaDB serves can lag the file on disk: the container reads its
# --ssl-cert at startup, so newly regenerated certs do nothing until a restart.
SERVED_PEM=$(echo | timeout 5 openssl s_client -connect 127.0.0.1:3306 -starttls mysql 2>/dev/null)
if [ -n "$SERVED_PEM" ] && echo "$SERVED_PEM" | openssl x509 -noout &>/dev/null; then
    if echo "$SERVED_PEM" | openssl x509 -noout -checkend 0 &>/dev/null; then
        echo -e "   Served by mariadb-local: ${GREEN}valid${NC}"
    else
        SERVED_END=$(echo "$SERVED_PEM" | openssl x509 -noout -enddate | cut -d'=' -f2)
        echo -e "   Served by mariadb-local: ${RED}EXPIRED ($SERVED_END)${NC}"
        echo -e "${YELLOW}   The cert file on disk differs from what the container loaded. Restart to pick it up:"
        echo -e "   restart the infra with ./udan in the workspace ($WS).${NC}"
        WARNINGS=$((WARNINGS+1))
    fi
fi

echo -e "\n📡 ${YELLOW}Testing Connectivity & Credentials...${NC}"
FAILED_CHECKS=0

# 2. MariaDB Connectivity (Uses extracted DB_USER/DB_PASS against the primary schema)
# TLS is negotiated exactly as the JDBC URL does it (useSSL=true&trustServerCertificate=true):
# encrypted, but the server certificate is NOT verified. Verifying it here would fail on the
# expired self-signed dev cert while Quarkus connects fine - a false negative that blocks a
# working stack. Cert health is reported separately in step 1.5.
DB_NAME=$(echo "$DB_URL" | sed -n 's|.*/\([^/?]*\)?.*|\1|p')
DB_NAME=${DB_NAME:-udan}
echo -n "MariaDB ($DB_USER@$DB_NAME)... "
DB_CLIENT=""
if command -v mariadb &> /dev/null; then
    DB_CLIENT="mariadb"
elif command -v mysql &> /dev/null; then
    DB_CLIENT="mysql"
fi

if [ -n "$DB_CLIENT" ]; then
    # mariadb CLI and mysql CLI need different flags to get "TLS on, verification off".
    if [ "$DB_CLIENT" = "mariadb" ]; then
        # The password goes in MYSQL_PWD, not on the command line, so `ps` does not show it.
        DB_ERR=$(MYSQL_PWD="$DB_PASS" "$DB_CLIENT" --ssl --ssl-verify-server-cert=OFF -h 127.0.0.1 -P 3306 \
            -u "$DB_USER" -D "$DB_NAME" -e "SELECT 1;" 2>&1 >/dev/null)
    else
        DB_ERR=$(MYSQL_PWD="$DB_PASS" "$DB_CLIENT" --ssl-mode=REQUIRED -h 127.0.0.1 -P 3306 \
            -u "$DB_USER" -D "$DB_NAME" -e "SELECT 1;" 2>&1 >/dev/null)
    fi
    DB_RC=$?
    if [ $DB_RC -eq 0 ]; then
        echo -e "${GREEN}CONNECTED${NC}"
    elif echo "$DB_ERR" | grep -qi "access denied"; then
        # Credential problem: the message must name the cause, not say "AUTH FAILED" for TLS issues too.
        echo -e "${RED}AUTH FAILED (rejected user/password)${NC}"
        echo -e "${YELLOW}   Hint: run ./sync-credentials.sh to re-read $WS/infra/.env. A data volume keeps the passwords it was"
        echo -e "   created with, so infra/.env must hold those values (or start the infra clean). Then ./udan doctor.${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS+1))
    elif echo "$DB_ERR" | grep -qiE "certificate|ssl|tls"; then
        echo -e "${RED}TLS ERROR${NC}"
        echo -e "${YELLOW}   $DB_ERR${NC}"
        echo -e "${YELLOW}   Not a credential problem. Regenerate the dev certs (see step 1.5).${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS+1))
    else
        echo -e "${RED}FAILED${NC}"
        echo -e "${YELLOW}   $DB_ERR${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS+1))
    fi
else
    if timeout 1 bash -c "</dev/tcp/127.0.0.1/3306" 2>/dev/null; then
        echo -e "${YELLOW}PORT OPEN (Client not installed; credentials NOT verified)${NC}"
        WARNINGS=$((WARNINGS+1))
    else
        echo -e "${RED}UNREACHABLE${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS+1))
    fi
fi

# 3. Postgres Connectivity (Uses preflight.postgres credentials)
echo -n "Postgres ($PG_USER)... "
if command -v psql &> /dev/null; then
    if PGPASSWORD="$PG_PASS" psql -h 127.0.0.1 -U "$PG_USER" -d postgres -c "SELECT 1;" &>/dev/null; then
        echo -e "${GREEN}CONNECTED${NC}"
    else
        echo -e "${RED}AUTH FAILED${NC}"
        echo -e "${YELLOW}   Hint: ./sync-credentials.sh copies POSTGRES_USER/POSTGRES_PASSWORD from $WS/infra/.env;"
        echo -e "   they must match the ones the Postgres data volume was created with.${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS+1))
    fi
else
    if timeout 1 bash -c "</dev/tcp/127.0.0.1/5432" 2>/dev/null; then
        echo -e "${YELLOW}PORT OPEN (Client not installed)${NC}"
    else
        echo -e "${RED}UNREACHABLE${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS+1))
    fi
fi

# 4. Elasticsearch Health (Rest API check)
echo -n "Elasticsearch... "
ES_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:9200)
if [ "$ES_STATUS" == "200" ]; then
    echo -e "${GREEN}HEALTHY${NC}"
else
    echo -e "${RED}FAILED (Status: $ES_STATUS)${NC}"
    FAILED_CHECKS=$((FAILED_CHECKS+1))
fi

# 5. Keycloak Access (Verifies OIDC flow tokens)
# The Server serves two tenants (realms UDAN and UDAN-Plugin). backend-service logs in to UDAN. In UDAN-Plugin the
# Server only verifies tokens (its backend-service has no service account), so that realm is checked by discovery.
KC_BASE=${KC_URL_TEST%/realms/*}
for KC_REALM in UDAN UDAN-Plugin; do
    echo -n "Keycloak OIDC ($KC_CLIENT_ID @ $KC_REALM)... "
    KC_FAILED=0
    DISCOVERY_STATUS=$(curl -s -k -o /dev/null -w "%{http_code}" "$KC_BASE/realms/$KC_REALM/.well-known/openid-configuration")
    if [ "$DISCOVERY_STATUS" != "200" ]; then
        echo -e "${RED}DISCOVERY FAILED (Status: $DISCOVERY_STATUS)${NC}"
        KC_FAILED=1
    elif [ "$KC_REALM" != "UDAN" ]; then
        echo -e "${GREEN}REACHABLE (discovery 200; tokens are only verified here)${NC}"
    else
        # The secret is read from stdin and URL-encoded, so it is not on the command line.
        KC_TOKEN=$(printf '%s' "$KC_CLIENT_SECRET" | curl -s -k -X POST "$KC_BASE/realms/$KC_REALM/protocol/openid-connect/token" \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "grant_type=client_credentials" \
            --data-urlencode "client_id=$KC_CLIENT_ID" \
            --data-urlencode "client_secret@-" 2>/dev/null | grep -o '"access_token":"[^"]*' | grep -o '[^"]*$')

        if [ -n "$KC_TOKEN" ]; then
            echo -e "${GREEN}AUTHENTICATED${NC}"
        else
            echo -e "${RED}AUTH FAILED (Check client-id/secret)${NC}"
            echo -e "${YELLOW}   Hint: ./sync-credentials.sh copies the secret from $REALM_FILE."
            echo -e "   Keycloak imports the realm files only into a fresh data volume; an older realm keeps its own secret.${NC}"
            KC_FAILED=1
        fi
    fi

    if [ $KC_FAILED -eq 1 ]; then
        echo -e "${YELLOW}   ℹ️  Keycloak often takes 3-4 minutes to fully initialize. Please wait and retry.${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS+1))
    fi
done

# 6. Nginx Gateway (SSL endpoint)
echo -n "Nginx Gateway... "
NGINX_STATUS=$(curl -s -k -o /dev/null -w "%{http_code}" https://127.0.0.1:443)
if [ "$NGINX_STATUS" != "000" ]; then
    echo -e "${GREEN}UP (Status: $NGINX_STATUS)${NC}"
else
    echo -e "${RED}DOWN${NC}"
    FAILED_CHECKS=$((FAILED_CHECKS+1))
fi

if [ $FAILED_CHECKS -gt 0 ]; then
    echo -e "\n${RED}❌ $FAILED_CHECKS pre-flight check(s) failed. Please resolve the issues above before starting.${NC}"
    exit 1
fi

if [ "${WARNINGS:-0}" -gt 0 ]; then
    echo -e "\n${YELLOW}⚠️  ${WARNINGS} warning(s) above. They do not stop Quarkus, but they break other tooling.${NC}"
fi

echo -e "\n${GREEN}✅ All pre-flight checks passed!${NC}"
exit 0
