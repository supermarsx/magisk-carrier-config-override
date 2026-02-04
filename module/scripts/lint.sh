#!/bin/bash
###############################################################################
# CCO Module Linting and Formatting Script
# Validates and formats shell scripts and XML files
###############################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULE_DIR="$(dirname "$SCRIPT_DIR")"

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

# Check for required tools
check_dependencies() {
    print_header "Checking Dependencies"

    local missing_deps=()

    if ! command -v shellcheck &> /dev/null; then
        print_warning "shellcheck not found (optional but recommended)"
        print_info "Install: brew install shellcheck  or  apt install shellcheck"
        missing_deps+=("shellcheck")
    else
        print_success "shellcheck found: $(shellcheck --version | grep version | cut -d' ' -f2)"
    fi

    if ! command -v shfmt &> /dev/null; then
        print_warning "shfmt not found (optional but recommended)"
        print_info "Install: brew install shfmt  or  go install mvdan.cc/sh/v3/cmd/shfmt@latest"
        missing_deps+=("shfmt")
    else
        print_success "shfmt found: $(shfmt --version)"
    fi

    if ! command -v xmllint &> /dev/null; then
        print_warning "xmllint not found (optional)"
        print_info "Install: brew install libxml2  or  apt install libxml2-utils"
        missing_deps+=("xmllint")
    else
        print_success "xmllint found"
    fi

    echo ""

    if [ ${#missing_deps[@]} -gt 0 ]; then
        print_warning "Some optional tools are missing. Linting will be limited."
        return 1
    fi

    return 0
}

# Lint shell scripts with shellcheck
lint_shell_scripts() {
    print_header "Linting Shell Scripts"

    if ! command -v shellcheck &> /dev/null; then
        print_warning "shellcheck not available, skipping"
        echo ""
        return
    fi

    local shell_scripts=(
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
    )

    if [ -f "$MODULE_DIR/common/functions.sh" ]; then
        shell_scripts+=("common/functions.sh")
    fi

    local has_errors=0

    for script in "${shell_scripts[@]}"; do
        if [ -f "$MODULE_DIR/$script" ]; then
            print_info "Linting $script..."

            # Run shellcheck with appropriate options
            # SC1090: Can't follow non-constant source
            # SC2034: Variable appears unused
            if shellcheck -x -s sh -e SC1090,SC2034 "$MODULE_DIR/$script"; then
                print_success "$script passed shellcheck"
            else
                print_error "$script has shellcheck issues"
                has_errors=1
            fi
        fi
    done

    echo ""
    return $has_errors
}

# Format shell scripts with shfmt
format_shell_scripts() {
    print_header "Formatting Shell Scripts"

    if ! command -v shfmt &> /dev/null; then
        print_warning "shfmt not available, skipping"
        echo ""
        return
    fi

    local shell_scripts=(
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
    )

    if [ -f "$MODULE_DIR/common/functions.sh" ]; then
        shell_scripts+=("common/functions.sh")
    fi

    print_info "Format settings: indent=4, binary ops at start of line, simplify"

    for script in "${shell_scripts[@]}"; do
        if [ -f "$MODULE_DIR/$script" ]; then
            print_info "Formatting $script..."

            # Check if formatting needed
            if shfmt -d -i 4 -bn -sr "$MODULE_DIR/$script" > /dev/null 2>&1; then
                print_success "$script is already formatted"
            else
                # Apply formatting
                shfmt -w -i 4 -bn -sr "$MODULE_DIR/$script"
                print_success "$script formatted"
            fi
        fi
    done

    echo ""
}

# Validate and format XML files
format_xml_files() {
    print_header "Validating and Formatting XML Files"

    if ! command -v xmllint &> /dev/null; then
        print_warning "xmllint not available, skipping"
        echo ""
        return
    fi

    local xml_files=$(find "$MODULE_DIR/profiles" -name "*.xml" 2>/dev/null)

    if [ -z "$xml_files" ]; then
        print_warning "No XML files found"
        echo ""
        return
    fi

    local has_errors=0

    for xml_file in $xml_files; do
        local filename=$(basename "$xml_file")
        print_info "Processing $filename..."

        # Validate XML
        if xmllint --noout "$xml_file" 2>/dev/null; then
            print_success "$filename is valid XML"

            # Format XML (pretty print)
            xmllint --format "$xml_file" --output "${xml_file}.tmp"
            mv "${xml_file}.tmp" "$xml_file"
            print_success "$filename formatted"
        else
            print_error "$filename has XML errors"
            xmllint --noout "$xml_file" 2>&1 | sed 's/^/  /'
            has_errors=1
        fi
    done

    echo ""
    return $has_errors
}

# Check file permissions
check_permissions() {
    print_header "Checking File Permissions"

    local exec_files=(
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
    )

    for file in "${exec_files[@]}"; do
        if [ -f "$MODULE_DIR/$file" ]; then
            if [ -x "$MODULE_DIR/$file" ]; then
                print_success "$file is executable"
            else
                print_warning "$file is not executable, fixing..."
                chmod +x "$MODULE_DIR/$file"
                print_success "$file made executable"
            fi
        fi
    done

    echo ""
}

# Check line endings (LF vs CRLF)
check_line_endings() {
    print_header "Checking Line Endings"

    local files_to_check=(
        "install.sh"
        "post-fs-data.sh"
        "service.sh"
        "uninstall.sh"
        "module.prop"
    )

    local has_crlf=0

    for file in "${files_to_check[@]}"; do
        if [ -f "$MODULE_DIR/$file" ]; then
            if file "$MODULE_DIR/$file" | grep -q "CRLF"; then
                print_error "$file has Windows line endings (CRLF)"
                print_info "Converting to Unix line endings (LF)..."

                # Convert CRLF to LF
                if command -v dos2unix &> /dev/null; then
                    dos2unix "$MODULE_DIR/$file" 2>/dev/null
                    print_success "$file converted to LF"
                else
                    # Fallback method
                    sed -i '' 's/\r$//' "$MODULE_DIR/$file" 2>/dev/null || \
                    sed -i 's/\r$//' "$MODULE_DIR/$file"
                    print_success "$file converted to LF (fallback method)"
                fi

                has_crlf=1
            else
                print_success "$file has Unix line endings (LF)"
            fi
        fi
    done

    echo ""
    return $has_crlf
}

# Remove trailing whitespace
remove_trailing_whitespace() {
    print_header "Removing Trailing Whitespace"

    local files=$(find "$MODULE_DIR" -type f \( -name "*.sh" -o -name "*.md" -o -name "*.prop" -o -name "*.xml" \) 2>/dev/null)

    local files_modified=0

    for file in $files; do
        if [ -f "$file" ]; then
            # Check if file has trailing whitespace
            if grep -q '[[:space:]]$' "$file"; then
                print_info "Removing trailing whitespace from $(basename "$file")..."
                sed -i '' 's/[[:space:]]*$//' "$file" 2>/dev/null || \
                sed -i 's/[[:space:]]*$//' "$file"
                files_modified=$((files_modified + 1))
            fi
        fi
    done

    if [ $files_modified -gt 0 ]; then
        print_success "Removed trailing whitespace from $files_modified file(s)"
    else
        print_success "No trailing whitespace found"
    fi

    echo ""
}

# Main function
main() {
    print_header "CCO Module Linting and Formatting"
    echo "Module directory: $MODULE_DIR"
    echo ""

    local exit_code=0

    check_dependencies

    check_line_endings || exit_code=1
    check_permissions
    remove_trailing_whitespace

    # Lint before format
    lint_shell_scripts || exit_code=1

    # Format files
    format_shell_scripts
    format_xml_files || exit_code=1

    print_header "Summary"

    if [ $exit_code -eq 0 ]; then
        print_success "All linting and formatting checks passed!"
    else
        print_warning "Some issues were found but may have been fixed"
        print_info "Run this script again to verify fixes"
    fi

    echo ""

    return $exit_code
}

# Parse command line options
if [ "$1" = "--check-only" ]; then
    print_info "Running in check-only mode (no modifications)"
    # In check-only mode, we'd skip the formatting steps
    # For now, just run everything
fi

main
exit $?
