# CCO Module Test Suite

Modular test framework for the CCO Magisk module.

## 📂 Structure

```
tests/
├── run_tests.sh           # Main test runner
├── test_common.sh         # Shared test utilities
├── test_structure.sh      # Module structure tests
├── test_syntax.sh         # Shell script syntax tests
├── test_permissions.sh    # File permission tests
├── test_xml.sh            # XML profile validation
├── test_metadata.sh       # Module metadata tests
├── test_functions.sh      # Functions library tests
├── test_security.sh       # Security checks
└── README.md              # This file
```

## 🚀 Quick Start

### Run All Tests
```bash
./tests/run_tests.sh
# or
./tests/run_tests.sh all
```

### Run Specific Test Category
```bash
./tests/run_tests.sh structure    # Module structure
./tests/run_tests.sh syntax        # Script syntax
./tests/run_tests.sh permissions   # File permissions
./tests/run_tests.sh xml           # XML validation
./tests/run_tests.sh metadata      # Module metadata
./tests/run_tests.sh functions     # Functions library
./tests/run_tests.sh security      # Security checks
```

## 📋 Test Categories

### 1. Structure Tests (`test_structure.sh`)
Validates the module's file and directory structure:
- Required files (module.prop, scripts, README)
- Optional files (system.prop, CHANGELOG)
- Directory structure (profiles/, docs/, common/)
- Profile count validation

**Run:** `./run_tests.sh structure`

### 2. Syntax Tests (`test_syntax.sh`)
Validates shell script syntax:
- Bash syntax validation
- Shebang line checks
- Script parsing errors

**Run:** `./run_tests.sh syntax`

### 3. Permission Tests (`test_permissions.sh`)
Checks file permissions:
- Executable scripts have +x
- Non-executable files lack +x
- Proper permission setup

**Run:** `./run_tests.sh permissions`

### 4. XML Tests (`test_xml.sh`)
Validates XML profiles:
- XML syntax validation (with xmllint if available)
- XML declarations
- carrier_config root elements
- Duplicate key detection
- Boolean value formats
- Critical Wi-Fi Calling keys

**Run:** `./run_tests.sh xml`

### 5. Metadata Tests (`test_metadata.sh`)
Validates module metadata:
- module.prop completeness
- Property format validation
- Version numbering (semantic versioning)
- Module ID format
- README existence and length
- CHANGELOG structure

**Run:** `./run_tests.sh metadata`

### 6. Functions Tests (`test_functions.sh`)
Tests the functions library:
- functions.sh existence
- Function count and definitions
- Documentation presence
- Proper sourcing in scripts
- No name collisions
- Naming conventions

**Run:** `./run_tests.sh functions`

### 7. Security Tests (`test_security.sh`)
Security validation:
- No hardcoded credentials
- Secure file permissions (644, 755)
- No world-writable files (777, 666)
- SELinux context handling
- Input validation
- Command injection checks

**Run:** `./run_tests.sh security`

## 🔧 Writing New Tests

### 1. Create a new test file

```bash
touch tests/test_myfeature.sh
chmod +x tests/test_myfeature.sh
```

### 2. Use the test framework

```bash
#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

test_my_feature() {
    print_header "Testing My Feature"
    
    print_test "Checking something..."
    if [ some_condition ]; then
        pass_test "Test passed"
    else
        fail_test "Test failed"
    fi
    
    # Use warn_test for warnings
    warn_test "This is a warning"
}

test_my_feature
print_test_summary "My Feature Tests"
exit $?
```

### 3. Add to test runner

Edit `run_tests.sh` and add your test:
- Add to `test_files` array in `run_all_tests()`
- Add case in `main()` switch statement
- Update help text

## 📊 Test Output

### Individual Test Suite
```
========================================
Testing Module Structure
========================================
[TEST] Checking required files exist...
[PASS] Required file exists: module.prop
[PASS] Required file exists: install.sh
[WARN] Missing optional file: system.prop
[FAIL] Missing required file: service.sh

========================================
Module Structure Tests Summary
========================================
Passed: 10
Failed: 1
Warnings: 2
✗ Some tests failed
```

### Full Test Run
```
╔════════════════════════════════════════╗
║   CCO Module Comprehensive Tests      ║
╚════════════════════════════════════════╝

▶ Running: structure tests
✓ structure tests passed

▶ Running: syntax tests
✓ syntax tests passed

════════════════════════════════════════
Final Test Summary
════════════════════════════════════════
Test suites passed: 7
Test suites failed: 0

╔════════════════════════════════════════╗
║     ✓ ALL TESTS PASSED!                ║
╚════════════════════════════════════════╝
```

## 🎯 Test Functions Available

From `test_common.sh`:

- `print_header "Title"` - Print colored section header
- `print_test "Description"` - Print test description
- `pass_test "Message"` - Mark test as passed (green)
- `fail_test "Message"` - Mark test as failed (red)
- `warn_test "Message"` - Mark test as warning (yellow)
- `print_test_summary "Suite Name"` - Print summary and return exit code

## 🔄 Integration with Build Pipeline

The test runner is integrated with the development scripts:

```bash
# scripts/dev.sh calls tests via:
./tests/run_tests.sh all

# Or use the legacy test.sh which now wraps this
./scripts/test.sh
```

## 📈 Adding Test Coverage

When adding new module features:

1. Identify the test category (structure, security, etc.)
2. Add tests to the appropriate `test_*.sh` file
3. Or create a new test category if needed
4. Run tests: `./run_tests.sh <category>`
5. Ensure all tests pass before committing

## 🐛 Debugging Tests

### Verbose mode
```bash
bash -x ./tests/test_structure.sh
```

### Run specific function
```bash
source tests/test_common.sh
source tests/test_structure.sh
# Call specific test functions
```

### Check exit codes
```bash
./tests/run_tests.sh structure
echo $?  # 0 = success, 1 = failure
```

## 📝 Best Practices

1. **Modularity**: Keep each test file focused on one aspect
2. **Independence**: Tests should not depend on each other
3. **Clarity**: Use descriptive test messages
4. **Coverage**: Test both success and failure cases
5. **Performance**: Keep tests fast (< 1 second per test)
6. **Warnings**: Use warnings for non-critical issues
7. **Documentation**: Document what each test validates

## 🎨 Color Coding

- 🔵 **Blue** - Headers and test descriptions
- 🟢 **Green** - Passed tests
- 🔴 **Red** - Failed tests
- 🟡 **Yellow** - Warnings

## 🔗 See Also

- [Development Scripts](../docs/SCRIPTS.md) - Build and development tools
- [Module Documentation](../docs/README.md) - User and developer docs
- [Integration Tests](../scripts/integration-test.sh) - End-to-end testing

---

**Last Updated:** 2026-02-04  
**Test Framework Version:** 1.0.0
