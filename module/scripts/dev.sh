#!/bin/bash
###############################################################################
# CCO Module Development Helper
# Unified script for development tasks
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

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

print_usage() {
    cat << EOF
CCO Module Development Helper

Usage: $(basename "$0") <command> [options]

Commands:
    test            Run unit and validation tests
    integration     Run integration tests
    lint            Lint and format code
    package         Build flashable ZIP
    all             Run lint, test, and package
    clean           Clean build artifacts
    help            Show this help message

Options:
    --skip-tests    Skip tests (for package command)
    --check-only    Check only, no modifications (for lint command)

Examples:
    $(basename "$0") test              # Run tests
    $(basename "$0") lint              # Lint and format code
    $(basename "$0") package           # Build module ZIP
    $(basename "$0") all               # Complete build pipeline

EOF
}

run_tests() {
    print_header "Running Tests"
    bash "$SCRIPT_DIR/test.sh"
}

run_integration_tests() {
    print_header "Running Integration Tests"
    bash "$SCRIPT_DIR/integration-test.sh"
}

run_lint() {
    print_header "Running Linter"
    bash "$SCRIPT_DIR/lint.sh" "$@"
}

run_package() {
    print_header "Packaging Module"
    bash "$SCRIPT_DIR/package.sh" "$@"
}

run_all() {
    print_header "Running Full Build Pipeline"

    echo ""
    echo "Step 1/4: Linting..."
    if ! bash "$SCRIPT_DIR/lint.sh"; then
        echo -e "${YELLOW}Linting found issues but continuing...${NC}"
    fi

    echo ""
    echo "Step 2/4: Unit Tests..."
    if ! bash "$SCRIPT_DIR/test.sh"; then
        echo -e "${RED}Tests failed!${NC}"
        exit 1
    fi

    echo ""
    echo "Step 3/4: Integration Tests..."
    if ! bash "$SCRIPT_DIR/integration-test.sh"; then
        echo -e "${YELLOW}Integration tests had issues but continuing...${NC}"
    fi

    echo ""
    echo "Step 4/4: Packaging..."
    if ! bash "$SCRIPT_DIR/package.sh" --skip-tests; then
        echo -e "${RED}Packaging failed!${NC}"
        exit 1
    fi

    echo ""
    print_header "Build Pipeline Complete"
    echo -e "${GREEN}Module is ready for testing!${NC}"
}

clean_build() {
    print_header "Cleaning Build Artifacts"

    local module_dir="$(dirname "$SCRIPT_DIR")"
    local project_dir="$(dirname "$module_dir")"

    if [ -d "$project_dir/build" ]; then
        rm -rf "$project_dir/build"
        echo -e "${GREEN}Removed: build/${NC}"
    fi

    if [ -d "$project_dir/dist" ]; then
        echo -e "${YELLOW}Keeping: dist/ (contains packaged modules)${NC}"
        echo -e "${YELLOW}To remove manually: rm -rf $project_dir/dist${NC}"
    fi

    # Clean test environments
    rm -rf /tmp/cco-module-test-* 2>/dev/null || true

    echo ""
    echo -e "${GREEN}Cleanup complete${NC}"
}

# Main
if [ $# -eq 0 ]; then
    print_usage
    exit 1
fi

command=$1
shift

case "$command" in
    test)
        run_tests "$@"
        ;;
    integration)
        run_integration_tests "$@"
        ;;
    lint)
        run_lint "$@"
        ;;
    package)
        run_package "$@"
        ;;
    all)
        run_all "$@"
        ;;
    clean)
        clean_build "$@"
        ;;
    help|--help|-h)
        print_usage
        ;;
    *)
        echo -e "${RED}Unknown command: $command${NC}"
        echo ""
        print_usage
        exit 1
        ;;
esac

exit $?
