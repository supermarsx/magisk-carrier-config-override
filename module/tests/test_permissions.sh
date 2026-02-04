#!/usr/bin/env bash
###############################################################################
# Test: File Permissions
# Validates executable permissions on scripts
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

test_permissions() {
    print_header "Testing Script Permissions"

    print_test "Checking executable permissions..."
    local executable_scripts=(
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
    )

    for script in "${executable_scripts[@]}"; do
        if [ -f "$MODULE_DIR/$script" ]; then
            if [ -x "$MODULE_DIR/$script" ]; then
                pass_test "Executable: $script"
            else
                fail_test "Not executable: $script"
            fi
        fi
    done

    print_test "Checking non-executable files..."
    local non_executable=(
        "module.prop"
        "system.prop"
        "README.md"
    )

    for file in "${non_executable[@]}"; do
        if [ -f "$MODULE_DIR/$file" ]; then
            if [ ! -x "$MODULE_DIR/$file" ]; then
                pass_test "Correctly non-executable: $file"
            else
                warn_test "Should not be executable: $file"
            fi
        fi
    done
}

test_permissions
print_test_summary "Permission Tests"
exit $?
