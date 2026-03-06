#!/bin/bash

# ==============================================================================
# SYNC-CREDENTIALS.SH
# ==============================================================================
# DESCRIPTION:
#   Synchronizes local.properties with central credentials from the 
#   Digital_Assistant_microServices project.
#
# DEPENDENCIES:
#   1. Digital_Assistant_microServices project cloned in the parent directory.
#   2. DOCKER-MAP.md file exists in the microservices project.
#   3. Local local.properties file in src/main/resources/.
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
PROP_FILE="src/main/resources/local.properties"

echo -e "🔄 ${YELLOW}Syncing credentials from $MAP_FILE...${NC}"

# Check for dependency: The DOCKER-MAP.md file in the sibling project
if [ ! -f "$MAP_FILE" ]; then
    echo -e "${RED}❌ Error: Configuration file not found at $MAP_FILE${NC}"
    echo -e "${YELLOW}Requirement: Digital_Assistant_microServices must be cloned in the same parent directory.${NC}"
    exit 1
fi

# Parsing Markdown table for credentials
# Logic: Scans for "Common Credentials" section and extracts pipe-delimited values
extract_cred() {
    local service=$1
    local column=$2 # 3 for Username, 4 for Password
    sed -n '/## 🔑 Common Credentials/,/##/p' "$MAP_FILE" | \
    grep -F "**$service**" | awk -F'|' -v col="$column" '{print $col}' | tr -d '` ' | head -n 1
}

# Updates or appends a key-value pair in local.properties
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

echo -e "${GREEN}DONE${NC}"
echo -e "✅ ${GREEN}Credentials synchronized successfully.${NC}"
