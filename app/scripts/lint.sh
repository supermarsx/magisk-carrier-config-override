#!/bin/bash
# Linting and formatting for CCO app

set -e

cd "$(dirname "$0")/.."

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

MODE="${1:-check}"

case "$MODE" in
    check)
        echo "🔍 Checking code style..."
        
        # Check Kotlin code style
        if command -v ktlint &> /dev/null; then
            echo "Running ktlint..."
            ktlint "app/src/**/*.kt" || {
                echo -e "${RED}✗ ktlint found issues${NC}"
                echo "Run './scripts/lint.sh fix' to auto-fix"
                exit 1
            }
        else
            echo -e "${YELLOW}⚠ ktlint not installed, skipping${NC}"
        fi
        
        echo -e "${GREEN}✓ Code style check passed${NC}"
        ;;
        
    fix)
        echo "🔧 Fixing code style..."
        
        if command -v ktlint &> /dev/null; then
            ktlint -F "app/src/**/*.kt"
            echo -e "${GREEN}✓ Code style fixed${NC}"
        else
            echo -e "${RED}✗ ktlint not installed${NC}"
            echo "Install: brew install ktlint"
            exit 1
        fi
        ;;
        
    *)
        echo "Usage: ./scripts/lint.sh [check|fix]"
        echo "  check - Check code style (default)"
        echo "  fix   - Auto-fix code style issues"
        exit 1
        ;;
esac
