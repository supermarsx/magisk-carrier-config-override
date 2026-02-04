#!/usr/bin/env bash
###############################################################################
# CCO Module Test Script (Wrapper)
# Runs the modular test suite in tests/
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
TESTS_DIR="$MODULE_DIR/tests"

# Check if new test system exists
if [ -f "$TESTS_DIR/run_tests.sh" ]; then
    exec "$TESTS_DIR/run_tests.sh" "$@"
else
    echo "Error: Modular test suite not found at $TESTS_DIR"
    exit 1
fi
