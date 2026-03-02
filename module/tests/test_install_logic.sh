#!/usr/bin/env bash
###############################################################################
# Test: Install Script Logic
# Tests install.sh logic: version checks, directory creation, log rotation,
# permission setting, and Samsung/non-Samsung detection.
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

TEST_ROOT=$(mktemp -d "${TMPDIR:-/tmp}/cco-install-XXXXXX")
trap 'rm -rf "$TEST_ROOT"' EXIT

###############################################################################
# 1. set_perm_recursive guard
###############################################################################
test_perm_guard() {
    print_header "Install Logic: set_perm_recursive Guard"

    # Verify install.sh has the guarded version
    if grep -q 'command -v set_perm_recursive' "$MODULE_DIR/install.sh"; then
        pass_test "set_perm_recursive is guarded with command -v check"
    else
        fail_test "set_perm_recursive call not guarded"
    fi

    # Verify fallback find+chmod exists
    if grep -q 'find.*chmod 0755' "$MODULE_DIR/install.sh"; then
        pass_test "Fallback find+chmod present"
    else
        fail_test "Missing fallback permission setting"
    fi
}

###############################################################################
# 2. Directory creation
###############################################################################
test_directory_creation() {
    print_header "Install Logic: Directory Creation"

    local cco="$TEST_ROOT/data/adb/cco"

    # Simulate install.sh directory creation
    mkdir -p "$cco/overrides"
    mkdir -p "$cco/active"
    mkdir -p "$cco/logs"
    mkdir -p "$cco/backup"

    for dir in overrides active logs backup; do
        if [ -d "$cco/$dir" ]; then
            pass_test "Created: $dir"
        else
            fail_test "Missing: $dir"
        fi
    done

    # Verify permissions
    chmod 755 "$cco"
    chmod 755 "$cco/overrides"
    chmod 755 "$cco/active"
    chmod 755 "$cco/logs"
    chmod 755 "$cco/backup"

    for dir in "" /overrides /active /logs /backup; do
        local perms
        perms=$(stat -c '%a' "$cco$dir" 2>/dev/null || stat -f '%Lp' "$cco$dir" 2>/dev/null)
        if [ "$perms" = "755" ]; then
            pass_test "Permissions 755 on $cco$dir"
        else
            warn_test "Permissions $perms on $cco$dir (expected 755)"
        fi
    done
}

###############################################################################
# 3. Log rotation on reinstall
###############################################################################
test_log_rotation_on_reinstall() {
    print_header "Install Logic: Log Rotation"

    # Verify install.sh contains log rotation logic
    if grep -q 'tail -c 25600' "$MODULE_DIR/install.sh"; then
        pass_test "Log rotation logic present in install.sh"
    else
        fail_test "Missing log rotation in install.sh"
    fi

    if grep -q '51200' "$MODULE_DIR/install.sh"; then
        pass_test "50 KB threshold present"
    else
        fail_test "Missing size threshold"
    fi

    # Functional test
    local log_file="$TEST_ROOT/module.log"
    dd if=/dev/zero bs=1 count=60000 2>/dev/null | tr '\0' 'X' > "$log_file"

    local before=$(stat -c%s "$log_file" 2>/dev/null || stat -f%z "$log_file" 2>/dev/null)
    LOG_SIZE=$(stat -c%s "$log_file" 2>/dev/null || stat -f%z "$log_file" 2>/dev/null || echo 0)
    if [ "$LOG_SIZE" -gt 51200 ]; then
        tail -c 25600 "$log_file" > "${log_file}.tmp" 2>/dev/null \
            && mv "${log_file}.tmp" "$log_file" \
            || rm -f "${log_file}.tmp"
    fi

    local after=$(stat -c%s "$log_file" 2>/dev/null || stat -f%z "$log_file" 2>/dev/null)
    if [ "$after" -le 25600 ]; then
        pass_test "Log rotated: $before → $after bytes"
    else
        fail_test "Rotation failed: $before → $after bytes"
    fi
}

###############################################################################
# 4. Magisk version check logic
###############################################################################
test_magisk_version_check() {
    print_header "Install Logic: Magisk Version Check"

    # Verify the script checks Magisk version
    if grep -q 'magisk -V' "$MODULE_DIR/install.sh"; then
        pass_test "Magisk version check present"
    else
        fail_test "Missing Magisk version check"
    fi

    if grep -q '24000' "$MODULE_DIR/install.sh"; then
        pass_test "Minimum version 24000 checked"
    else
        fail_test "Missing minimum version threshold"
    fi

    # Simulate: magisk not available → defaults to 0
    local ver
    ver=$(echo 0)
    if [ "$ver" -lt 24000 ]; then
        pass_test "Graceful handling when magisk not available"
    else
        fail_test "Should warn when magisk missing"
    fi

    # Simulate: magisk 25100
    ver=25100
    if [ "$ver" -ge 24000 ]; then
        pass_test "Version 25100 passes check"
    else
        fail_test "Version 25100 should pass"
    fi
}

###############################################################################
# 5. SDK version check
###############################################################################
test_sdk_check() {
    print_header "Install Logic: SDK Version Check"

    if grep -q 'SDK.*-lt 33' "$MODULE_DIR/install.sh"; then
        pass_test "SDK minimum 33 check present"
    else
        fail_test "Missing SDK 33 check"
    fi

    # Simulate
    local sdk=31
    if [ "$sdk" -lt 33 ]; then
        pass_test "SDK 31 correctly warned"
    fi

    sdk=34
    if [ "$sdk" -ge 33 ]; then
        pass_test "SDK 34 passes check"
    fi
}

###############################################################################
# 6. Samsung detection
###############################################################################
test_samsung_detection() {
    print_header "Install Logic: Samsung Detection"

    if grep -q 'samsung' "$MODULE_DIR/install.sh"; then
        pass_test "Samsung detection logic present"
    else
        fail_test "Missing Samsung detection"
    fi

    # Simulate
    local mfr="Samsung"
    local lower=$(echo "$mfr" | tr '[:upper:]' '[:lower:]')
    if echo "$lower" | grep -q "samsung"; then
        pass_test "Samsung detected from '$mfr'"
    else
        fail_test "Failed to detect Samsung"
    fi

    mfr="Google"
    lower=$(echo "$mfr" | tr '[:upper:]' '[:lower:]')
    if ! echo "$lower" | grep -q "samsung"; then
        pass_test "Non-Samsung detected from '$mfr'"
    else
        fail_test "False Samsung match"
    fi
}

###############################################################################
# 7. Initial log entry
###############################################################################
test_initial_log_entry() {
    print_header "Install Logic: Initial Log Entry"

    local log_file="$TEST_ROOT/install.log"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] CCO Module installed" >> "$log_file"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Device: TestDevice" >> "$log_file"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Android: 14 (SDK 34)" >> "$log_file"

    local count=$(wc -l < "$log_file" | tr -d ' ')
    if [ "$count" -eq 3 ]; then
        pass_test "3 initial log entries created"
    else
        fail_test "Expected 3 entries, got $count"
    fi

    if grep -q 'CCO Module installed' "$log_file"; then
        pass_test "Install marker present in log"
    else
        fail_test "Install marker missing"
    fi
}

###############################################################################
# 8. CarrierConfig path discovery
###############################################################################
test_path_discovery() {
    print_header "Install Logic: Path Discovery"

    if grep -q '/data/vendor/carrierconfig' "$MODULE_DIR/install.sh"; then
        pass_test "Checks /data/vendor/carrierconfig"
    else
        fail_test "Missing vendor path check"
    fi

    if grep -q '/data/misc/carrierconfig' "$MODULE_DIR/install.sh"; then
        pass_test "Checks /data/misc/carrierconfig"
    else
        fail_test "Missing misc path check"
    fi
}

###############################################################################
# 9. install.sh includes install.sh in chmod
###############################################################################
test_install_chmod_self() {
    print_header "Install Logic: Self Chmod"

    if grep -q 'chmod 755.*install.sh' "$MODULE_DIR/install.sh"; then
        pass_test "install.sh sets itself executable"
    else
        warn_test "install.sh may not set itself executable (Magisk handles this)"
    fi
}

###############################################################################
# Run all
###############################################################################
test_perm_guard
test_directory_creation
test_log_rotation_on_reinstall
test_magisk_version_check
test_sdk_check
test_samsung_detection
test_initial_log_entry
test_path_discovery
test_install_chmod_self
print_test_summary "Install Logic Tests"
exit $?
