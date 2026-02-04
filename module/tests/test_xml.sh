#!/usr/bin/env bash
###############################################################################
# Test: XML Profile Validation
# Validates XML profiles structure and content
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

test_xml_profiles() {
    print_header "Testing XML Profile Validation"

    if [ ! -d "$MODULE_DIR/profiles" ]; then
        fail_test "Profiles directory not found"
        return 1
    fi

    print_test "Validating XML syntax..."
    for profile in "$MODULE_DIR/profiles"/*.xml; do
        if [ -f "$profile" ]; then
            local basename=$(basename "$profile")
            
            # Check with xmllint if available
            if command -v xmllint >/dev/null 2>&1; then
                if xmllint --noout "$profile" 2>/dev/null; then
                    pass_test "Valid XML: $basename"
                else
                    fail_test "Invalid XML: $basename"
                fi
            else
                # Basic check
                if grep -q "<?xml" "$profile" && grep -q "</carrier_config>" "$profile"; then
                    pass_test "Basic XML check passed: $basename"
                else
                    fail_test "XML structure issue: $basename"
                fi
            fi
        fi
    done

    print_test "Checking XML declarations..."
    for profile in "$MODULE_DIR/profiles"/*.xml; do
        if [ -f "$profile" ]; then
            if grep -q "<?xml version" "$profile"; then
                pass_test "$(basename "$profile") has XML declaration"
            else
                warn_test "$(basename "$profile") missing XML declaration"
            fi
        fi
    done

    print_test "Checking carrier_config root element..."
    for profile in "$MODULE_DIR/profiles"/*.xml; do
        if [ -f "$profile" ]; then
            if grep -q "<carrier_config" "$profile"; then
                pass_test "$(basename "$profile") has carrier_config root"
            else
                fail_test "$(basename "$profile") missing carrier_config root"
            fi
        fi
    done

    print_test "Checking for duplicate keys..."
    for profile in "$MODULE_DIR/profiles"/*.xml; do
        if [ -f "$profile" ]; then
            local keys=$(grep -oE 'name="[^"]+"' "$profile" | sort)
            local unique_keys=$(echo "$keys" | uniq)
            if [ "$(echo "$keys" | wc -l)" -eq "$(echo "$unique_keys" | wc -l)" ]; then
                pass_test "$(basename "$profile") has no duplicate keys"
            else
                warn_test "$(basename "$profile") may have duplicate keys"
            fi
        fi
    done

    print_test "Validating boolean values..."
    for profile in "$MODULE_DIR/profiles"/*.xml; do
        if [ -f "$profile" ]; then
            if grep -E 'type="boolean"' "$profile" | grep -vE 'value="(true|false)"' >/dev/null 2>&1; then
                warn_test "$(basename "$profile") may have invalid boolean values"
            else
                pass_test "$(basename "$profile") boolean values are valid"
            fi
        fi
    done

    print_test "Checking for critical WFC keys..."
    local critical_keys="carrier_wfc_ims_available_bool|editable_wfc_mode_bool"
    for profile in "$MODULE_DIR/profiles"/*.xml; do
        if [ -f "$profile" ]; then
            if grep -qE "$critical_keys" "$profile"; then
                pass_test "$(basename "$profile") includes critical WFC keys"
            else
                warn_test "$(basename "$profile") may be missing critical keys"
            fi
        fi
    done
}

test_xml_profiles
print_test_summary "XML Profile Tests"
exit $?
