#!/bin/bash
# Enhanced test runner script for CCO
# Tests: Unit, Integration, and UI

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

if [ -f "./gradlew" ]; then
    GRADLE_CMD="./gradlew"
elif command -v gradle >/dev/null 2>&1; then
    GRADLE_CMD="gradle"
else
    GRADLE_CMD=""
fi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Banner
echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   CCO Test Suite - Comprehensive Testing Framework      ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
echo ""

# Parse arguments
TEST_TYPE="${1:-all}"
COVERAGE="${2:-false}"

show_help() {
    echo "Usage: ./test.sh [TYPE] [COVERAGE]"
    echo ""
    echo "TEST_TYPE:"
    echo "  all         - Run all tests (default)"
    echo "  unit        - Run unit tests only"
    echo "  integration - Run integration tests only"
    echo "  ui          - Run UI tests only"
    echo "  quick       - Run quick unit tests"
    echo ""
    echo "COVERAGE:"
    echo "  coverage    - Generate code coverage report"
    echo ""
    echo "Examples:"
    echo "  ./test.sh                    # Run all tests"
    echo "  ./test.sh unit               # Unit tests only"
    echo "  ./test.sh all coverage       # All tests with coverage"
    echo ""
}

if [[ "$TEST_TYPE" == "help" ]] || [[ "$TEST_TYPE" == "-h" ]] || [[ "$TEST_TYPE" == "--help" ]]; then
    show_help
    exit 0
fi

# Function to run unit tests
run_unit_tests() {
    if [ -z "$GRADLE_CMD" ]; then
        echo -e "${RED}✗ Gradle not found. Add ./gradlew to app/ or install 'gradle'.${NC}"
        return 1
    fi
    echo -e "${YELLOW}▶ Running Unit Tests...${NC}"
    echo ""
    
    if [[ "$COVERAGE" == "coverage" ]]; then
        echo -e "${BLUE}  • With code coverage enabled${NC}"
        $GRADLE_CMD test jacocoTestReport --continue
    else
        $GRADLE_CMD test --continue
    fi
    
    local EXIT_CODE=$?
    if [ $EXIT_CODE -eq 0 ]; then
        echo -e "${GREEN}✓ Unit Tests: PASSED${NC}"
    else
        echo -e "${RED}✗ Unit Tests: FAILED${NC}"
    fi
    echo ""
    return $EXIT_CODE
}

# Function to run integration tests
run_integration_tests() {
    if [ -z "$GRADLE_CMD" ]; then
        echo -e "${RED}✗ Gradle not found. Add ./gradlew to app/ or install 'gradle'.${NC}"
        return 1
    fi
    echo -e "${YELLOW}▶ Running Integration Tests...${NC}"
    echo ""
    
    # Check if device/emulator is connected
    if ! command -v adb >/dev/null 2>&1 || ! adb devices | grep -q "device$"; then
        echo -e "${RED}✗ No Android device/emulator detected${NC}"
        echo -e "${YELLOW}  Please install adb and connect a device/emulator${NC}"
        return 1
    fi
    
    echo -e "${BLUE}  • Device detected: $(adb devices | grep device | head -1 | awk '{print $1}')${NC}"
    
    $GRADLE_CMD connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.supermarsx.carrierconfig.integration.RepositoryIntegrationTest
    
    local EXIT_CODE=$?
    if [ $EXIT_CODE -eq 0 ]; then
        echo -e "${GREEN}✓ Integration Tests: PASSED${NC}"
    else
        echo -e "${RED}✗ Integration Tests: FAILED${NC}"
    fi
    echo ""
    return $EXIT_CODE
}

# Function to run UI tests
run_ui_tests() {
    if [ -z "$GRADLE_CMD" ]; then
        echo -e "${RED}✗ Gradle not found. Add ./gradlew to app/ or install 'gradle'.${NC}"
        return 1
    fi
    echo -e "${YELLOW}▶ Running UI Tests...${NC}"
    echo ""
    
    # Check if device/emulator is connected
    if ! command -v adb >/dev/null 2>&1 || ! adb devices | grep -q "device$"; then
        echo -e "${RED}✗ No Android device/emulator detected${NC}"
        echo -e "${YELLOW}  Please install adb and connect a device/emulator${NC}"
        return 1
    fi
    
    echo -e "${BLUE}  • Device detected: $(adb devices | grep device | head -1 | awk '{print $1}')${NC}"
    echo -e "${BLUE}  • Running Compose UI tests${NC}"
    
    $GRADLE_CMD connectedAndroidTest
    
    local EXIT_CODE=$?
    if [ $EXIT_CODE -eq 0 ]; then
        echo -e "${GREEN}✓ UI Tests: PASSED${NC}"
    else
        echo -e "${RED}✗ UI Tests: FAILED${NC}"
    fi
    echo ""
    return $EXIT_CODE
}

# Function to run quick tests
run_quick_tests() {
    if [ -z "$GRADLE_CMD" ]; then
        echo -e "${RED}✗ Gradle not found. Add ./gradlew to app/ or install 'gradle'.${NC}"
        return 1
    fi
    echo -e "${YELLOW}▶ Running Quick Unit Tests (Repository layer only)...${NC}"
    echo ""
    
    $GRADLE_CMD test --tests "*Repository*Test" --continue
    
    local EXIT_CODE=$?
    if [ $EXIT_CODE -eq 0 ]; then
        echo -e "${GREEN}✓ Quick Tests: PASSED${NC}"
    else
        echo -e "${RED}✗ Quick Tests: FAILED${NC}"
    fi
    echo ""
    return $EXIT_CODE
}

# Function to show test results
show_test_results() {
    echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║                    Test Results Summary                   ║${NC}"
    echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
    echo ""
    
    echo -e "${YELLOW}Unit Test Report:${NC}"
    echo "  file://$(pwd)/app/build/reports/tests/testDebugUnitTest/index.html"
    echo ""
    
    if [[ "$COVERAGE" == "coverage" ]]; then
        echo -e "${YELLOW}Coverage Report:${NC}"
        echo "  file://$(pwd)/app/build/reports/jacoco/jacocoTestReport/html/index.html"
        echo ""
    fi
    
    if command -v adb >/dev/null 2>&1 && adb devices | grep -q "device$"; then
        echo -e "${YELLOW}Android Test Report:${NC}"
        echo "  file://$(pwd)/app/build/reports/androidTests/connected/index.html"
        echo ""
    fi
}

# Function to clean test artifacts
clean_tests() {
    if [ -z "$GRADLE_CMD" ]; then
        echo -e "${RED}✗ Gradle not found. Add ./gradlew to app/ or install 'gradle'.${NC}"
        return 1
    fi
    echo -e "${YELLOW}▶ Cleaning test artifacts...${NC}"
    $GRADLE_CMD cleanTest cleanTestDebugUnitTest
    echo -e "${GREEN}✓ Cleaned${NC}"
    echo ""
}

# Main test execution
main() {
    local START_TIME=$(date +%s)
    local FAILED=0
    
    echo -e "${BLUE}Configuration:${NC}"
    echo -e "  Test Type: ${YELLOW}$TEST_TYPE${NC}"
    echo -e "  Coverage:  ${YELLOW}$COVERAGE${NC}"
    echo ""
    
    case "$TEST_TYPE" in
        "all")
            echo -e "${GREEN}═══════════════════════════════════════${NC}"
            echo -e "${GREEN}    Running Complete Test Suite       ${NC}"
            echo -e "${GREEN}═══════════════════════════════════════${NC}"
            echo ""
            
            run_unit_tests || FAILED=1
            run_integration_tests || FAILED=1
            run_ui_tests || FAILED=1
            ;;
            
        "unit")
            run_unit_tests || FAILED=1
            ;;
            
        "integration")
            run_integration_tests || FAILED=1
            ;;
            
        "ui")
            run_ui_tests || FAILED=1
            ;;
            
        "quick")
            run_quick_tests || FAILED=1
            ;;
            
        "clean")
            clean_tests
            exit 0
            ;;
            
        *)
            echo -e "${RED}Unknown test type: $TEST_TYPE${NC}"
            show_help
            exit 1
            ;;
    esac
    
    local END_TIME=$(date +%s)
    local DURATION=$((END_TIME - START_TIME))
    
    echo ""
    echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║                    Test Execution Complete                ║${NC}"
    echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "  Duration: ${YELLOW}${DURATION}s${NC}"
    echo ""
    
    if [ $FAILED -eq 0 ]; then
        echo -e "${GREEN}✓ All tests PASSED! 🎉${NC}"
        show_test_results
        exit 0
    else
        echo -e "${RED}✗ Some tests FAILED${NC}"
        echo -e "${YELLOW}  Check test reports for details${NC}"
        show_test_results
        exit 1
    fi
}

# Run main function
main
