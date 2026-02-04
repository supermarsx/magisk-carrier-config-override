#!/usr/bin/env bash
###############################################################################
# Test: Security
# Validates security aspects of scripts
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

test_security() {
    print_header "Testing Security"

    print_test "Checking for hardcoded credentials..."
    local cred_patterns="password|passwd|secret|token|api_key|apikey"
    local found_creds=0
    for script in install.sh post-fs-data.sh service.sh uninstall.sh common/functions.sh; do
        if [ -f "$MODULE_DIR/$script" ]; then
            if grep -iE "$cred_patterns" "$MODULE_DIR/$script" | grep -v "^\s*#" | grep -vE "PASSWORD|user.*password" >/dev/null 2>&1; then
                found_creds=1
            fi
        fi
    done
    if [ $found_creds -eq 0 ]; then
        pass_test "No hardcoded credentials found"
    else
        warn_test "Potential credential references found (review manually)"
    fi

    print_test "Checking file permission settings..."
    local has_secure_perms=0
    for script in install.sh post-fs-data.sh service.sh; do
        if [ -f "$MODULE_DIR/$script" ]; then
            if grep -qE "chmod (644|755|700|600)" "$MODULE_DIR/$script"; then
                has_secure_perms=1
            fi
        fi
    done
    if [ $has_secure_perms -eq 1 ]; then
        pass_test "Scripts set secure file permissions"
    else
        warn_test "Consider explicit permission settings"
    fi

    print_test "Checking for world-writable operations..."
    local world_writable=0
    for script in install.sh post-fs-data.sh service.sh uninstall.sh; do
        if [ -f "$MODULE_DIR/$script" ]; then
            if grep -E "chmod.*777|chmod.*666" "$MODULE_DIR/$script" | grep -v "^\s*#" >/dev/null 2>&1; then
                world_writable=1
                warn_test "$script may create world-writable files"
                break
            fi
        fi
    done
    if [ $world_writable -eq 0 ]; then
        pass_test "No world-writable permissions found"
    fi

    print_test "Checking for SELinux context handling..."
    if grep -qE "chcon|selinux|restorecon" "$MODULE_DIR/service.sh" 2>/dev/null; then
        pass_test "service.sh handles SELinux contexts"
    else
        warn_test "Consider adding SELinux context handling"
    fi

    print_test "Checking for input validation..."
    local has_validation=0
    for script in install.sh service.sh common/functions.sh; do
        if [ -f "$MODULE_DIR/$script" ]; then
            if grep -qE "if \[\[? -[nz] .*\]\]?|if \[\[? -[fd] .*\]\]?" "$MODULE_DIR/$script"; then
                has_validation=1
            fi
        fi
    done
    if [ $has_validation -eq 1 ]; then
        pass_test "Scripts perform input validation"
    else
        warn_test "Consider adding more input validation"
    fi

    print_test "Checking for command injection vulnerabilities..."
    local safe=1
    for script in install.sh service.sh uninstall.sh; do
        if [ -f "$MODULE_DIR/$script" ]; then
            # Check for unquoted eval or exec
            if grep -E "eval |exec " "$MODULE_DIR/$script" | grep -v "^\s*#" | grep -v '"' >/dev/null 2>&1; then
                warn_test "$script may have command injection risk"
                safe=0
            fi
        fi
    done
    if [ $safe -eq 1 ]; then
        pass_test "No obvious command injection vulnerabilities"
    fi
}

test_security
print_test_summary "Security Tests"
exit $?
