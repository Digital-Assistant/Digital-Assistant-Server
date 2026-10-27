#!/bin/bash

# ==============================================================================
# PRE-FLIGHT-CHECK.SH
# ==============================================================================
# DESCRIPTION:
#   Validates that the local development environment is ready for the UDAN
#   backend by checking containers, SSL certificates, and credentials.
#
# DEPENDENCIES:
#   1. local.properties in src/main/resources/ (populated with credentials).
#   2. Docker Desktop/Engine running.
#   3. local-up.sh has been run in Digital_Assistant_microServices.
#   4. Tools: curl, docker, (optional: mariadb/mysql client, psql client).
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

# File path for local configuration
PROP_FILE="src/main/resources/local.properties"

echo -e "🔍 ${YELLOW}Starting Comprehensive Pre-flight Check for UDAN...${NC}"

# Dependency Check: local.properties must exist
if [ ! -f "$PROP_FILE" ]; then
    echo -e "${RED}❌ Error: local.properties not found at $PROP_FILE${NC}"
    echo -e "${YELLOW}Hint: Run ./sync-credentials.sh if it's missing or needs update.${NC}"
    exit 1
fi

# Logic: Extracts value for key from PROP_FILE (prioritizes %dev. prefix)
extract_prop() {
    local key=$1
    local val=$(grep "^%dev\.$key=" "$PROP_FILE" | cut -d'=' -f2-)
    if [ -z "$val" ]; then
        val=$(grep "^$key=" "$PROP_FILE" | cut -d'=' -f2-)
    fi
    echo "$val" | tr -d '\r\n '
}

# Pull credentials from properties file
DB_USER=$(extract_prop "quarkus.datasource.username")
DB_PASS=$(extract_prop "quarkus.datasource.password")
DB_URL=$(extract_prop "quarkus.datasource.jdbc.url")
PG_USER=$(extract_prop "preflight.postgres.username")
PG_PASS=$(extract_prop "preflight.postgres.password")

# Keycloak OIDC Configuration
KC_URL=$(extract_prop "quarkus.oidc.auth-server-url")
KC_CLIENT_ID=$(extract_prop "quarkus.oidc.client-id")
KC_CLIENT_SECRET=$(extract_prop "quarkus.oidc.credentials.secret")

# Adjust URL for localhost testing (127.0.0.1 for stability)
KC_URL_TEST=$(echo "$KC_URL" | sed 's/localhost/127.0.0.1/')

# Logic: Checks if a docker container by the given name is running
check_container() {
    local name=$1
    echo -n "Checking Container [$name]... "
    if [ "$(docker ps -q -f name=^/${name}$)" ]; then
        echo -e "${GREEN}RUNNING${NC}"
    else
        echo -e "${RED}NOT RUNNING${NC}"
        return 1
    fi
}

# 1. Container Status Checks
CONTAINERS=("mariadb-local" "postgres-local" "elasticsearch-local" "keycloak-local" "token-service-local" "nginx-local")
FAILED_CONTAINERS=0
for container in "${CONTAINERS[@]}"; do
    check_container "$container" || FAILED_CONTAINERS=$((FAILED_CONTAINERS+1))
done

if [ $FAILED_CONTAINERS -gt 0 ]; then
    echo -e "${RED}❌ Some containers are not running. Please run ./local-up.sh in Digital_Assistant_microServices.${NC}"
    exit 1
fi

# 1.5. SSL Certificate Check (shared dependency with microservices)
# The same files back Nginx, Keycloak and MariaDB's --ssl-cert, so an expired cert
# breaks TLS verification for every host client (mariadb CLI, curl, browsers) while
# Quarkus keeps working, because its JDBC URL sets trustServerCertificate=true.
# Expiry is therefore a WARNING here, not a failure: it must not block a stack that
# Quarkus can use. It is reported because it silently breaks everything else.
SSL_CERT="../Digital_Assistant_microServices/docker/nginx/localhost/localhost.crt"
echo -n "Checking SSL Certificate... "
if [ ! -f "$SSL_CERT" ]; then
    echo -e "${RED}MISSING (Run ./local-up.sh to generate)${NC}"
    exit 1
fi

WARNINGS=0
if CERT_END=$(openssl x509 -in "$SSL_CERT" -noout -enddate 2>/dev/null | cut -d'=' -f2); then
    # openssl x509 -checkend <seconds> exits non-zero once the cert expires inside that window.
    if ! openssl x509 -in "$SSL_CERT" -noout -checkend 0 &>/dev/null; then
        echo -e "${RED}EXPIRED${NC}"
        echo -e "${YELLOW}   ⚠️  NotAfter=$CERT_END. MariaDB/Nginx/Keycloak serve this cert; host clients that verify it fail with"
        echo -e "      'ERROR 2026 (HY000): TLS/SSL error: certificate has expired'. Quarkus still connects"
        echo -e "      (trustServerCertificate=true), so this does not block the server.${NC}"
        echo -e "${YELLOW}   Fix: delete docker/nginx/localhost/localhost.{crt,key} in Digital_Assistant_microServices,"
        echo -e "      then ./local-down.sh && ./local-up.sh (local-up.sh regenerates and re-copies to Keycloak).${NC}"
        WARNINGS=$((WARNINGS+1))
    elif ! openssl x509 -in "$SSL_CERT" -noout -checkend 604800 &>/dev/null; then
        echo -e "${YELLOW}EXPIRING WITHIN 7 DAYS (NotAfter=$CERT_END)${NC}"
        echo -e "${YELLOW}   Fix: ./local-down.sh && ./local-up.sh after deleting docker/nginx/localhost/localhost.{crt,key}.${NC}"
        WARNINGS=$((WARNINGS+1))
    else
        echo -e "${GREEN}VALID (NotAfter=$CERT_END)${NC}"
    fi
else
    echo -e "${YELLOW}UNREADABLE (NotAfter unknown; is openssl installed?)${NC}"
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
        echo -e "   ./local-down.sh && ./local-up.sh (in Digital_Assistant_microServices).${NC}"
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
        DB_ERR=$("$DB_CLIENT" --ssl --ssl-verify-server-cert=OFF -h 127.0.0.1 -P 3306 \
            -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" -e "SELECT 1;" 2>&1 >/dev/null)
    else
        DB_ERR=$("$DB_CLIENT" --ssl-mode=REQUIRED -h 127.0.0.1 -P 3306 \
            -u "$DB_USER" -p"$DB_PASS" -D "$DB_NAME" -e "SELECT 1;" 2>&1 >/dev/null)
    fi
    DB_RC=$?
    if [ $DB_RC -eq 0 ]; then
        echo -e "${GREEN}CONNECTED${NC}"
    elif echo "$DB_ERR" | grep -qi "access denied"; then
        # Credential problem: the message must name the cause, not say "AUTH FAILED" for TLS issues too.
        echo -e "${RED}AUTH FAILED (rejected user/password)${NC}"
        echo -e "${YELLOW}   Hint: ./sync-credentials.sh to re-read DOCKER-MAP.md, or check the MariaDB user in docker/mariadb/init/.${NC}"
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
echo -n "Keycloak OIDC ($KC_CLIENT_ID)... "
KC_FAILED=0
DISCOVERY_STATUS=$(curl -s -k -o /dev/null -w "%{http_code}" "$KC_URL_TEST/.well-known/openid-configuration")
if [ "$DISCOVERY_STATUS" != "200" ]; then
    echo -e "${RED}DISCOVERY FAILED (Status: $DISCOVERY_STATUS)${NC}"
    KC_FAILED=1
else
    KC_TOKEN=$(curl -s -k -X POST "$KC_URL_TEST/protocol/openid-connect/token" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "grant_type=client_credentials" \
        -d "client_id=$KC_CLIENT_ID" \
        -d "client_secret=$KC_CLIENT_SECRET" 2>/dev/null | grep -o '"access_token":"[^"]*' | grep -o '[^"]*$')

    if [ -n "$KC_TOKEN" ]; then
        echo -e "${GREEN}AUTHENTICATED${NC}"
    else
        echo -e "${RED}AUTH FAILED (Check client-id/secret)${NC}"
        KC_FAILED=1
    fi
fi

if [ $KC_FAILED -eq 1 ]; then
    echo -e "${YELLOW}   ℹ️  Keycloak often takes 3-4 minutes to fully initialize. Please wait and retry.${NC}"
    FAILED_CHECKS=$((FAILED_CHECKS+1))
fi

# 6. NestJS API (Check token service)
echo -n "NestJS API... "
NEST_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:3003)
if [ "$NEST_STATUS" != "000" ]; then
    echo -e "${GREEN}UP (Status: $NEST_STATUS)${NC}"
else
    echo -e "${RED}DOWN${NC}"
    FAILED_CHECKS=$((FAILED_CHECKS+1))
fi

# 7. Nginx Gateway (SSL endpoint)
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
