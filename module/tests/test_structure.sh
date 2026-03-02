#!/usr/bin/env bash
###############################################################################
# Test: Module Structure
# Validates required files and directory structure
###############################################################################

# Source common functions
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

test_module_structure() {
    print_header "Testing Module Structure"

    print_test "Checking required files exist..."
    local required_files=(
        "module.prop"
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
        "README.md"
    )

    for file in "${required_files[@]}"; do
        if [ -f "$MODULE_DIR/$file" ]; then
            pass_test "Required file exists: $file"
        else
            fail_test "Missing required file: $file"
        fi
    done

    print_test "Checking optional but recommended files..."
    local optional_files=(
        "system.prop"
        "docs/CHANGELOG.md"
        "common/functions.sh"
        "update.json"
    )

    for file in "${optional_files[@]}"; do
        if [ -f "$MODULE_DIR/$file" ]; then
            pass_test "Optional file exists: $file"
        else
            warn_test "Missing optional file: $file"
        fi
    done

    print_test "Checking profiles directory..."
    if [ -d "$MODULE_DIR/profiles" ]; then
        pass_test "Profiles directory exists"
        local profile_count=$(find "$MODULE_DIR/profiles" -name "*.xml" -type f | wc -l | tr -d ' ')
        pass_test "Found $profile_count profile(s)"
    else
        fail_test "Profiles directory missing"
    fi

    print_test "Checking docs directory..."
    if [ -d "$MODULE_DIR/docs" ]; then
        pass_test "Documentation directory exists"
    else
        warn_test "Documentation directory missing"
    fi

    print_test "Checking common directory..."
    if [ -d "$MODULE_DIR/common" ]; then
        pass_test "Common utilities directory exists"
    else
        warn_test "Common directory missing"
    fi
}

# Run tests
test_module_structure
print_test_summary "Module Structure Tests"
exit $?
