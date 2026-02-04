#!/bin/bash
###############################################################################
# CCO Module Packaging Script
# Creates a flashable Magisk module ZIP
###############################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULE_DIR="$(dirname "$SCRIPT_DIR")"
PROJECT_DIR="$(dirname "$MODULE_DIR")"
BUILD_DIR="$PROJECT_DIR/build"
DIST_DIR="$PROJECT_DIR/dist"

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

# Read module properties
read_module_info() {
    if [ ! -f "$MODULE_DIR/module.prop" ]; then
        print_error "module.prop not found!"
        exit 1
    fi

    MODULE_ID=$(grep "^id=" "$MODULE_DIR/module.prop" | cut -d'=' -f2)
    MODULE_VERSION=$(grep "^version=" "$MODULE_DIR/module.prop" | cut -d'=' -f2)
    MODULE_VERSION_CODE=$(grep "^versionCode=" "$MODULE_DIR/module.prop" | cut -d'=' -f2)

    if [ -z "$MODULE_ID" ] || [ -z "$MODULE_VERSION" ]; then
        print_error "Invalid module.prop!"
        exit 1
    fi

    print_info "Module ID: $MODULE_ID"
    print_info "Version: $MODULE_VERSION (code: $MODULE_VERSION_CODE)"
}

# Clean previous builds
clean_build() {
    print_header "Cleaning Build Directory"

    if [ -d "$BUILD_DIR" ]; then
        print_info "Removing old build directory..."
        rm -rf "$BUILD_DIR"
    fi

    mkdir -p "$BUILD_DIR"
    mkdir -p "$DIST_DIR"

    print_success "Build directory prepared"
    echo ""
}

# Copy module files to build directory
copy_module_files() {
    print_header "Copying Module Files"

    local files_to_copy=(
        "module.prop"
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
        "README.md"
    )

    # Copy required files
    for file in "${files_to_copy[@]}"; do
        if [ -f "$MODULE_DIR/$file" ]; then
            cp "$MODULE_DIR/$file" "$BUILD_DIR/"
            print_success "Copied: $file"
        else
            print_error "Missing required file: $file"
            exit 1
        fi
    done

    # Copy optional files
    if [ -f "$MODULE_DIR/system.prop" ]; then
        cp "$MODULE_DIR/system.prop" "$BUILD_DIR/"
        print_success "Copied: system.prop"
    fi

    # Copy docs directory if exists
    if [ -d "$MODULE_DIR/docs" ]; then
        cp -r "$MODULE_DIR/docs" "$BUILD_DIR/"
        print_success "Copied: docs/ directory"
    fi

    # Copy common directory if exists
    if [ -d "$MODULE_DIR/common" ]; then
        cp -r "$MODULE_DIR/common" "$BUILD_DIR/"
        print_success "Copied: common/ directory"
    fi

    # Copy profiles directory
    if [ -d "$MODULE_DIR/profiles" ]; then
        cp -r "$MODULE_DIR/profiles" "$BUILD_DIR/"
        print_success "Copied: profiles/ directory"
    else
        print_warning "profiles/ directory not found"
    fi

    # Create empty system directory (required for Magisk)
    mkdir -p "$BUILD_DIR/system"
    print_success "Created: system/ directory"

    echo ""
}

# Set correct permissions
set_permissions() {
    print_header "Setting Permissions"

    # Make scripts executable
    chmod 755 "$BUILD_DIR"/*.sh 2>/dev/null || true

    if [ -d "$BUILD_DIR/common" ]; then
        chmod 755 "$BUILD_DIR/common"/*.sh 2>/dev/null || true
    fi

    # Set file permissions
    find "$BUILD_DIR" -type f -name "*.sh" -exec chmod 755 {} \;
    find "$BUILD_DIR" -type f -name "*.prop" -exec chmod 644 {} \;
    find "$BUILD_DIR" -type f -name "*.md" -exec chmod 644 {} \;
    find "$BUILD_DIR" -type f -name "*.xml" -exec chmod 644 {} \;

    print_success "Permissions set"
    echo ""
}

# Validate build
validate_build() {
    print_header "Validating Build"

    local required_files=(
        "module.prop"
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
    )

    local validation_failed=0

    for file in "${required_files[@]}"; do
        if [ ! -f "$BUILD_DIR/$file" ]; then
            print_error "Missing: $file"
            validation_failed=1
        else
            print_success "Found: $file"
        fi
    done

    # Check system directory exists
    if [ ! -d "$BUILD_DIR/system" ]; then
        print_error "Missing: system/ directory"
        validation_failed=1
    else
        print_success "Found: system/ directory"
    fi

    if [ $validation_failed -eq 1 ]; then
        print_error "Build validation failed!"
        exit 1
    fi

    print_success "Build validation passed"
    echo ""
}

# Create ZIP archive
create_zip() {
    print_header "Creating ZIP Archive"

    local zip_name="${MODULE_ID}-${MODULE_VERSION}.zip"
    local zip_path="$DIST_DIR/$zip_name"

    # Remove old ZIP if exists
    if [ -f "$zip_path" ]; then
        rm "$zip_path"
    fi

    print_info "Creating: $zip_name"

    # Create ZIP with proper structure
    cd "$BUILD_DIR"

    if command -v zip &> /dev/null; then
        # Use zip command if available
        zip -r9 "$zip_path" . -x "*.git*" "*.DS_Store" 2>/dev/null
        print_success "ZIP created using zip command"
    else
        print_error "zip command not found!"
        print_info "Install: brew install zip  or  apt install zip"
        exit 1
    fi

    cd "$PROJECT_DIR"

    # Get ZIP size
    local zip_size=$(du -h "$zip_path" | cut -f1)

    print_success "Module packaged: $zip_name ($zip_size)"
    print_info "Location: $zip_path"

    echo ""
}

# Create checksum
create_checksum() {
    print_header "Creating Checksum"

    local zip_name="${MODULE_ID}-${MODULE_VERSION}.zip"
    local zip_path="$DIST_DIR/$zip_name"

    if [ ! -f "$zip_path" ]; then
        print_error "ZIP file not found!"
        exit 1
    fi

    cd "$DIST_DIR"

    # Create SHA256 checksum
    if command -v sha256sum &> /dev/null; then
        sha256sum "$zip_name" > "${zip_name}.sha256"
        print_success "SHA256 checksum created"
        cat "${zip_name}.sha256"
    elif command -v shasum &> /dev/null; then
        shasum -a 256 "$zip_name" > "${zip_name}.sha256"
        print_success "SHA256 checksum created"
        cat "${zip_name}.sha256"
    else
        print_warning "sha256sum/shasum not available, skipping checksum"
    fi

    cd "$PROJECT_DIR"

    echo ""
}

# Generate release notes
generate_release_notes() {
    print_header "Generating Release Notes"

    local zip_name="${MODULE_ID}-${MODULE_VERSION}.zip"
    local notes_file="$DIST_DIR/${MODULE_ID}-${MODULE_VERSION}-RELEASE_NOTES.txt"

    cat > "$notes_file" << EOF
CCO CarrierConfig Override Module
Version: ${MODULE_VERSION} (${MODULE_VERSION_CODE})
Build Date: $(date '+%Y-%m-%d %H:%M:%S')

═══════════════════════════════════════════════════════════

INSTALLATION INSTRUCTIONS:

1. Download: ${zip_name}
2. Open Magisk Manager
3. Tap "Modules" → "Install from storage"
4. Select the downloaded ZIP
5. Reboot when prompted

QUICK START:

1. Install CCO Manager app (recommended)
2. Choose a profile:
   - Generic: Standard Wi-Fi Calling enablement
   - Aggressive: Maximum enablement, bypass restrictions
   - Wi-Fi Only: Force Wi-Fi calling only

3. Deploy profile via app or manually:
   cp profiles/generic_wfc_enable.xml /data/adb/cco/active/override.xml
   chmod 644 /data/adb/cco/active/override.xml

4. Reboot device
5. Check Settings → Connections → Wi-Fi Calling

═══════════════════════════════════════════════════════════

REQUIREMENTS:

✓ Magisk 24.0+
✓ Android 13+ (SDK 33+) recommended
✓ Samsung device (One UI 5/6/7)
✓ Root access

INCLUDED PROFILES:

• generic_wfc_enable.xml - Standard Wi-Fi Calling enablement
• aggressive_enable.xml - Maximum enablement with bypass
• wifi_only_mode.xml - Force Wi-Fi Only mode

═══════════════════════════════════════════════════════════

TROUBLESHOOTING:

• No Wi-Fi Calling after install?
  → Try aggressive profile
  → Check logs: /data/adb/cco/logs/module.log
  → Verify mount: mount | grep carrierconfig

• Device issues?
  → Disable: touch /data/adb/cco/disable && reboot
  → Or toggle off module in Magisk

• Logs location: /data/adb/cco/logs/module.log
• Data directory: /data/adb/cco/

═══════════════════════════════════════════════════════════

SUPPORT:

GitHub: https://github.com/supermarsx/magisk-carrier-config-override
Issues: https://github.com/supermarsx/magisk-carrier-config-override/issues

═══════════════════════════════════════════════════════════

CHANGELOG:

EOF

    # Append changelog if exists
    if [ -f "$MODULE_DIR/docs/CHANGELOG.md" ]; then
        echo "" >> "$notes_file"
        head -n 50 "$MODULE_DIR/docs/CHANGELOG.md" >> "$notes_file"
    fi

    print_success "Release notes created: $(basename "$notes_file")"
    echo ""
}

# Print summary
print_summary() {
    print_header "Build Summary"

    local zip_name="${MODULE_ID}-${MODULE_VERSION}.zip"
    local zip_path="$DIST_DIR/$zip_name"

    echo -e "${GREEN}Package Information:${NC}"
    echo "  Name: $zip_name"
    echo "  Version: $MODULE_VERSION"
    echo "  Version Code: $MODULE_VERSION_CODE"
    echo "  Size: $(du -h "$zip_path" | cut -f1)"
    echo ""

    echo -e "${GREEN}Files:${NC}"
    echo "  Module ZIP: $zip_path"

    if [ -f "${zip_path}.sha256" ]; then
        echo "  Checksum: ${zip_path}.sha256"
    fi

    if [ -f "$DIST_DIR/${MODULE_ID}-${MODULE_VERSION}-RELEASE_NOTES.txt" ]; then
        echo "  Release Notes: $DIST_DIR/${MODULE_ID}-${MODULE_VERSION}-RELEASE_NOTES.txt"
    fi

    echo ""

    echo -e "${GREEN}Next Steps:${NC}"
    echo "  1. Test the module on a device"
    echo "  2. Flash ZIP in Magisk Manager"
    echo "  3. Check logs: adb shell cat /data/adb/cco/logs/module.log"
    echo ""

    echo -e "${GREEN}Build completed successfully!${NC}"
}

# Main function
main() {
    print_header "CCO Module Packaging"
    echo "Module directory: $MODULE_DIR"
    echo ""

    read_module_info
    echo ""

    clean_build
    copy_module_files
    set_permissions
    validate_build
    create_zip
    create_checksum
    generate_release_notes
    print_summary
}

# Parse command line options
SKIP_TESTS=0

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-tests)
            SKIP_TESTS=1
            shift
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --skip-tests    Skip running tests before packaging"
            echo "  --help          Show this help message"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Run tests before packaging (unless skipped)
if [ $SKIP_TESTS -eq 0 ] && [ -f "$SCRIPT_DIR/test.sh" ]; then
    print_info "Running tests before packaging..."
    echo ""

    if bash "$SCRIPT_DIR/test.sh"; then
        print_success "Tests passed, proceeding with packaging"
        echo ""
    else
        print_error "Tests failed! Fix issues before packaging."
        print_info "Use --skip-tests to package anyway (not recommended)"
        exit 1
    fi
fi

main
exit $?
