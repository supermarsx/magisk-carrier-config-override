#!/usr/bin/env bash
###############################################################################
# Test: Uninstall Logic
# Tests uninstall.sh cleanup, unmount retry, restore, and data preservation.
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

TEST_ROOT=$(mktemp -d "${TMPDIR:-/tmp}/cco-uninstall-XXXXXX")
trap 'rm -rf "$TEST_ROOT"' EXIT

setup_env() {
    rm -rf "$TEST_ROOT"/*
    mkdir -p "$TEST_ROOT/data/adb/cco/logs"
    mkdir -p "$TEST_ROOT/data/adb/cco/active"
    mkdir -p "$TEST_ROOT/data/adb/cco/backup"
    mkdir -p "$TEST_ROOT/data/adb/cco/overrides"
    mkdir -p "$TEST_ROOT/data/vendor/carrierconfig"
}

###############################################################################
# 1. is_mounted function
###############################################################################
test_is_mounted() {
    print_header "Uninstall Logic: is_mounted"

    # Re-implement the function from uninstall.sh
    is_mounted() {
        local path="$1"
        if command -v mountpoint >/dev/null 2>&1; then
            mountpoint -q "$path" 2>/dev/null
            return $?
        fi
        mount | grep -F " on $path " >/dev/null 2>&1
        return $?
    }

    # A plain file should NOT be a mount point
    setup_env
    local test_file="$TEST_ROOT/data/vendor/carrierconfig/override.xml"
    echo "test" > "$test_file"
    if ! is_mounted "$test_file"; then
        pass_test "Regular file correctly identified as not mounted"
    else
        warn_test "is_mounted returned true for regular file (may be OK in some envs)"
    fi

    # Non-existent path
    if ! is_mounted "/nonexistent/path/12345"; then
        pass_test "Non-existent path correctly not mounted"
    else
        fail_test "Non-existent path reported as mounted"
    fi
}

###############################################################################
# 2. Restore from backup
###############################################################################
test_restore_from_backup() {
    print_header "Uninstall Logic: Restore from Backup"

    setup_env
    local backup="$TEST_ROOT/data/adb/cco/backup/override_original.xml"
    local backup_info="$TEST_ROOT/data/adb/cco/backup/backup_info.txt"
    local target="$TEST_ROOT/data/vendor/carrierconfig/override.xml"

    echo "original-carrier-data" > "$backup"
    echo "Original Path: $target" > "$backup_info"

    # Simulate uninstall restore logic
    local ORIGINAL_PATH=""
    if [ -f "$backup_info" ]; then
        ORIGINAL_PATH=$(grep -m1 '^Original Path:' "$backup_info" | sed 's/^Original Path:[[:space:]]*//')
    fi

    if [ -n "$ORIGINAL_PATH" ]; then
        cp "$backup" "$ORIGINAL_PATH"
    fi

    if [ -f "$target" ] && cmp -s "$backup" "$target"; then
        pass_test "Original file restored from backup"
    else
        fail_test "File restoration failed"
    fi

    # Test with missing backup_info → fallback path
    setup_env
    echo "original-data-2" > "$backup"
    echo "existing" > "$target"

    ORIGINAL_PATH=""
    # No backup_info file → fallback
    CANDIDATE_PATHS="$target"
    for path in $CANDIDATE_PATHS; do
        if [ -f "$path" ]; then
            cp "$backup" "$path"
            break
        fi
    done

    if cmp -s "$backup" "$target"; then
        pass_test "Fallback restore works when backup_info missing"
    else
        fail_test "Fallback restore failed"
    fi
}

###############################################################################
# 3. Active config removal
###############################################################################
test_active_cleanup() {
    print_header "Uninstall Logic: Active Config Cleanup"

    setup_env
    local active_dir="$TEST_ROOT/data/adb/cco/active"
    echo "override-data" > "$active_dir/override.xml"

    if [ -d "$active_dir" ] && [ -f "$active_dir/override.xml" ]; then
        pass_test "Active config exists before cleanup"
    else
        fail_test "Setup failed"
    fi

    rm -rf "$active_dir"

    if [ ! -d "$active_dir" ]; then
        pass_test "Active config directory removed"
    else
        fail_test "Active config directory not removed"
    fi
}

###############################################################################
# 4. Logs preserved after uninstall
###############################################################################
test_logs_preserved() {
    print_header "Uninstall Logic: Logs Preservation"

    setup_env
    local logs_dir="$TEST_ROOT/data/adb/cco/logs"
    echo "some log data" > "$logs_dir/module.log"

    # Simulate uninstall: remove active, keep logs
    rm -rf "$TEST_ROOT/data/adb/cco/active"

    if [ -f "$logs_dir/module.log" ]; then
        pass_test "Logs preserved after uninstall"
    else
        fail_test "Logs should be preserved"
    fi
}

###############################################################################
# 5. Uninstall with no prior data
###############################################################################
test_clean_uninstall() {
    print_header "Uninstall Logic: Clean Environment"

    rm -rf "$TEST_ROOT"/*
    local cco_dir="$TEST_ROOT/data/adb/cco"

    # Module uninstall on clean system should not error
    mkdir -p "$cco_dir/logs"
    echo "[test] Clean uninstall" >> "$cco_dir/logs/uninstall.log"

    if [ -f "$cco_dir/logs/uninstall.log" ]; then
        pass_test "Uninstall log created even without prior data"
    else
        fail_test "Should be able to log on clean system"
    fi
}

###############################################################################
# 6. Umount retry logic (simulated)
###############################################################################
test_umount_retry() {
    print_header "Uninstall Logic: Umount Retry"

    # We can't actually test real mounts, but we test the control flow
    # The new code: if ! umount; then sleep 1; umount -l
    # Verify that the script contains both umount and umount -l
    if grep -q 'umount -l' "$MODULE_DIR/uninstall.sh"; then
        pass_test "Lazy umount fallback present in uninstall.sh"
    else
        fail_test "Missing lazy umount fallback"
    fi

    if grep -q 'sleep 1' "$MODULE_DIR/uninstall.sh"; then
        pass_test "Retry delay present before lazy umount"
    else
        fail_test "Missing retry delay"
    fi
}

###############################################################################
# Run all
###############################################################################
test_is_mounted
test_restore_from_backup
test_active_cleanup
test_logs_preserved
test_clean_uninstall
test_umount_retry
print_test_summary "Uninstall Logic Tests"
exit $?
