#!/usr/bin/env bash
###############################################################################
# Test: Script Syntax
# Validates shell script syntax
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

test_script_syntax() {
    print_header "Testing Script Syntax"

    print_test "Checking shell script syntax..."
    local scripts=(
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
        "common/functions.sh"
    )

    for script in "${scripts[@]}"; do
        if [ -f "$MODULE_DIR/$script" ]; then
            if bash -n "$MODULE_DIR/$script" 2>/dev/null; then
                pass_test "Syntax valid: $script"
            else
                fail_test "Syntax error in: $script"
                bash -n "$MODULE_DIR/$script" 2>&1 | sed 's/^/  /'
            fi
        fi
    done

    print_test "Checking shebang lines..."
    for script in "${scripts[@]}"; do
        if [ -f "$MODULE_DIR/$script" ]; then
            local shebang=$(head -n 1 "$MODULE_DIR/$script")
            if [[ "$shebang" =~ ^#!/ ]]; then
                pass_test "$script has valid shebang: $shebang"
            else
                warn_test "$script missing or invalid shebang"
            fi
        fi
    done
}

test_script_syntax
print_test_summary "Script Syntax Tests"
exit $?
