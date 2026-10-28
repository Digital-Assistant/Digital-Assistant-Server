#!/bin/bash

# ==============================================================================
# SYNC-CREDENTIALS.SH
# ==============================================================================
# DESCRIPTION:
#   Writes local-secrets.properties (untracked) from the central credentials of the 
#   Digital_Assistant_microServices project.
#
# DEPENDENCIES:
#   1. Digital_Assistant_microServices project cloned in the parent directory.
#   2. DOCKER-MAP.md file exists in the microservices project.
#   3. local-secrets.properties.example in src/main/resources/ (the template).
#   4. jq, for reading the backend-service client secret from the realm export.
#
# USAGE:
#   ./sync-credentials.sh
# ==============================================================================

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# File paths
MAP_FILE="../Digital_Assistant_microServices/DOCKER-MAP.md"
REALM_FILE="../Digital_Assistant_microServices/docker/keycloak/realms/udan-realm.json"
PROP_FILE="src/main/resources/local-secrets.properties"
TEMPLATE_FILE="src/main/resources/local-secrets.properties.example"

echo -e "🔄 ${YELLOW}Syncing credentials from $MAP_FILE...${NC}"

# Check for dependency: The DOCKER-MAP.md file in the sibling project
if [ ! -f "$MAP_FILE" ]; then
    echo -e "${RED}❌ Error: Configuration file not found at $MAP_FILE${NC}"
    echo -e "${YELLOW}Requirement: Digital_Assistant_microServices must be cloned in the same parent directory.${NC}"
    exit 1
fi

# The secrets file is untracked: create it from the template on first run
if [ ! -f "$PROP_FILE" ]; then
    cp "$TEMPLATE_FILE" "$PROP_FILE"
    chmod 600 "$PROP_FILE"
fi

# Parsing Markdown table for credentials
# Logic: Scans for "Common Credentials" section and extracts pipe-delimited values
extract_cred() {
    local service=$1
    local column=$2 # 3 for Username, 4 for Password
    sed -n '/## 🔑 Common Credentials/,/##/p' "$MAP_FILE" | \
    grep -F "**$service**" | awk -F'|' -v col="$column" '{print $col}' | tr -d '` ' | head -n 1
}

# Updates or appends a key-value pair in local-secrets.properties
update_prop() {
    local key=$1
    local val=$2
    if [ -z "$val" ]; then
        echo -e "${YELLOW}   ⚠️  Warning: No value found for $key in $MAP_FILE${NC}"
        return
    fi
    
    # Check if key exists, update if it does, append if it doesn't
    if grep -q "^$key=" "$PROP_FILE"; then
        sed -i "s|^$key=.*|$key=$val|" "$PROP_FILE"
    else
        echo "$key=$val" >> "$PROP_FILE"
    fi
}

# Extract credentials for MariaDB and Postgres
DB_USER=$(extract_cred "MariaDB" 3)
DB_PASS=$(extract_cred "MariaDB" 4)
PG_USER=$(extract_cred "Postgres (Superuser)" 3)
PG_PASS=$(extract_cred "Postgres (Superuser)" 4)

echo -n "Updating $PROP_FILE... "
# Primary datasource (MariaDB)
update_prop "quarkus.datasource.username" "$DB_USER"
update_prop "quarkus.datasource.password" "$DB_PASS"

# Custom pre-flight keys (Postgres)
update_prop "preflight.postgres.username" "$PG_USER"
update_prop "preflight.postgres.password" "$PG_PASS"


# Keycloak client secret of backend-service, read from the realm export in the sibling project
if [ -f "$REALM_FILE" ] && command -v jq > /dev/null; then
    KC_SECRET=$(jq -r '.clients[] | select(.clientId == "backend-service") | .secret // empty' "$REALM_FILE")
    update_prop "quarkus.oidc.credentials.secret" "$KC_SECRET"
else
    echo -e "${YELLOW}   ⚠️  Warning: realm export or jq not found; quarkus.oidc.credentials.secret not set${NC}"
fi

echo -e "${GREEN}DONE${NC}"
echo -e "✅ ${GREEN}Credentials synchronized successfully.${NC}"
