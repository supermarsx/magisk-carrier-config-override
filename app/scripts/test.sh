#!/bin/bash
# Test runner for CCO app

set -e

echo "🧪 Running CCO App Tests..."

cd "$(dirname "$0")/.."

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Run unit tests
echo "📝 Running unit tests..."
if ./gradlew test --quiet; then
    echo -e "${GREEN}✓ Unit tests passed${NC}"
else
    echo -e "${RED}✗ Unit tests failed${NC}"
    exit 1
fi

# Check if device is connected for instrumentation tests
if adb devices | grep -q "device$"; then
    echo ""
    read -p "📱 Run instrumentation tests on device? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🤖 Running instrumentation tests..."
        if ./gradlew connectedAndroidTest; then
            echo -e "${GREEN}✓ Instrumentation tests passed${NC}"
        else
            echo -e "${RED}✗ Instrumentation tests failed${NC}"
            exit 1
        fi
    fi
else
    echo -e "${YELLOW}⚠ No device connected - skipping instrumentation tests${NC}"
fi

echo ""
echo -e "${GREEN}✅ All tests passed!${NC}"
