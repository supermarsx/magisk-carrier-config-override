#!/bin/bash
###############################################################################
# CCO Module Test Suite
# Validates module structure, scripts, and functionality
###############################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULE_DIR="$(dirname "$SCRIPT_DIR")"
TEST_RESULTS=()
TESTS_PASSED=0
TESTS_FAILED=0

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_test() {
    echo -e "${YELLOW}[TEST]${NC} $1"
}

pass_test() {
    echo -e "${GREEN}[PASS]${NC} $1"
    TEST_RESULTS+=("PASS: $1")
    TESTS_PASSED=$((TESTS_PASSED + 1))
}

fail_test() {
    echo -e "${RED}[FAIL]${NC} $1"
    TEST_RESULTS+=("FAIL: $1")
    TESTS_FAILED=$((TESTS_FAILED + 1))
}

warn_test() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Test 1: Module structure
test_module_structure() {
    print_header "Testing Module Structure"

    print_test "Checking required files exist..."
    local required_files=(
        "module.prop"
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
        "README.md"
    )

    for file in "${required_files[@]}"; do
        if [ -f "$MODULE_DIR/$file" ]; then
            pass_test "Required file exists: $file"
        else
            fail_test "Missing required file: $file"
        fi
    done

    print_test "Checking optional but recommended files..."
    local optional_files=(
        "system.prop"
        "docs/CHANGELOG.md"
        "common/functions.sh"
    )

    for file in "${optional_files[@]}"; do
        if [ -f "$MODULE_DIR/$file" ]; then
            pass_test "Optional file exists: $file"
        else
            warn_test "Optional file missing: $file"
        fi
    done

    print_test "Checking profiles directory..."
    if [ -d "$MODULE_DIR/profiles" ]; then
        pass_test "Profiles directory exists"
        local profile_count=$(find "$MODULE_DIR/profiles" -name "*.xml" | wc -l)
        if [ "$profile_count" -gt 0 ]; then
            pass_test "Found $profile_count profile(s)"
        else
            fail_test "No profiles found in profiles directory"
        fi
    else
        fail_test "Profiles directory missing"
    fi

    echo ""
}

# Test 2: Script syntax
test_script_syntax() {
    print_header "Testing Script Syntax"

    print_test "Checking shell script syntax..."
    local shell_scripts=(
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
    )

    if [ -f "$MODULE_DIR/common/functions.sh" ]; then
        shell_scripts+=("common/functions.sh")
    fi

    for script in "${shell_scripts[@]}"; do
        if [ -f "$MODULE_DIR/$script" ]; then
            if bash -n "$MODULE_DIR/$script" 2>/dev/null; then
                pass_test "Syntax valid: $script"
            else
                fail_test "Syntax error in: $script"
                bash -n "$MODULE_DIR/$script" 2>&1 | sed 's/^/  /'
            fi
        fi
    done

    echo ""
}

# Test 3: Script permissions
test_script_permissions() {
    print_header "Testing Script Permissions"

    print_test "Checking executable permissions..."
    local exec_scripts=(
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
    )

    for script in "${exec_scripts[@]}"; do
        if [ -f "$MODULE_DIR/$script" ]; then
            if [ -x "$MODULE_DIR/$script" ]; then
                pass_test "Executable: $script"
            else
                warn_test "Not executable: $script (will be set during packaging)"
            fi
        fi
    done

    echo ""
}

# Test 4: XML validation
test_xml_validation() {
    print_header "Testing XML Profile Validation"

    if ! command -v xmllint &> /dev/null; then
        warn_test "xmllint not found, skipping XML validation"
        echo ""
        return
    fi

    print_test "Validating XML profiles..."
    local xml_files=$(find "$MODULE_DIR/profiles" -name "*.xml" 2>/dev/null)

    if [ -z "$xml_files" ]; then
        warn_test "No XML profiles found to validate"
    else
        for xml_file in $xml_files; do
            if xmllint --noout "$xml_file" 2>/dev/null; then
                pass_test "Valid XML: $(basename "$xml_file")"
            else
                fail_test "Invalid XML: $(basename "$xml_file")"
                xmllint --noout "$xml_file" 2>&1 | sed 's/^/  /'
            fi
        done
    fi

    echo ""
}

# Test 5: module.prop validation
test_module_prop() {
    print_header "Testing module.prop"

    local prop_file="$MODULE_DIR/module.prop"

    if [ ! -f "$prop_file" ]; then
        fail_test "module.prop not found"
        echo ""
        return
    fi

    print_test "Checking required properties..."
    local required_props=("id" "name" "version" "versionCode" "author" "description")

    for prop in "${required_props[@]}"; do
        if grep -q "^${prop}=" "$prop_file"; then
            local value=$(grep "^${prop}=" "$prop_file" | cut -d'=' -f2-)
            if [ -n "$value" ]; then
                pass_test "Property '$prop' has value: $value"
            else
                fail_test "Property '$prop' is empty"
            fi
        else
            fail_test "Property '$prop' missing"
        fi
    done

    print_test "Validating property values..."

    # Check version code is numeric
    local version_code=$(grep "^versionCode=" "$prop_file" | cut -d'=' -f2)
    if [[ "$version_code" =~ ^[0-9]+$ ]]; then
        pass_test "versionCode is numeric: $version_code"
    else
        fail_test "versionCode must be numeric, got: $version_code"
    fi

    # Check id format (no spaces, lowercase)
    local module_id=$(grep "^id=" "$prop_file" | cut -d'=' -f2)
    if [[ "$module_id" =~ ^[a-z0-9_-]+$ ]]; then
        pass_test "Module ID format valid: $module_id"
    else
        fail_test "Module ID should be lowercase alphanumeric with - or _"
    fi

    echo ""
}

# Test 6: Script content validation
test_script_content() {
    print_header "Testing Script Content"

    print_test "Checking for bashisms in POSIX scripts..."
    local posix_scripts=(
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
    )

    for script in "${posix_scripts[@]}"; do
        if [ -f "$MODULE_DIR/$script" ]; then
            # Check shebang
            local shebang=$(head -n1 "$MODULE_DIR/$script")
            if [[ "$shebang" == "#!/system/bin/sh" ]]; then
                pass_test "Correct shebang: $script"
            else
                warn_test "Non-standard shebang in $script: $shebang"
            fi

            # Check for common bashisms
            if grep -q '\[\[' "$MODULE_DIR/$script"; then
                warn_test "Found [[ in $script (may not work in all shells)"
            fi

            if grep -q 'function ' "$MODULE_DIR/$script"; then
                warn_test "Found 'function' keyword in $script (not POSIX)"
            fi
        fi
    done

    print_test "Checking for hardcoded paths..."
    local scripts=(
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
    )

    for script in "${scripts[@]}"; do
        if [ -f "$MODULE_DIR/$script" ]; then
            # Check if /data/adb/cco is consistently used
            if grep -q "/data/adb/cco" "$MODULE_DIR/$script"; then
                pass_test "Uses standard CCO data path: $script"
            else
                warn_test "No CCO data path references in: $script"
            fi
        fi
    done

    echo ""
}

# Test 7: Documentation
test_documentation() {
    print_header "Testing Documentation"

    print_test "Checking README.md..."
    if [ -f "$MODULE_DIR/README.md" ]; then
        local readme_size=$(wc -l < "$MODULE_DIR/README.md")
        if [ "$readme_size" -gt 50 ]; then
            pass_test "README.md exists and has $readme_size lines"
        else
            warn_test "README.md seems short ($readme_size lines)"
        fi

        # Check for important sections
        local sections=("Installation" "Usage" "Troubleshooting")
        for section in "${sections[@]}"; do
            if grep -qi "$section" "$MODULE_DIR/README.md"; then
                pass_test "README contains '$section' section"
            else
                warn_test "README missing '$section' section"
            fi
        done
    else
        fail_test "README.md not found"
    fi

    print_test "Checking profiles documentation..."
    if [ -f "$MODULE_DIR/profiles/README.md" ]; then
        pass_test "Profiles README exists"
    else
        warn_test "profiles/README.md not found"
    fi

    echo ""
}

# Test 8: Profile content validation
test_profile_content() {
    print_header "Testing Profile Content"

    print_test "Checking profile configurations..."
    local profiles=$(find "$MODULE_DIR/profiles" -name "*.xml" 2>/dev/null)

    if [ -z "$profiles" ]; then
        warn_test "No profiles to validate"
        echo ""
        return
    fi

    for profile in $profiles; do
        local profile_name=$(basename "$profile")

        # Check for required carrier_config root element
        if grep -q "<carrier_config>" "$profile"; then
            pass_test "$profile_name has carrier_config root element"
        else
            fail_test "$profile_name missing carrier_config root element"
        fi

        # Check for common Wi-Fi Calling keys
        if grep -q "carrier_wfc_ims_available_bool" "$profile"; then
            pass_test "$profile_name includes WFC enablement key"
        else
            warn_test "$profile_name may not enable Wi-Fi Calling"
        fi

        # Check for XML declaration
        if head -n1 "$profile" | grep -q '<?xml version'; then
            pass_test "$profile_name has XML declaration"
        else
            warn_test "$profile_name missing XML declaration"
        fi
    done

    echo ""
}

# Test 9: Logging capabilities
test_logging() {
    print_header "Testing Logging Implementation"

    print_test "Checking for log statements..."
    local scripts_with_logging=(
        "service.sh"
        "post-fs-data.sh"
        "uninstall.sh"
    )

    for script in "${scripts_with_logging[@]}"; do
        if [ -f "$MODULE_DIR/$script" ]; then
            if grep -q "log" "$MODULE_DIR/$script"; then
                pass_test "$script implements logging"

                # Check for log file definition
                if grep -q "LOG_FILE=" "$MODULE_DIR/$script"; then
                    pass_test "$script defines LOG_FILE variable"
                else
                    warn_test "$script may not define log file path"
                fi
            else
                warn_test "$script may not implement logging"
            fi
        fi
    done

    echo ""
}

# Test 10: Safety features
test_safety_features() {
    print_header "Testing Safety Features"

    print_test "Checking for backup implementation..."
    if grep -q "backup" "$MODULE_DIR/service.sh" 2>/dev/null; then
        pass_test "service.sh includes backup logic"
    else
        warn_test "service.sh may not backup original files"
    fi

    print_test "Checking for disable flag support..."
    if grep -q "disable" "$MODULE_DIR/service.sh" 2>/dev/null; then
        pass_test "service.sh supports disable flag"
    else
        warn_test "service.sh may not support disable flag"
    fi

    print_test "Checking uninstall cleanup..."
    if [ -f "$MODULE_DIR/uninstall.sh" ]; then
        if grep -q "umount\|unmount" "$MODULE_DIR/uninstall.sh"; then
            pass_test "uninstall.sh unmounts bind mounts"
        else
            warn_test "uninstall.sh may not clean up mounts"
        fi

        if grep -q "backup" "$MODULE_DIR/uninstall.sh"; then
            pass_test "uninstall.sh restores backups"
        else
            warn_test "uninstall.sh may not restore backups"
        fi
    fi

    echo ""
}

# Print summary
print_summary() {
    print_header "Test Summary"

    local total_tests=$((TESTS_PASSED + TESTS_FAILED))

    echo -e "${BLUE}Total tests run:${NC} $total_tests"
    echo -e "${GREEN}Tests passed:${NC} $TESTS_PASSED"
    echo -e "${RED}Tests failed:${NC} $TESTS_FAILED"

    if [ $TESTS_FAILED -eq 0 ]; then
        echo ""
        echo -e "${GREEN}========================================${NC}"
        echo -e "${GREEN}ALL TESTS PASSED!${NC}"
        echo -e "${GREEN}========================================${NC}"
        return 0
    else
        echo ""
        echo -e "${RED}========================================${NC}"
        echo -e "${RED}SOME TESTS FAILED${NC}"
        echo -e "${RED}========================================${NC}"
        echo ""
        echo "Failed tests:"
        for result in "${TEST_RESULTS[@]}"; do
            if [[ "$result" == FAIL:* ]]; then
                echo -e "${RED}  - ${result#FAIL: }${NC}"
            fi
        done
        return 1
    fi
}

# Main test execution
main() {
    print_header "CCO Module Test Suite"
    echo "Module directory: $MODULE_DIR"
    echo ""

    test_module_structure
    test_module_prop
    test_script_syntax
    test_script_permissions
    test_xml_validation
    test_script_content
    test_documentation
    test_profile_content
    test_logging
    test_safety_features

    print_summary
}

# Run tests
main
exit $?
