#!/bin/bash

# ==============================================================================
# START-SERVER.SH
# ==============================================================================
# DESCRIPTION:
#   Main entry point for starting the UDAN Backend development server.
#   It ensures all environmental prerequisites are met before launching
#   the Quarkus application in dev mode.
#
# DEPENDENCIES:
#   1. ./pre-flight-check.sh (must be in the same directory).
#   2. ./gradlew (Gradle wrapper must be present and executable).
#
# USAGE:
#   ./start-server.sh
# ==============================================================================

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "🚀 ${GREEN}Initializing UDAN Backend Startup Sequence...${NC}"

# 1. Run Pre-flight Check
# This validates containers, connectivity, and credentials.
if [ -f "./pre-flight-check.sh" ]; then
    ./pre-flight-check.sh
    if [ $? -ne 0 ]; then
        echo -e "${RED}🛑 Startup aborted due to pre-flight failures.${NC}"
        echo -e "${YELLOW}Please resolve the issues above and try again.${NC}"
        exit 1
    fi
else
    echo -e "${RED}❌ Error: pre-flight-check.sh not found!${NC}"
    exit 1
fi

# 2. Start Quarkus Dev Mode
# Using --no-daemon to ensure a clean process lifecycle in the terminal.
echo -e "\n🔥 ${GREEN}Starting Quarkus in Dev Mode...${NC}"
if [ -f "./gradlew" ]; then
    chmod +x gradlew
    ./gradlew quarkusDev --no-daemon
else
    echo -e "${RED}❌ Error: gradlew (Gradle wrapper) not found!${NC}"
    exit 1
fi
