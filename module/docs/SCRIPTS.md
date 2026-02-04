# CCO Module Development Scripts

Comprehensive tooling for developing, testing, and packaging the CCO Magisk module.

## Available Scripts

### 🧪 `test.sh` - Unit & Validation Tests
Runs comprehensive validation tests on module structure and configuration.

**Tests performed:**
- Module structure validation
- Script syntax checking
- File permissions verification
- XML profile validation
- module.prop validation
- Script content analysis
- Documentation completeness
- Profile content validation
- Logging implementation
- Safety features verification

**Usage:**
```bash
./scripts/test.sh
```

**Output:**
- ✅ Green: Tests passed
- ❌ Red: Tests failed
- ⚠️ Yellow: Warnings

**Exit codes:**
- `0` - All tests passed
- `1` - Some tests failed

---

### 🔍 `lint.sh` - Code Linting & Formatting
Validates and formats shell scripts and XML files.

**Features:**
- Shell script syntax validation (shellcheck)
- Code formatting (shfmt)
- XML validation and formatting (xmllint)
- Line ending normalization (CRLF → LF)
- Trailing whitespace removal
- Permission verification

**Usage:**
```bash
# Run with formatting
./scripts/lint.sh

# Check only (no modifications)
./scripts/lint.sh --check-only
```

**Dependencies (optional but recommended):**
```bash
# macOS
brew install shellcheck shfmt libxml2

# Ubuntu/Debian
apt install shellcheck libxml2-utils
# shfmt: download from https://github.com/mvdan/sh

# Fedora
dnf install shellcheck libxml2
```

**Output:**
- Validates all shell scripts and XML files
- Formats code according to standards
- Reports issues and fixes applied

---

### 🔧 `integration-test.sh` - Integration Tests
Simulates module functionality in a test environment.

**Tests performed:**
- Directory structure creation
- Override file deployment
- Path detection algorithm
- Backup functionality
- Profile validation
- Logging system
- Disable flag mechanism
- Script syntax validation

**Usage:**
```bash
./scripts/integration-test.sh
```

**Test environment:**
- Creates temporary test directory at `/tmp/cco-module-test-*`
- Simulates Android data structure
- Tests module scripts in isolation
- Automatic cleanup on exit

---

### 📦 `package.sh` - Module Packaging
Creates a flashable Magisk module ZIP.

**Features:**
- Builds production-ready ZIP
- Validates module structure
- Sets correct permissions
- Generates checksums (SHA256)
- Creates release notes
- Runs tests before packaging

**Usage:**
```bash
# Normal build (with tests)
./scripts/package.sh

# Skip tests (not recommended)
./scripts/package.sh --skip-tests
```

**Output:**
- `dist/cco-carrierconfig-VERSION.zip` - Flashable module
- `dist/cco-carrierconfig-VERSION.zip.sha256` - Checksum
- `dist/cco-carrierconfig-VERSION-RELEASE_NOTES.txt` - Release notes

**Directory structure:**
```
build/          # Temporary build directory
├── module.prop
├── install.sh
├── post-fs-data.sh
├── service.sh
├── uninstall.sh
├── common/
├── profiles/
└── system/     # Empty (required by Magisk)

dist/           # Final output
└── cco-carrierconfig-1.0.0.zip
    cco-carrierconfig-1.0.0.zip.sha256
    cco-carrierconfig-1.0.0-RELEASE_NOTES.txt
```

---

### 🚀 `dev.sh` - Development Helper
Unified interface for all development tasks.

**Commands:**

| Command | Description |
|---------|-------------|
| `test` | Run unit and validation tests |
| `integration` | Run integration tests |
| `lint` | Lint and format code |
| `package` | Build flashable ZIP |
| `all` | Complete build pipeline |
| `clean` | Clean build artifacts |
| `help` | Show help message |

**Usage:**
```bash
# Run individual tasks
./scripts/dev.sh test
./scripts/dev.sh lint
./scripts/dev.sh package

# Full build pipeline
./scripts/dev.sh all

# Clean build artifacts
./scripts/dev.sh clean
```

**Full pipeline (recommended):**
```bash
./scripts/dev.sh all
```

This runs:
1. Linting and formatting
2. Unit tests
3. Integration tests
4. Packaging

---

## Quick Start

### First Time Setup

1. **Make scripts executable:**
   ```bash
   chmod +x scripts/*.sh
   ```

2. **Install optional dependencies:**
   ```bash
   # macOS
   brew install shellcheck shfmt libxml2 zip

   # Ubuntu/Debian
   apt install shellcheck libxml2-utils zip
   ```

3. **Run tests:**
   ```bash
   ./scripts/dev.sh test
   ```

### Development Workflow

#### 1. Make Changes
Edit module files (scripts, profiles, documentation)

#### 2. Validate Changes
```bash
# Quick validation
./scripts/test.sh

# Comprehensive validation
./scripts/dev.sh all
```

#### 3. Package Module
```bash
./scripts/package.sh
```

#### 4. Test on Device
```bash
# Push to device
adb push dist/cco-carrierconfig-*.zip /sdcard/

# Flash in Magisk Manager or via adb:
adb shell su -c 'magisk --install-module /sdcard/cco-carrierconfig-*.zip'
adb reboot
```

---

## Development Best Practices

### Before Committing Code

```bash
# 1. Format and lint
./scripts/lint.sh

# 2. Run all tests
./scripts/test.sh
./scripts/integration-test.sh

# 3. Build and verify
./scripts/package.sh
```

### Before Releasing

```bash
# Complete build pipeline
./scripts/dev.sh all

# Verify output
ls -lh dist/

# Test on device
# (flash the generated ZIP)
```

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build Module

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Install dependencies
        run: |
          sudo apt-get update
          sudo apt-get install -y shellcheck libxml2-utils zip

      - name: Make scripts executable
        run: chmod +x module/scripts/*.sh

      - name: Run tests
        run: ./module/scripts/test.sh

      - name: Run integration tests
        run: ./module/scripts/integration-test.sh

      - name: Lint code
        run: ./module/scripts/lint.sh

      - name: Package module
        run: ./module/scripts/package.sh --skip-tests

      - name: Upload artifact
        uses: actions/upload-artifact@v3
        with:
          name: cco-module
          path: dist/*.zip
```

---

## Troubleshooting

### "Permission denied" errors
```bash
chmod +x scripts/*.sh
```

### "shellcheck: command not found"
Install shellcheck (optional but recommended):
```bash
# macOS
brew install shellcheck

# Ubuntu/Debian
apt install shellcheck
```

Scripts will work without optional tools, but with limited functionality.

### Tests failing
1. Check error output
2. Fix issues in module files
3. Run lint to auto-fix formatting
4. Run tests again

### Packaging errors
1. Ensure all required files exist
2. Run `./scripts/test.sh` first
3. Check module.prop has valid properties
4. Verify profiles/ directory exists

---

## Script Dependencies

### Required (built-in)
- bash
- find
- grep
- sed
- chmod
- zip

### Optional (enhanced functionality)
- **shellcheck** - Shell script linting
- **shfmt** - Shell script formatting
- **xmllint** - XML validation
- **dos2unix** - Line ending conversion

### Installing Optional Tools

**macOS:**
```bash
brew install shellcheck shfmt libxml2
```

**Ubuntu/Debian:**
```bash
apt install shellcheck libxml2-utils
# shfmt: download from https://github.com/mvdan/sh
```

**Fedora:**
```bash
dnf install shellcheck libxml2
```

---

## File Structure

```
scripts/
├── README.md              # This file
├── dev.sh                 # Development helper (unified interface)
├── test.sh                # Unit and validation tests
├── integration-test.sh    # Integration tests
├── lint.sh                # Linting and formatting
└── package.sh             # Module packaging

Generated directories:
../build/                  # Temporary build files
../dist/                   # Final packaged modules
```

---

## Exit Codes

All scripts follow consistent exit code conventions:

- `0` - Success
- `1` - Failure or errors found

Use in scripts:
```bash
if ./scripts/test.sh; then
    echo "Tests passed"
else
    echo "Tests failed"
    exit 1
fi
```

---

## Contributing

When adding new tests or scripts:

1. Follow existing script structure
2. Use color coding for output
3. Provide clear success/failure indicators
4. Return appropriate exit codes
5. Document in this README
6. Test on both macOS and Linux

---

## Version

**Scripts Version:** 1.0.0
**Last Updated:** 2026-02-04
**Compatible with:** CCO Module 1.0.0+

---

## Support

For issues with development scripts:
- GitHub Issues: https://github.com/supermarsx/magisk-carrier-config-override/issues
- Include script name and error output
- Mention your OS and shell version
