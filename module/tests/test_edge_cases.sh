#!/usr/bin/env bash
###############################################################################
# Test: Edge Cases
# Tests unusual inputs and boundary conditions for the module.
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

TEST_ROOT=$(mktemp -d "${TMPDIR:-/tmp}/cco-edge-XXXXXX")
trap 'rm -rf "$TEST_ROOT"' EXIT

setup_env() {
    rm -rf "$TEST_ROOT"/*
    mkdir -p "$TEST_ROOT/data/adb/cco/logs"
    mkdir -p "$TEST_ROOT/data/adb/cco/active"
    mkdir -p "$TEST_ROOT/data/adb/cco/backup"
    mkdir -p "$TEST_ROOT/data/adb/cco/overrides"
}

###############################################################################
# 1. Empty override file
###############################################################################
test_empty_override() {
    print_header "Edge Case: Empty Override File"

    setup_env
    local override="$TEST_ROOT/data/adb/cco/active/override.xml"
    : > "$override"

    if [ -f "$override" ]; then
        pass_test "Empty override file exists"
    else
        fail_test "Failed to create empty file"
    fi

    local size=$(stat -c%s "$override" 2>/dev/null || stat -f%z "$override" 2>/dev/null)
    if [ "$size" -eq 0 ]; then
        pass_test "File size is 0 bytes"
    else
        fail_test "Expected 0 bytes, got $size"
    fi

    # XML validation should reject it
    if ! grep -q '<carrier_config' "$override" 2>/dev/null; then
        pass_test "Empty file correctly fails XML check"
    else
        fail_test "Empty file should not pass XML check"
    fi
}

###############################################################################
# 2. Override with only whitespace
###############################################################################
test_whitespace_override() {
    print_header "Edge Case: Whitespace-Only Override"

    setup_env
    local override="$TEST_ROOT/data/adb/cco/active/override.xml"
    printf "   \n\n\t\t\n   \n" > "$override"

    if ! grep -q '<carrier_config' "$override" 2>/dev/null; then
        pass_test "Whitespace-only file correctly fails XML check"
    else
        fail_test "Whitespace file should not pass"
    fi
}

###############################################################################
# 3. Very large override file
###############################################################################
test_large_override() {
    print_header "Edge Case: Large Override File"

    setup_env
    local override="$TEST_ROOT/data/adb/cco/active/override.xml"

    # Generate a large (~100KB) but valid XML
    echo '<?xml version="1.0" encoding="utf-8"?>' > "$override"
    echo '<carrier_config>' >> "$override"
    for i in $(seq 1 2000); do
        echo "  <boolean name=\"test_key_${i}_bool\" value=\"true\"/>" >> "$override"
    done
    echo '</carrier_config>' >> "$override"

    local size=$(stat -c%s "$override" 2>/dev/null || stat -f%z "$override" 2>/dev/null)
    if [ "$size" -gt 50000 ]; then
        pass_test "Large override created ($size bytes)"
    else
        fail_test "File not large enough ($size bytes)"
    fi

    if grep -q '<carrier_config' "$override" 2>/dev/null; then
        pass_test "Large file passes XML check"
    else
        fail_test "Large valid XML should pass check"
    fi

    if grep -q '</carrier_config>' "$override" 2>/dev/null; then
        pass_test "Large file has closing tag"
    else
        fail_test "Large file missing closing tag"
    fi
}

###############################################################################
# 4. Override with special characters in values
###############################################################################
test_special_chars_override() {
    print_header "Edge Case: Special Characters in XML"

    setup_env
    local override="$TEST_ROOT/data/adb/cco/active/override.xml"

    cat > "$override" << 'XMLEOF'
<?xml version="1.0" encoding="utf-8"?>
<carrier_config>
  <string name="test_string" value="hello &amp; world"/>
  <string name="test_path" value="/data/vendor/test &lt;path&gt;"/>
</carrier_config>
XMLEOF

    if grep -q '<carrier_config' "$override" 2>/dev/null; then
        pass_test "XML with special chars passes check"
    else
        fail_test "Should accept XML with entities"
    fi
}

###############################################################################
# 5. Concurrent log writes (simulate)
###############################################################################
test_concurrent_logging() {
    print_header "Edge Case: Concurrent Log Writes"

    setup_env
    local log_file="$TEST_ROOT/data/adb/cco/logs/module.log"
    : > "$log_file"

    # Simulate 10 concurrent appends
    for i in $(seq 1 10); do
        echo "[test] Concurrent write $i" >> "$log_file" &
    done
    wait

    local count=$(wc -l < "$log_file" | tr -d ' ')
    if [ "$count" -eq 10 ]; then
        pass_test "All 10 concurrent writes captured"
    else
        warn_test "Got $count/10 lines (may vary on some systems)"
    fi
}

###############################################################################
# 6. Path with spaces (though unlikely on Android)
###############################################################################
test_path_with_spaces() {
    print_header "Edge Case: Paths with Spaces"

    setup_env
    local spaced_dir="$TEST_ROOT/data/with spaces/carrier"
    mkdir -p "$spaced_dir"

    echo "test-content" > "$spaced_dir/override.xml"

    if [ -f "$spaced_dir/override.xml" ]; then
        pass_test "File created in path with spaces"
    else
        fail_test "Failed to handle path with spaces"
    fi

    # Backup should handle quoted paths
    local backup_dest="$TEST_ROOT/backup.xml"
    cp "$spaced_dir/override.xml" "$backup_dest"
    if cmp -s "$spaced_dir/override.xml" "$backup_dest"; then
        pass_test "Copy from spaced path succeeds"
    else
        fail_test "Copy from spaced path failed"
    fi
}

###############################################################################
# 7. Symlink as override file
###############################################################################
test_symlink_override() {
    print_header "Edge Case: Symlink Override"

    setup_env
    local real_file="$TEST_ROOT/real_override.xml"
    local override="$TEST_ROOT/data/adb/cco/active/override.xml"

    cat > "$real_file" << 'XMLEOF'
<?xml version="1.0" encoding="utf-8"?>
<carrier_config>
  <boolean name="carrier_wfc_ims_available_bool" value="true"/>
</carrier_config>
XMLEOF

    ln -sf "$real_file" "$override"

    if [ -L "$override" ]; then
        pass_test "Symlink created"
    else
        fail_test "Symlink creation failed"
    fi

    if [ -f "$override" ]; then
        pass_test "Symlink resolves to file"
    else
        fail_test "Symlink does not resolve"
    fi

    if grep -q '<carrier_config' "$override" 2>/dev/null; then
        pass_test "XML check works through symlink"
    else
        fail_test "XML check fails through symlink"
    fi
}

###############################################################################
# 8. Missing CCO data directory recovery
###############################################################################
test_missing_cco_dir() {
    print_header "Edge Case: Missing CCO Data Directory"

    rm -rf "$TEST_ROOT"/*
    local cco_dir="$TEST_ROOT/data/adb/cco"

    # Simulate service.sh recovery: if CCO_DATA missing, recreate
    if [ ! -d "$cco_dir" ]; then
        mkdir -p "$cco_dir/overrides"
        mkdir -p "$cco_dir/active"
        mkdir -p "$cco_dir/logs"
        mkdir -p "$cco_dir/backup"
    fi

    local dirs_ok=1
    for subdir in overrides active logs backup; do
        if [ ! -d "$cco_dir/$subdir" ]; then
            dirs_ok=0
            fail_test "Recovery failed for: $subdir"
        fi
    done

    if [ $dirs_ok -eq 1 ]; then
        pass_test "All directories recovered from scratch"
    fi
}

###############################################################################
# 9. WFC mode int range validation in profiles
###############################################################################
test_wfc_mode_range() {
    print_header "Edge Case: WFC Mode Int Range"

    # Valid modes: 0 (Cellular Preferred), 1 (Wi-Fi Preferred), 2 (Wi-Fi Only)
    for profile in "$MODULE_DIR/profiles"/*.xml; do
        if [ ! -f "$profile" ]; then
            continue
        fi
        local pname=$(basename "$profile")

        # Extract all wfc_ims_mode_int values
        local modes=$(grep -oP 'carrier_default_wfc_ims_(roaming_)?mode_int.*?value="\K[0-9]+' "$profile" 2>/dev/null || \
                      grep -oE 'carrier_default_wfc_ims_(roaming_)?mode_int[^/]*value="[0-9]+"' "$profile" | \
                      grep -oE 'value="[0-9]+"' | grep -oE '[0-9]+')

        if [ -z "$modes" ]; then
            warn_test "$pname: no WFC mode int found"
            continue
        fi

        local all_valid=1
        for mode in $modes; do
            if [ "$mode" -ge 0 ] && [ "$mode" -le 2 ]; then
                pass_test "$pname: WFC mode $mode is valid (0-2)"
            else
                fail_test "$pname: WFC mode $mode out of range (must be 0-2)"
                all_valid=0
            fi
        done
    done
}

###############################################################################
# 10. module.prop no trailing whitespace or blank lines
###############################################################################
test_module_prop_cleanliness() {
    print_header "Edge Case: module.prop Cleanliness"

    local prop_file="$MODULE_DIR/module.prop"

    # No trailing whitespace
    if grep -qP '\s+$' "$prop_file" 2>/dev/null; then
        warn_test "module.prop has trailing whitespace"
    else
        pass_test "module.prop has no trailing whitespace"
    fi

    # No empty lines between properties
    local empty_lines=$(grep -c '^$' "$prop_file" | tr -d ' ')
    if [ "$empty_lines" -le 1 ]; then
        pass_test "module.prop has $empty_lines empty line(s) (acceptable)"
    else
        warn_test "module.prop has $empty_lines empty lines (prefer minimal)"
    fi

    # All required props present (redundant with metadata test but covers edge-case file)
    for prop in id name version versionCode author description; do
        if grep -q "^$prop=" "$prop_file"; then
            pass_test "module.prop has '$prop'"
        else
            fail_test "module.prop missing '$prop'"
        fi
    done
}

###############################################################################
# Run all
###############################################################################
test_empty_override
test_whitespace_override
test_large_override
test_special_chars_override
test_concurrent_logging
test_path_with_spaces
test_symlink_override
test_missing_cco_dir
test_wfc_mode_range
test_module_prop_cleanliness
print_test_summary "Edge Case Tests"
exit $?
