#!/bin/bash
# CCO App Development Scripts
# Collection of useful commands for development

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

GRADLE_CMD=""
if [ -f "./gradlew" ]; then
    GRADLE_CMD="./gradlew"
elif command -v gradle >/dev/null 2>&1; then
    GRADLE_CMD="gradle"
fi

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

echo_success() {
    echo -e "${GREEN}✓${NC} $1"
}

echo_error() {
    echo -e "${RED}✗${NC} $1"
}

echo_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

require_gradle() {
    if [ -z "$GRADLE_CMD" ]; then
        echo_error "Gradle not found. Add ./gradlew to app/ or install 'gradle' on PATH."
        return 1
    fi
    return 0
}

# Show help
show_help() {
    cat << EOF
CCO App Development Scripts

Usage: ./scripts/dev.sh [command]

Commands:
    lint            Run Kotlin linter (detekt)
    format          Format Kotlin code (ktlint)
    format-check    Check code formatting without changes
    type-check      Run type checking and compilation
    test            Run unit tests
    test-ui         Run instrumented UI tests
    build           Build debug APK
    build-release   Build release APK
    install         Build and install debug APK to device
    clean           Clean build artifacts
    check-all       Run all checks (lint + format + type + test)
    adb-logs        Show app logs via adb
    list-devices    List connected Android devices
    help            Show this help message

Examples:
    ./scripts/dev.sh lint
    ./scripts/dev.sh format
    ./scripts/dev.sh build
    ./scripts/dev.sh install

EOF
}

# Lint
run_lint() {
    require_gradle || return 1
    echo_info "Running Kotlin linter..."
    $GRADLE_CMD detekt || {
        echo_error "Linting failed"
        return 1
    }
    echo_success "Linting passed"
}

# Format
run_format() {
    require_gradle || return 1
    echo_info "Formatting Kotlin code..."
    $GRADLE_CMD ktlintFormat || {
        echo_error "Formatting failed"
        return 1
    }
    echo_success "Code formatted"
}

# Format check
run_format_check() {
    require_gradle || return 1
    echo_info "Checking code formatting..."
    $GRADLE_CMD ktlintCheck || {
        echo_error "Format check failed - run './scripts/dev.sh format' to fix"
        return 1
    }
    echo_success "Format check passed"
}

# Type check
run_type_check() {
    require_gradle || return 1
    echo_info "Running type checking and compilation..."
    $GRADLE_CMD compileDebugKotlin || {
        echo_error "Type checking failed"
        return 1
    }
    echo_success "Type checking passed"
}

# Test
run_test() {
    require_gradle || return 1
    echo_info "Running unit tests..."
    $GRADLE_CMD test || {
        echo_error "Tests failed"
        return 1
    }
    echo_success "Tests passed"
}

# UI Test
run_test_ui() {
    echo_info "Running instrumented UI tests..."
    require_gradle || return 1
    if ! command -v adb >/dev/null 2>&1; then
        echo_error "adb not found"
        return 1
    fi
    if ! adb devices | grep -q "device$"; then
        echo_error "No Android device connected"
        return 1
    fi
    $GRADLE_CMD connectedAndroidTest || {
        echo_error "UI tests failed"
        return 1
    }
    echo_success "UI tests passed"
}

# Build
run_build() {
    require_gradle || return 1
    echo_info "Building debug APK..."
    $GRADLE_CMD assembleDebug || {
        echo_error "Build failed"
        return 1
    }
    APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" | head -1)
    if [ -n "$APK_PATH" ]; then
        echo_success "Build successful: $APK_PATH"
    else
        echo_error "APK not found"
        return 1
    fi
}

# Build Release
run_build_release() {
    require_gradle || return 1
    echo_info "Building release APK..."
    $GRADLE_CMD assembleRelease || {
        echo_error "Release build failed"
        return 1
    }
    APK_PATH=$(find app/build/outputs/apk/release -name "*.apk" | head -1)
    if [ -n "$APK_PATH" ]; then
        echo_success "Release build successful: $APK_PATH"
    else
        echo_error "Release APK not found"
        return 1
    fi
}

# Install
run_install() {
    echo_info "Building and installing to device..."
    require_gradle || return 1
    if ! command -v adb >/dev/null 2>&1; then
        echo_error "adb not found"
        return 1
    fi
    if ! adb devices | grep -q "device$"; then
        echo_error "No Android device connected"
        return 1
    fi
    $GRADLE_CMD installDebug || {
        echo_error "Installation failed"
        return 1
    }
    echo_success "App installed successfully"
}

# Clean
run_clean() {
    require_gradle || return 1
    echo_info "Cleaning build artifacts..."
    $GRADLE_CMD clean || {
        echo_error "Clean failed"
        return 1
    }
    echo_success "Build cleaned"
}

# Check all
run_check_all() {
    echo_info "Running all checks..."
    
    run_format_check || return 1
    run_lint || return 1
    run_type_check || return 1
    run_test || return 1
    
    echo_success "All checks passed! 🎉"
}

# ADB logs
run_adb_logs() {
    if ! command -v adb >/dev/null 2>&1; then
        echo_error "adb not found"
        return 1
    fi
    if ! adb devices | grep -q "device$"; then
        echo_error "No Android device connected"
        return 1
    fi
    echo_info "Showing app logs (Ctrl+C to stop)..."
    adb logcat | grep -E "(CCO|CarrierConfig|supermarx)"
}

# List devices
list_devices() {
    echo_info "Connected Android devices:"
    adb devices -l
}

# Main
case "${1:-help}" in
    lint)
        run_lint
        ;;
    format)
        run_format
        ;;
    format-check)
        run_format_check
        ;;
    type-check)
        run_type_check
        ;;
    test)
        run_test
        ;;
    test-ui)
        run_test_ui
        ;;
    build)
        run_build
        ;;
    build-release)
        run_build_release
        ;;
    install)
        run_install
        ;;
    clean)
        run_clean
        ;;
    check-all)
        run_check_all
        ;;
    adb-logs)
        run_adb_logs
        ;;
    list-devices)
        list_devices
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
