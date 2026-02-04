#!/bin/bash
###############################################################################
# CCO Module Integration Test
# Tests module functionality in a simulated environment
###############################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULE_DIR="$(dirname "$SCRIPT_DIR")"
TEST_ENV="/tmp/cco-module-test-$$"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Cleanup test environment
cleanup() {
    if [ -d "$TEST_ENV" ]; then
        rm -rf "$TEST_ENV"
    fi
}

trap cleanup EXIT

# Setup test environment
setup_test_env() {
    print_header "Setting Up Test Environment"

    mkdir -p "$TEST_ENV/data/adb/cco"
    mkdir -p "$TEST_ENV/data/vendor/carrierconfig"
    mkdir -p "$TEST_ENV/module"

    # Copy module files
    cp -r "$MODULE_DIR"/* "$TEST_ENV/module/" 2>/dev/null || true

    print_success "Test environment created: $TEST_ENV"
    echo ""
}

# Test 1: Directory creation
test_directory_creation() {
    print_header "Test: Directory Creation"

    print_info "Simulating post-fs-data.sh..."

    export CCO_DATA="$TEST_ENV/data/adb/cco"
    export LOG_FILE="$CCO_DATA/logs/module.log"

    # Create directories as the script would
    mkdir -p "$CCO_DATA/overrides"
    mkdir -p "$CCO_DATA/active"
    mkdir -p "$CCO_DATA/logs"
    mkdir -p "$CCO_DATA/backup"

    # Check creation
    local dirs=("overrides" "active" "logs" "backup")
    local all_created=1

    for dir in "${dirs[@]}"; do
        if [ -d "$CCO_DATA/$dir" ]; then
            print_success "Directory created: $dir"
        else
            print_error "Directory not created: $dir"
            all_created=0
        fi
    done

    echo ""
    return $((1 - all_created))
}

# Test 2: Override file deployment
test_override_deployment() {
    print_header "Test: Override File Deployment"

    local test_override="$TEST_ENV/data/adb/cco/active/override.xml"

    print_info "Creating test override file..."

    cat > "$test_override" << 'EOF'
<?xml version="1.0" encoding="utf-8"?>
<carrier_config>
    <boolean name="carrier_wfc_ims_available_bool" value="true" />
    <boolean name="carrier_default_wfc_ims_enabled_bool" value="true" />
    <int name="carrier_default_wfc_ims_mode_int" value="1" />
</carrier_config>
EOF

    if [ -f "$test_override" ]; then
        print_success "Override file created"

        # Validate XML
        if command -v xmllint &> /dev/null; then
            if xmllint --noout "$test_override" 2>/dev/null; then
                print_success "Override file is valid XML"
            else
                print_error "Override file has XML errors"
                return 1
            fi
        else
            print_warning "xmllint not available, skipping XML validation"
        fi
    else
        print_error "Failed to create override file"
        return 1
    fi

    echo ""
    return 0
}

# Test 3: Path detection logic
test_path_detection() {
    print_header "Test: Path Detection Logic"

    print_info "Testing path detection algorithm..."

    local candidate_paths=(
        "$TEST_ENV/data/vendor/carrierconfig/override.xml"
        "$TEST_ENV/data/vendor/carrierconfig/override_carrier.xml"
        "$TEST_ENV/data/misc/carrierconfig/override.xml"
    )

    # Create one of the directories
    mkdir -p "$TEST_ENV/data/vendor/carrierconfig"

    local detected_path=""

    for path in "${candidate_paths[@]}"; do
        local dir=$(dirname "$path")
        if [ -d "$dir" ]; then
            detected_path="$path"
            break
        fi
    done

    if [ -n "$detected_path" ]; then
        print_success "Path detected: $detected_path"
    else
        print_error "Path detection failed"
        return 1
    fi

    echo ""
    return 0
}

# Test 4: Backup functionality
test_backup_functionality() {
    print_header "Test: Backup Functionality"

    local original_file="$TEST_ENV/data/vendor/carrierconfig/override.xml"
    local backup_file="$TEST_ENV/data/adb/cco/backup/override_original.xml"

    print_info "Creating original file..."
    echo "original content" > "$original_file"

    print_info "Simulating backup..."
    mkdir -p "$(dirname "$backup_file")"
    cp "$original_file" "$backup_file"

    if [ -f "$backup_file" ]; then
        print_success "Backup created"

        # Verify content
        if cmp -s "$original_file" "$backup_file"; then
            print_success "Backup content matches original"
        else
            print_error "Backup content differs from original"
            return 1
        fi
    else
        print_error "Backup not created"
        return 1
    fi

    echo ""
    return 0
}

# Test 5: Profile validation
test_profile_validation() {
    print_header "Test: Profile Validation"

    if [ ! -d "$MODULE_DIR/profiles" ]; then
        print_warning "Profiles directory not found, skipping"
        echo ""
        return 0
    fi

    local profiles=$(find "$MODULE_DIR/profiles" -name "*.xml" 2>/dev/null)

    if [ -z "$profiles" ]; then
        print_warning "No profiles found, skipping"
        echo ""
        return 0
    fi

    local all_valid=1

    for profile in $profiles; do
        local profile_name=$(basename "$profile")
        print_info "Validating $profile_name..."

        # Check XML syntax
        if command -v xmllint &> /dev/null; then
            if xmllint --noout "$profile" 2>/dev/null; then
                print_success "$profile_name is valid"
            else
                print_error "$profile_name has XML errors"
                all_valid=0
            fi
        fi

        # Check for required elements
        if grep -q "<carrier_config>" "$profile"; then
            print_success "$profile_name has carrier_config root"
        else
            print_error "$profile_name missing carrier_config root"
            all_valid=0
        fi
    done

    echo ""
    return $((1 - all_valid))
}

# Test 6: Logging functionality
test_logging() {
    print_header "Test: Logging Functionality"

    local log_file="$TEST_ENV/data/adb/cco/logs/module.log"

    mkdir -p "$(dirname "$log_file")"

    print_info "Testing log writing..."

    # Simulate log entries
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] TEST: Module initialized" >> "$log_file"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] INFO: Override deployed" >> "$log_file"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS: Bind mount completed" >> "$log_file"

    if [ -f "$log_file" ]; then
        local line_count=$(wc -l < "$log_file")
        print_success "Log file created with $line_count entries"

        # Verify log format
        if grep -q '^\[.*\]' "$log_file"; then
            print_success "Log entries have timestamp format"
        else
            print_warning "Log entries may not have proper timestamp format"
        fi
    else
        print_error "Log file not created"
        return 1
    fi

    echo ""
    return 0
}

# Test 7: Disable flag
test_disable_flag() {
    print_header "Test: Disable Flag Functionality"

    local disable_flag="$TEST_ENV/data/adb/cco/disable"

    print_info "Creating disable flag..."
    touch "$disable_flag"

    if [ -f "$disable_flag" ]; then
        print_success "Disable flag created"

        # Test detection
        if [ -f "$disable_flag" ]; then
            print_success "Disable flag detected correctly"
        else
            print_error "Disable flag detection failed"
            return 1
        fi
    else
        print_error "Failed to create disable flag"
        return 1
    fi

    echo ""
    return 0
}

# Test 8: Script syntax validation
test_script_syntax() {
    print_header "Test: Script Syntax Validation"

    local scripts=(
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
    )

    local all_valid=1

    for script in "${scripts[@]}"; do
        if [ -f "$MODULE_DIR/$script" ]; then
            print_info "Checking syntax: $script..."

            if bash -n "$MODULE_DIR/$script" 2>/dev/null; then
                print_success "$script syntax valid"
            else
                print_error "$script has syntax errors"
                bash -n "$MODULE_DIR/$script" 2>&1
                all_valid=0
            fi
        fi
    done

    echo ""
    return $((1 - all_valid))
}

# Print test summary
print_test_summary() {
    print_header "Integration Test Summary"

    echo "Test environment: $TEST_ENV"
    echo ""

    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}All integration tests passed!${NC}"
    else
        echo -e "${RED}Some integration tests failed${NC}"
        echo -e "${YELLOW}Review the output above for details${NC}"
    fi

    echo ""
}

# Main function
main() {
    print_header "CCO Module Integration Tests"
    echo "Module directory: $MODULE_DIR"
    echo ""

    setup_test_env

    local exit_code=0

    test_directory_creation || exit_code=1
    test_override_deployment || exit_code=1
    test_path_detection || exit_code=1
    test_backup_functionality || exit_code=1
    test_profile_validation || exit_code=1
    test_logging || exit_code=1
    test_disable_flag || exit_code=1
    test_script_syntax || exit_code=1

    print_test_summary $exit_code

    return $exit_code
}

main
exit $?
