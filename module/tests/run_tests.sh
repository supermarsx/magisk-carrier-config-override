#!/usr/bin/env bash
###############################################################################
# CCO Module Test Runner
# Runs all test suites or specific test categories
###############################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
export MODULE_DIR

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Test results
TOTAL_PASSED=0
TOTAL_FAILED=0
TOTAL_WARNED=0
FAILED_TESTS=()

print_banner() {
    echo ""
    echo -e "${CYAN}╔════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║   CCO Module Comprehensive Tests      ║${NC}"
    echo -e "${CYAN}╚════════════════════════════════════════╝${NC}"
    echo ""
}

run_test() {
    local test_file="$1"
    local test_name=$(basename "$test_file" .sh | sed 's/test_//')
    
    echo -e "${CYAN}▶ Running: $test_name tests${NC}"
    
    if bash "$test_file"; then
        echo -e "${GREEN}✓ $test_name tests passed${NC}"
        return 0
    else
        echo -e "${RED}✗ $test_name tests failed${NC}"
        FAILED_TESTS+=("$test_name")
        return 1
    fi
}

run_all_tests() {
    local test_files=(
        "$SCRIPT_DIR/test_structure.sh"
        "$SCRIPT_DIR/test_syntax.sh"
        "$SCRIPT_DIR/test_permissions.sh"
        "$SCRIPT_DIR/test_xml.sh"
        "$SCRIPT_DIR/test_metadata.sh"
        "$SCRIPT_DIR/test_functions.sh"
        "$SCRIPT_DIR/test_security.sh"
        "$SCRIPT_DIR/test_service_logic.sh"
        "$SCRIPT_DIR/test_uninstall.sh"
        "$SCRIPT_DIR/test_functions_unit.sh"
        "$SCRIPT_DIR/test_edge_cases.sh"
        "$SCRIPT_DIR/test_install_logic.sh"
    )
    
    local passed=0
    local failed=0
    
    for test in "${test_files[@]}"; do
        if [ -f "$test" ]; then
            if run_test "$test"; then
                passed=$((passed + 1))
            else
                failed=$((failed + 1))
            fi
            echo ""
        fi
    done
    
    # Print final summary
    echo ""
    echo -e "${CYAN}════════════════════════════════════════${NC}"
    echo -e "${CYAN}Final Test Summary${NC}"
    echo -e "${CYAN}════════════════════════════════════════${NC}"
    echo -e "${GREEN}Test suites passed: $passed${NC}"
    echo -e "${RED}Test suites failed: $failed${NC}"
    
    if [ $failed -eq 0 ]; then
        echo ""
        echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
        echo -e "${GREEN}║     ✓ ALL TESTS PASSED!                ║${NC}"
        echo -e "${GREEN}╚════════════════════════════════════════╝${NC}"
        return 0
    else
        echo ""
        echo -e "${RED}╔════════════════════════════════════════╗${NC}"
        echo -e "${RED}║     ✗ SOME TESTS FAILED                ║${NC}"
        echo -e "${RED}╚════════════════════════════════════════╝${NC}"
        echo ""
        echo "Failed test suites:"
        for test in "${FAILED_TESTS[@]}"; do
            echo -e "${RED}  - $test${NC}"
        done
        return 1
    fi
}

show_usage() {
    echo "Usage: $0 [test-category]"
    echo ""
    echo "Available test categories:"
    echo "  structure      - Module structure and required files"
    echo "  syntax         - Shell script syntax validation"
    echo "  permissions    - File permission checks"
    echo "  xml            - XML profile validation"
    echo "  metadata       - Module metadata and documentation"
    echo "  functions      - Functions library tests"
    echo "  security       - Security checks"
    echo "  service_logic  - Service script logic paths"
    echo "  uninstall      - Uninstall/cleanup logic"
    echo "  functions_unit - Function library unit tests"
    echo "  edge_cases     - Edge case and boundary tests"
    echo "  install_logic  - Install script logic"
    echo "  all            - Run all tests (default)"
    echo ""
    echo "Examples:"
    echo "  $0              # Run all tests"
    echo "  $0 all          # Run all tests"
    echo "  $0 security     # Run only security tests"
    echo "  $0 xml          # Run only XML tests"
}

# Main execution
main() {
    # Make all test scripts executable
    chmod +x "$SCRIPT_DIR"/*.sh 2>/dev/null || true
    
    print_banner
    echo "Module directory: $MODULE_DIR"
    echo ""
    
    case "${1:-all}" in
        structure)
            run_test "$SCRIPT_DIR/test_structure.sh"
            ;;
        syntax)
            run_test "$SCRIPT_DIR/test_syntax.sh"
            ;;
        permissions)
            run_test "$SCRIPT_DIR/test_permissions.sh"
            ;;
        xml)
            run_test "$SCRIPT_DIR/test_xml.sh"
            ;;
        metadata)
            run_test "$SCRIPT_DIR/test_metadata.sh"
            ;;
        functions)
            run_test "$SCRIPT_DIR/test_functions.sh"
            ;;
        security)
            run_test "$SCRIPT_DIR/test_security.sh"
            ;;
        service_logic)
            run_test "$SCRIPT_DIR/test_service_logic.sh"
            ;;
        uninstall)
            run_test "$SCRIPT_DIR/test_uninstall.sh"
            ;;
        functions_unit)
            run_test "$SCRIPT_DIR/test_functions_unit.sh"
            ;;
        edge_cases)
            run_test "$SCRIPT_DIR/test_edge_cases.sh"
            ;;
        install_logic)
            run_test "$SCRIPT_DIR/test_install_logic.sh"
            ;;
        all)
            run_all_tests
            ;;
        -h|--help|help)
            show_usage
            exit 0
            ;;
        *)
            echo -e "${RED}Error: Unknown test category '$1'${NC}"
            echo ""
            show_usage
            exit 1
            ;;
    esac
}

main "$@"
exit $?
