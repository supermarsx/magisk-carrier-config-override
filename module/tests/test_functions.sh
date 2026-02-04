#!/usr/bin/env bash
###############################################################################
# Test: Functions Library
# Validates common/functions.sh
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

test_functions() {
    print_header "Testing Function Definitions"

    print_test "Checking functions.sh exists..."
    if [ -f "$MODULE_DIR/common/functions.sh" ]; then
        pass_test "functions.sh exists"
    else
        fail_test "functions.sh not found"
        return 1
    fi

    print_test "Counting function definitions..."
    local func_count=$(grep -cE "^[a-z_]+\(\)" "$MODULE_DIR/common/functions.sh" 2>/dev/null || echo 0)
    if [ "$func_count" -gt 0 ]; then
        pass_test "Found $func_count function(s) in functions.sh"
    else
        warn_test "No functions found in functions.sh"
    fi

    print_test "Checking function documentation..."
    if grep -qE "^#.*function|^# " "$MODULE_DIR/common/functions.sh"; then
        pass_test "functions.sh includes documentation"
    else
        warn_test "Consider adding function documentation"
    fi

    print_test "Checking if functions are sourced..."
    for script in install.sh service.sh uninstall.sh; do
        if [ -f "$MODULE_DIR/$script" ]; then
            if grep -qE "\.|source.*functions\.sh" "$MODULE_DIR/$script"; then
                pass_test "$script sources functions.sh"
            else
                warn_test "$script may not source functions.sh"
            fi
        fi
    done

    print_test "Checking for function name collisions..."
    local func_names=$(grep -oE "^[a-z_]+\(\)" "$MODULE_DIR/common/functions.sh" | sed 's/()//' | sort)
    local unique_names=$(echo "$func_names" | uniq)
    if [ "$(echo "$func_names" | wc -l)" -eq "$(echo "$unique_names" | wc -l)" ]; then
        pass_test "No duplicate function names"
    else
        fail_test "Duplicate function names found"
    fi

    print_test "Checking function naming conventions..."
    if grep -qE "^[a-z_]+\(\)" "$MODULE_DIR/common/functions.sh"; then
        if ! grep -E "^[A-Z]+\(\)" "$MODULE_DIR/common/functions.sh" >/dev/null 2>&1; then
            pass_test "Functions use lowercase naming convention"
        else
            warn_test "Some functions use uppercase names"
        fi
    fi
}

test_functions
print_test_summary "Functions Library Tests"
exit $?
