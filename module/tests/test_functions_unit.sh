#!/usr/bin/env bash
###############################################################################
# Test: Functions Library Unit Tests
# Directly exercises every function exported by common/functions.sh
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

TEST_ROOT=$(mktemp -d "${TMPDIR:-/tmp}/cco-functest-XXXXXX")
trap 'rm -rf "$TEST_ROOT"' EXIT

# Source functions under test with LOG_FILE redirected
export LOG_FILE="$TEST_ROOT/test.log"
mkdir -p "$TEST_ROOT"
source "$MODULE_DIR/common/functions.sh"

###############################################################################
# 1. log / log_info / log_warn / log_error
###############################################################################
test_logging_functions() {
    print_header "Functions: Logging"

    : > "$LOG_FILE"
    log_info "info message"
    if grep -q '\[INFO\] info message' "$LOG_FILE"; then
        pass_test "log_info writes INFO level"
    else
        fail_test "log_info output unexpected"
    fi

    log_warn "warning message"
    if grep -q '\[WARN\] warning message' "$LOG_FILE"; then
        pass_test "log_warn writes WARN level"
    else
        fail_test "log_warn output unexpected"
    fi

    log_error "error message"
    if grep -q '\[ERROR\] error message' "$LOG_FILE"; then
        pass_test "log_error writes ERROR level"
    else
        fail_test "log_error output unexpected"
    fi

    # Verify timestamp format [YYYY-MM-DD HH:MM:SS]
    if grep -qE '^\[[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}\]' "$LOG_FILE"; then
        pass_test "Timestamps follow ISO format"
    else
        fail_test "Timestamps malformed"
    fi

    # Verify three lines written
    local count=$(wc -l < "$LOG_FILE" | tr -d ' ')
    if [ "$count" -eq 3 ]; then
        pass_test "Exactly 3 log lines produced"
    else
        fail_test "Expected 3 log lines, got $count"
    fi
}

###############################################################################
# 2. detect_override_path
###############################################################################
test_detect_override_path() {
    print_header "Functions: detect_override_path"

    # The function checks real filesystem paths; mock by creating dirs in /tmp
    # Since the function hardcodes /data paths, we test the logic separately.

    # Test that function outputs a non-empty string
    local result
    result=$(detect_override_path)
    if [ -n "$result" ]; then
        pass_test "detect_override_path returns a path: $result"
    else
        fail_test "detect_override_path returned empty"
    fi

    # Default fallback should be /data/vendor/carrierconfig/override.xml
    # (since none of the candidate dirs exist on the test host)
    if [ "$result" = "/data/vendor/carrierconfig/override.xml" ]; then
        pass_test "Fallback path is correct default"
    else
        # Could be on a host where /data exists — just warn
        warn_test "Path differs from default fallback: $result (may be valid)"
    fi
}

###############################################################################
# 3. wait_for_path
###############################################################################
test_wait_for_path() {
    print_header "Functions: wait_for_path"

    local test_file="$TEST_ROOT/wait_target"

    # File exists → immediate success
    touch "$test_file"
    if wait_for_path "$test_file" 2; then
        pass_test "wait_for_path succeeds when file exists"
    else
        fail_test "Should succeed when file exists"
    fi

    # File does not exist → timeout
    rm -f "$test_file"
    if ! wait_for_path "$test_file" 1; then
        pass_test "wait_for_path times out when file missing"
    else
        fail_test "Should timeout when file missing"
    fi

    # Default timeout (30s is too long for test, just verify function accepts 1 arg)
    touch "$test_file"
    if wait_for_path "$test_file"; then
        pass_test "wait_for_path works with default timeout"
    else
        fail_test "Default timeout call failed"
    fi
}

###############################################################################
# 4. create_dir_with_context
###############################################################################
test_create_dir_with_context() {
    print_header "Functions: create_dir_with_context"

    local test_dir="$TEST_ROOT/context_dir/sub"

    create_dir_with_context "$test_dir"

    if [ -d "$test_dir" ]; then
        pass_test "Directory created: $test_dir"
    else
        fail_test "Directory not created"
    fi

    # Check permissions (755 octal)
    local perms
    perms=$(stat -c '%a' "$test_dir" 2>/dev/null || stat -f '%Lp' "$test_dir" 2>/dev/null)
    if [ "$perms" = "755" ]; then
        pass_test "Directory has 755 permissions"
    else
        warn_test "Directory permissions are $perms (expected 755, may differ by umask)"
    fi

    # Idempotent — calling again should not fail
    create_dir_with_context "$test_dir"
    if [ -d "$test_dir" ]; then
        pass_test "Idempotent call succeeds"
    else
        fail_test "Idempotent call broke directory"
    fi
}

###############################################################################
# 5. safe_bind_mount (partial — can't do real mounts without root)
###############################################################################
test_safe_bind_mount_validation() {
    print_header "Functions: safe_bind_mount (validation)"

    : > "$LOG_FILE"

    # Missing source → should fail
    safe_bind_mount "/nonexistent_source_file_xyz" "$TEST_ROOT/target" 2>/dev/null
    local rc=$?
    if [ $rc -ne 0 ]; then
        pass_test "Rejects missing source file (rc=$rc)"
    else
        fail_test "Should reject missing source"
    fi

    if grep -q 'ERROR.*Source file does not exist' "$LOG_FILE"; then
        pass_test "Error logged for missing source"
    else
        fail_test "Error message not logged"
    fi

    # Source exists, target dir does not → should create target dir
    local source="$TEST_ROOT/source.xml"
    local target="$TEST_ROOT/auto_dir/target.xml"
    echo "<carrier_config/>" > "$source"
    : > "$LOG_FILE"

    # mount will fail (no root), but dir should be created
    safe_bind_mount "$source" "$target" 2>/dev/null || true
    if [ -d "$(dirname "$target")" ]; then
        pass_test "Target directory auto-created"
    else
        fail_test "Target directory not created"
    fi
}

###############################################################################
# 6. is_module_disabled
###############################################################################
test_is_module_disabled() {
    print_header "Functions: is_module_disabled"

    # Without /data/adb/cco/disable → should return non-zero
    rm -f /data/adb/cco/disable 2>/dev/null || true
    if ! is_module_disabled 2>/dev/null; then
        pass_test "Module not disabled when flag absent"
    else
        warn_test "is_module_disabled returned 0 (disable flag may exist on host)"
    fi
}

###############################################################################
# 7. get_device_info
###############################################################################
test_get_device_info() {
    print_header "Functions: get_device_info"

    local info
    info=$(get_device_info 2>/dev/null)

    # On non-Android, getprop won't exist, so fields will be empty
    if echo "$info" | grep -q "Manufacturer:"; then
        pass_test "get_device_info outputs Manufacturer field"
    else
        fail_test "Missing Manufacturer field"
    fi

    if echo "$info" | grep -q "Model:"; then
        pass_test "get_device_info outputs Model field"
    else
        fail_test "Missing Model field"
    fi

    if echo "$info" | grep -q "Android:"; then
        pass_test "get_device_info outputs Android field"
    else
        fail_test "Missing Android field"
    fi

    if echo "$info" | grep -q "Build:"; then
        pass_test "get_device_info outputs Build field"
    else
        fail_test "Missing Build field"
    fi
}

###############################################################################
# 8. Color variables defined
###############################################################################
test_color_variables() {
    print_header "Functions: Color Variables"

    if [ -n "$RED" ] && [ -n "$GREEN" ] && [ -n "$YELLOW" ] && [ -n "$NC" ]; then
        pass_test "Color variables are defined"
    else
        fail_test "Some color variables missing"
    fi
}

###############################################################################
# Run all
###############################################################################
test_logging_functions
test_detect_override_path
test_wait_for_path
test_create_dir_with_context
test_safe_bind_mount_validation
test_is_module_disabled
test_get_device_info
test_color_variables
print_test_summary "Functions Unit Tests"
exit $?
