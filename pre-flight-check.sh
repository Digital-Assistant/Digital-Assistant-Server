#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

MAP_FILE="../Digital_Assistant_microServices/DOCKER_MAP.md"

echo -e "🔍 ${YELLOW}Starting Comprehensive Pre-flight Check for UDAN...${NC}"

if [ ! -f "$MAP_FILE" ]; then
    echo -e "${RED}❌ Error: Configuration file not found at $MAP_FILE${NC}"
    exit 1
fi

# Parsing Markdown table for credentials
# Expecting: | **Service** | `Username` | `Password` |
extract_cred() {
    local service=$1
    local column=$2 # 3 for Username, 4 for Password
    # Extract the Common Credentials section and search within it
    sed -n '/## 🔑 Common Credentials/,/##/p' "$MAP_FILE" | \
    grep -F "**$service**" | awk -F'|' -v col="$column" '{print $col}' | tr -d '` ' | head -n 1
}

DB_USER=$(extract_cred "MariaDB" 3)
DB_PASS=$(extract_cred "MariaDB" 4)
PG_USER=$(extract_cred "Postgres (Superuser)" 3)
PG_PASS=$(extract_cred "Postgres (Superuser)" 4)

# Helper to extract Quarkus properties from local.properties
# Priority: %dev.key, then key
extract_prop() {
    local key=$1
    local file="src/main/resources/local.properties"
    local val=$(grep "^%dev\.$key=" "$file" | cut -d'=' -f2-)
    if [ -z "$val" ]; then
        val=$(grep "^$key=" "$file" | cut -d'=' -f2-)
    fi
    echo "$val" | tr -d '\r\n '
}

# Keycloak OIDC Configuration (Matching Quarkus)
KC_URL=$(extract_prop "quarkus.oidc.auth-server-url")
KC_CLIENT_ID=$(extract_prop "quarkus.oidc.client-id")
KC_CLIENT_SECRET=$(extract_prop "quarkus.oidc.credentials.secret")

# Adjust URL for localhost testing if needed (use 127.0.0.1 for stability)
KC_URL_TEST=$(echo "$KC_URL" | sed 's/localhost/127.0.0.1/')

# Debug: Print extracted credentials (masking password)
echo -e "Extracted credentials: MariaDB($DB_USER), Postgres($PG_USER), Keycloak(URL: $KC_URL_TEST, ID: $KC_CLIENT_ID)"

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

# 1.5. SSL Certificate Check
SSL_CERT="../Digital_Assistant_microServices/docker/nginx/localhost/localhost.crt"
echo -n "Checking SSL Certificate... "
if [ -f "$SSL_CERT" ]; then
    echo -e "${GREEN}FOUND${NC}"
else
    echo -e "${RED}MISSING (Run ./local-up.sh to generate)${NC}"
    exit 1
fi

echo -e "\n📡 ${YELLOW}Testing Connectivity & Credentials...${NC}"

# 2. MariaDB Connectivity
echo -n "MariaDB ($DB_USER)... "
if command -v mariadb &> /dev/null; then
    if mariadb -h 127.0.0.1 -u "$DB_USER" -p"$DB_PASS" -e "SELECT 1;" &>/dev/null; then
        echo -e "${GREEN}CONNECTED${NC}"
    else
        echo -e "${RED}AUTH FAILED${NC}"
        exit 1
    fi
elif command -v mysql &> /dev/null; then
    if mysql -h 127.0.0.1 -u "$DB_USER" -p"$DB_PASS" -e "SELECT 1;" &>/dev/null; then
        echo -e "${GREEN}CONNECTED${NC}"
    else
        echo -e "${RED}AUTH FAILED${NC}"
        exit 1
    fi
else
    if timeout 1 bash -c "</dev/tcp/127.0.0.1/3306" 2>/dev/null; then
        echo -e "${YELLOW}PORT OPEN (Client not installed)${NC}"
    else
        echo -e "${RED}UNREACHABLE${NC}"
        exit 1
    fi
fi

# 3. Postgres Connectivity
echo -n "Postgres ($PG_USER)... "
if command -v psql &> /dev/null; then
    if PGPASSWORD="$PG_PASS" psql -h 127.0.0.1 -U "$PG_USER" -d postgres -c "SELECT 1;" &>/dev/null; then
        echo -e "${GREEN}CONNECTED${NC}"
    else
        echo -e "${RED}AUTH FAILED${NC}"
        exit 1
    fi
else
    if timeout 1 bash -c "</dev/tcp/127.0.0.1/5432" 2>/dev/null; then
        echo -e "${YELLOW}PORT OPEN (Client not installed)${NC}"
    else
        echo -e "${RED}UNREACHABLE${NC}"
        exit 1
    fi
fi

# 4. Elasticsearch Health
echo -n "Elasticsearch... "
ES_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:9200)
if [ "$ES_STATUS" == "200" ]; then
    echo -e "${GREEN}HEALTHY${NC}"
else
    echo -e "${RED}FAILED (Status: $ES_STATUS)${NC}"
    exit 1
fi

# 5. Keycloak Access (Matching Quarkus OIDC flow)
echo -n "Keycloak OIDC ($KC_CLIENT_ID)... "
# 1. First check if the OIDC discovery endpoint is accessible
DISCOVERY_STATUS=$(curl -s -k -o /dev/null -w "%{http_code}" "$KC_URL_TEST/.well-known/openid-configuration")
if [ "$DISCOVERY_STATUS" != "200" ]; then
    echo -e "${RED}DISCOVERY FAILED (Status: $DISCOVERY_STATUS)${NC}"
    exit 1
fi

# 2. Try to get a token using client_credentials (this verifies client_id and secret)
KC_TOKEN=$(curl -s -k -X POST "$KC_URL_TEST/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=client_credentials" \
    -d "client_id=$KC_CLIENT_ID" \
    -d "client_secret=$KC_CLIENT_SECRET" 2>/dev/null | grep -o '"access_token":"[^"]*' | grep -o '[^"]*$')

if [ -n "$KC_TOKEN" ]; then
    echo -e "${GREEN}AUTHENTICATED${NC}"
else
    echo -e "${RED}AUTH FAILED (Check client-id/secret)${NC}"
    exit 1
fi

# 6. NestJS API (Token Service)
echo -n "NestJS API... "
# The NestJS service is bound to 3003 (as per local-up.sh and DOCKER_MAP.md)
# Container healthcheck uses http, so we check http here.
NEST_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:3003)
if [ "$NEST_STATUS" != "000" ]; then
    echo -e "${GREEN}UP (Status: $NEST_STATUS)${NC}"
else
    echo -e "${RED}DOWN${NC}"
    exit 1
fi

# 7. Nginx Gateway
echo -n "Nginx Gateway... "
NGINX_STATUS=$(curl -s -k -o /dev/null -w "%{http_code}" https://127.0.0.1:443)
if [ "$NGINX_STATUS" != "000" ]; then
    echo -e "${GREEN}UP (Status: $NGINX_STATUS)${NC}"
else
    echo -e "${RED}DOWN${NC}"
    exit 1
fi

echo -e "\n${GREEN}🚀 All pre-flight checks passed! Starting Quarkus Backend...${NC}"
chmod +x gradlew
./gradlew quarkusDev --no-daemon
