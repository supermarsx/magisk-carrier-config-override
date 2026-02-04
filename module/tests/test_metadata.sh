#!/usr/bin/env bash
###############################################################################
# Test: Module Metadata
# Validates module.prop and documentation
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

test_metadata() {
    print_header "Testing Module Metadata"

    print_test "Checking module.prop completeness..."
    local required_props=(
        "id"
        "name"
        "version"
        "versionCode"
        "author"
        "description"
    )

    for prop in "${required_props[@]}"; do
        if grep -qE "^$prop=" "$MODULE_DIR/module.prop"; then
            local value=$(grep "^$prop=" "$MODULE_DIR/module.prop" | cut -d'=' -f2-)
            pass_test "module.prop has '$prop': $value"
        else
            fail_test "module.prop missing '$prop' property"
        fi
    done

    print_test "Validating versionCode is numeric..."
    local versionCode=$(grep "^versionCode=" "$MODULE_DIR/module.prop" | cut -d'=' -f2)
    if [[ "$versionCode" =~ ^[0-9]+$ ]]; then
        pass_test "versionCode is numeric: $versionCode"
    else
        fail_test "versionCode is not numeric: $versionCode"
    fi

    print_test "Checking version format..."
    local version=$(grep "^version=" "$MODULE_DIR/module.prop" | cut -d'=' -f2)
    if echo "$version" | grep -qE "^[0-9]+\.[0-9]+\.[0-9]+"; then
        pass_test "Version follows semantic versioning: $version"
    else
        warn_test "Version may not follow semantic versioning: $version"
    fi

    print_test "Checking module ID format..."
    local module_id=$(grep "^id=" "$MODULE_DIR/module.prop" | cut -d'=' -f2)
    if echo "$module_id" | grep -qE "^[a-z0-9_-]+$"; then
        pass_test "Module ID format is valid: $module_id"
    else
        fail_test "Module ID contains invalid characters: $module_id"
    fi

    print_test "Checking for optional properties..."
    if grep -qE "^support=" "$MODULE_DIR/module.prop"; then
        pass_test "module.prop includes support URL"
    else
        warn_test "Consider adding 'support=' URL to module.prop"
    fi

    print_test "Checking README..."
    if [ -f "$MODULE_DIR/README.md" ]; then
        local readme_lines=$(wc -l < "$MODULE_DIR/README.md")
        if [ "$readme_lines" -gt 50 ]; then
            pass_test "README.md is comprehensive ($readme_lines lines)"
        else
            warn_test "README.md is short ($readme_lines lines)"
        fi
    else
        fail_test "README.md not found"
    fi

    print_test "Checking CHANGELOG..."
    if [ -f "$MODULE_DIR/docs/CHANGELOG.md" ]; then
        pass_test "CHANGELOG.md exists"
        if grep -qE "^## |^# " "$MODULE_DIR/docs/CHANGELOG.md"; then
            pass_test "CHANGELOG.md has structured entries"
        else
            warn_test "CHANGELOG.md may lack structure"
        fi
    else
        warn_test "Consider adding docs/CHANGELOG.md"
    fi
}

test_metadata
print_test_summary "Metadata Tests"
exit $?
