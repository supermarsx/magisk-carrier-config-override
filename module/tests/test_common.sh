#!/usr/bin/env bash
###############################################################################
# CCO Module Test Framework - Common Functions
# Shared utilities for all test scripts
###############################################################################

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counters
PASS_COUNT=0
FAIL_COUNT=0
WARN_COUNT=0
TEST_RESULTS=()

# Module directory (can be overridden)
MODULE_DIR="${MODULE_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)}"

# Print colored header
print_header() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

# Print test info
print_test() {
    echo -e "${BLUE}[TEST]${NC} $1"
}

# Pass test
pass_test() {
    echo -e "${GREEN}[PASS]${NC} $1"
    PASS_COUNT=$((PASS_COUNT + 1))
    TEST_RESULTS+=("PASS: $1")
}

# Fail test
fail_test() {
    echo -e "${RED}[FAIL]${NC} $1"
    FAIL_COUNT=$((FAIL_COUNT + 1))
    TEST_RESULTS+=("FAIL: $1")
}

# Warn test
warn_test() {
    echo -e "${YELLOW}[WARN]${NC} $1"
    WARN_COUNT=$((WARN_COUNT + 1))
    TEST_RESULTS+=("WARN: $1")
}

# Print summary for this test file
print_test_summary() {
    local test_name="$1"
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$test_name Summary${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo -e "${GREEN}Passed:${NC} $PASS_COUNT"
    echo -e "${RED}Failed:${NC} $FAIL_COUNT"
    echo -e "${YELLOW}Warnings:${NC} $WARN_COUNT"
    
    if [ $FAIL_COUNT -eq 0 ]; then
        echo -e "${GREEN}✓ All tests passed${NC}"
        return 0
    else
        echo -e "${RED}✗ Some tests failed${NC}"
        return 1
    fi
}

# Export functions and variables
export -f print_header print_test pass_test fail_test warn_test print_test_summary
export RED GREEN YELLOW BLUE NC
export PASS_COUNT FAIL_COUNT WARN_COUNT MODULE_DIR
