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

# 1.5. SSL Certificate Check (Shared dependency with microservices)
SSL_CERT="../Digital_Assistant_microServices/docker/nginx/localhost/localhost.crt"
echo -n "Checking SSL Certificate... "
if [ -f "$SSL_CERT" ]; then
    echo -e "${GREEN}FOUND${NC}"
else
    echo -e "${RED}MISSING (Run ./local-up.sh to generate)${NC}"
    exit 1
fi

echo -e "\n📡 ${YELLOW}Testing Connectivity & Credentials...${NC}"
FAILED_CHECKS=0

# 2. MariaDB Connectivity (Uses extracted DB_USER/DB_PASS)
echo -n "MariaDB ($DB_USER)... "
if command -v mariadb &> /dev/null; then
    if mariadb -h 127.0.0.1 -u "$DB_USER" -p"$DB_PASS" -e "SELECT 1;" &>/dev/null; then
        echo -e "${GREEN}CONNECTED${NC}"
    else
        echo -e "${RED}AUTH FAILED${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS+1))
    fi
elif command -v mysql &> /dev/null; then
    if mysql -h 127.0.0.1 -u "$DB_USER" -p"$DB_PASS" -e "SELECT 1;" &>/dev/null; then
        echo -e "${GREEN}CONNECTED${NC}"
    else
        echo -e "${RED}AUTH FAILED${NC}"
        FAILED_CHECKS=$((FAILED_CHECKS+1))
    fi
else
    if timeout 1 bash -c "</dev/tcp/127.0.0.1/3306" 2>/dev/null; then
        echo -e "${YELLOW}PORT OPEN (Client not installed)${NC}"
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

echo -e "\n${GREEN}✅ All pre-flight checks passed!${NC}"
exit 0
