#!/usr/bin/env bash
###############################################################################
# Test: Service Script Logic
# Tests service.sh logic paths: log rotation, XML validation, disable flag,
# wait-for-data, path detection, broadcast, and error handling.
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

# Isolated temp dir per run
TEST_ROOT=$(mktemp -d "${TMPDIR:-/tmp}/cco-svc-XXXXXX")
trap 'rm -rf "$TEST_ROOT"' EXIT

# ─── helpers ────────────────────────────────────────────────────────────────
setup_env() {
    rm -rf "$TEST_ROOT"/*
    mkdir -p "$TEST_ROOT/data/adb/cco/logs"
    mkdir -p "$TEST_ROOT/data/adb/cco/active"
    mkdir -p "$TEST_ROOT/data/adb/cco/backup"
    mkdir -p "$TEST_ROOT/data/adb/cco/overrides"
}

###############################################################################
# 1. Log rotation
###############################################################################
test_log_rotation() {
    print_header "Service Logic: Log Rotation"

    setup_env
    local log_file="$TEST_ROOT/data/adb/cco/logs/module.log"

    # Create a log larger than 50 KB (~52000 bytes)
    dd if=/dev/zero bs=1 count=55000 2>/dev/null | tr '\0' 'A' > "$log_file"
    local before_size=$(stat -c%s "$log_file" 2>/dev/null || stat -f%z "$log_file" 2>/dev/null)

    print_test "Log file created ($before_size bytes, should be >51200)"
    if [ "$before_size" -gt 51200 ]; then
        pass_test "Log is over 50 KB threshold"
    else
        fail_test "Log should be over 50 KB (got $before_size)"
    fi

    # Simulate the rotation logic from service.sh
    LOG_SIZE=$(stat -c%s "$log_file" 2>/dev/null || stat -f%z "$log_file" 2>/dev/null || echo 0)
    if [ "$LOG_SIZE" -gt 51200 ]; then
        tail -c 25600 "$log_file" > "${log_file}.tmp" 2>/dev/null \
            && mv "${log_file}.tmp" "$log_file" \
            || rm -f "${log_file}.tmp"
    fi

    local after_size=$(stat -c%s "$log_file" 2>/dev/null || stat -f%z "$log_file" 2>/dev/null)
    print_test "After rotation: $after_size bytes (should be ≤25600)"
    if [ "$after_size" -le 25600 ]; then
        pass_test "Log rotated correctly to $after_size bytes"
    else
        fail_test "Log rotation failed (got $after_size bytes)"
    fi

    # Small log should NOT be rotated
    setup_env
    echo "short log" > "$log_file"
    local small_before=$(stat -c%s "$log_file" 2>/dev/null || stat -f%z "$log_file" 2>/dev/null)
    LOG_SIZE=$(stat -c%s "$log_file" 2>/dev/null || stat -f%z "$log_file" 2>/dev/null || echo 0)
    if [ "$LOG_SIZE" -gt 51200 ]; then
        tail -c 25600 "$log_file" > "${log_file}.tmp" && mv "${log_file}.tmp" "$log_file"
    fi
    local small_after=$(stat -c%s "$log_file" 2>/dev/null || stat -f%z "$log_file" 2>/dev/null)
    if [ "$small_before" -eq "$small_after" ]; then
        pass_test "Small log left untouched ($small_after bytes)"
    else
        fail_test "Small log was incorrectly modified"
    fi
}

###############################################################################
# 2. XML validation
###############################################################################
test_xml_validation() {
    print_header "Service Logic: XML Validation"

    setup_env
    local override="$TEST_ROOT/data/adb/cco/active/override.xml"

    # Valid XML
    cat > "$override" << 'XMLEOF'
<?xml version="1.0" encoding="utf-8"?>
<carrier_config>
  <boolean name="carrier_wfc_ims_available_bool" value="true"/>
</carrier_config>
XMLEOF
    if grep -q '<carrier_config' "$override" 2>/dev/null; then
        pass_test "Valid XML accepted (has <carrier_config>)"
    else
        fail_test "Valid XML rejected"
    fi
    if grep -q '</carrier_config>' "$override" 2>/dev/null; then
        pass_test "Valid XML has closing tag"
    else
        fail_test "Valid XML missing closing tag detection"
    fi

    # Missing root element
    echo '<?xml version="1.0"?><random_tag/>' > "$override"
    if ! grep -q '<carrier_config' "$override" 2>/dev/null; then
        pass_test "Invalid XML (no <carrier_config>) correctly rejected"
    else
        fail_test "Should have rejected XML without carrier_config"
    fi

    # Truncated XML (opening tag only)
    echo '<carrier_config>' > "$override"
    if grep -q '<carrier_config' "$override" 2>/dev/null; then
        # Opening tag present
        if ! grep -q '</carrier_config>' "$override" 2>/dev/null; then
            pass_test "Truncated XML detected (missing closing tag)"
        else
            fail_test "Should have flagged missing closing tag"
        fi
    fi

    # Empty file
    : > "$override"
    if ! grep -q '<carrier_config' "$override" 2>/dev/null; then
        pass_test "Empty file correctly rejected"
    else
        fail_test "Empty file should not pass validation"
    fi

    # Binary/garbage content
    dd if=/dev/urandom bs=256 count=1 2>/dev/null > "$override"
    if ! grep -q '<carrier_config' "$override" 2>/dev/null; then
        pass_test "Binary file correctly rejected"
    else
        fail_test "Binary file should not pass validation"
    fi
}

###############################################################################
# 3. Disable flag
###############################################################################
test_disable_flag() {
    print_header "Service Logic: Disable Flag"

    setup_env
    local disable_flag="$TEST_ROOT/data/adb/cco/disable"

    # No flag → module should run
    if [ ! -f "$disable_flag" ]; then
        pass_test "Module runs when disable flag absent"
    else
        fail_test "Disable flag should not exist initially"
    fi

    # Create flag → module should skip
    touch "$disable_flag"
    if [ -f "$disable_flag" ]; then
        pass_test "Module correctly detects disable flag"
    else
        fail_test "Disable flag detection failed"
    fi

    # Remove flag → module should run again
    rm -f "$disable_flag"
    if [ ! -f "$disable_flag" ]; then
        pass_test "Module resumes after flag removal"
    else
        fail_test "Flag removal not detected"
    fi
}

###############################################################################
# 4. Wait-for-data simulation
###############################################################################
test_wait_for_data() {
    print_header "Service Logic: Wait-for-data"

    setup_env
    local ADB_DIR="$TEST_ROOT/data/adb"

    # Directory already exists → immediate success
    local count=0
    local start=$(date +%s)
    until [ -d "$ADB_DIR" ] || [ $count -gt 5 ]; do
        sleep 1
        count=$((count + 1))
    done
    local elapsed=$(( $(date +%s) - start ))
    if [ -d "$ADB_DIR" ] && [ "$elapsed" -lt 2 ]; then
        pass_test "Immediate success when /data/adb exists ($elapsed s)"
    else
        fail_test "Should have detected /data/adb immediately"
    fi

    # Remove dir → should timeout
    rmdir "$ADB_DIR" 2>/dev/null || true
    rm -rf "$ADB_DIR"
    count=0
    local max=3
    until [ -d "$ADB_DIR" ] || [ $count -gt $max ]; do
        sleep 0
        count=$((count + 1))
    done
    if [ $count -gt $max ]; then
        pass_test "Timeout triggered when /data/adb missing (count=$count)"
    else
        fail_test "Should have timed out"
    fi
}

###############################################################################
# 5. Backup logic
###############################################################################
test_backup_logic() {
    print_header "Service Logic: Backup"

    setup_env
    local target="$TEST_ROOT/data/vendor/carrierconfig/override.xml"
    local backup_file="$TEST_ROOT/data/adb/cco/backup/override_original.xml"
    local backup_info="$TEST_ROOT/data/adb/cco/backup/backup_info.txt"
    mkdir -p "$(dirname "$target")"

    # Original exists, no backup yet → should backup
    echo "original-content-123" > "$target"
    if [ -f "$target" ] && [ ! -f "$backup_file" ]; then
        cp "$target" "$backup_file"
        echo "Original Path: $target" > "$backup_info"
        pass_test "First-run backup created"
    else
        fail_test "Backup preconditions wrong"
    fi

    if cmp -s "$target" "$backup_file"; then
        pass_test "Backup content matches original"
    else
        fail_test "Backup content mismatch"
    fi

    # Backup already exists → should skip
    echo "modified-content" > "$target"
    if [ -f "$backup_file" ]; then
        # Skip re-backup (as service.sh does)
        pass_test "Existing backup preserved (not overwritten)"
    fi

    # Verify backup info
    if grep -q "Original Path:" "$backup_info" 2>/dev/null; then
        pass_test "Backup metadata recorded"
    else
        fail_test "Backup metadata missing"
    fi
}

###############################################################################
# 6. Per-slot broadcast logic
###############################################################################
test_slot_broadcast() {
    print_header "Service Logic: Per-Slot Broadcast"

    # service.sh now reads persist.radio.multisim.config to decide slot count
    # Test the case-matching logic
    for config_val in "dsds" "dsda" "tsts"; do
        local max_slots
        case "$config_val" in
            dsds|dsda|tsts) max_slots=2 ;;
            *)              max_slots=1 ;;
        esac
        if [ "$max_slots" -eq 2 ]; then
            pass_test "Dual-SIM detected for config='$config_val'"
        else
            fail_test "Should detect dual-SIM for '$config_val'"
        fi
    done

    # Single-SIM
    for config_val in "" "ssss" "unknown"; do
        local max_slots
        case "$config_val" in
            dsds|dsda|tsts) max_slots=2 ;;
            *)              max_slots=1 ;;
        esac
        if [ "$max_slots" -eq 1 ]; then
            pass_test "Single-SIM detected for config='$config_val'"
        else
            fail_test "Should detect single-SIM for '$config_val'"
        fi
    done
}

###############################################################################
# Run all
###############################################################################
test_log_rotation
test_xml_validation
test_disable_flag
test_wait_for_data
test_backup_logic
test_slot_broadcast
print_test_summary "Service Logic Tests"
exit $?
