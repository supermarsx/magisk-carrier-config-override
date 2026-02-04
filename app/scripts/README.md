# CCO App Development Scripts

Convenient scripts for building, testing, and maintaining the CCO Android app.

## Available Scripts

### 🔨 Development Script (`dev.sh`)

Main development command runner with multiple operations:

```bash
./scripts/dev.sh [command]
```

**Commands:**
- `lint` - Run Kotlin linter (detekt)
- `format` - Format Kotlin code (ktlint)
- `format-check` - Check code formatting without changes
- `type-check` - Run type checking and compilation
- `test` - Run unit tests
- `test-ui` - Run instrumented UI tests
- `build` - Build debug APK
- `build-release` - Build release APK
- `install` - Build and install debug APK to device
- `clean` - Clean build artifacts
- `check-all` - Run all checks (lint + format + type + test)
- `adb-logs` - Show app logs via adb
- `list-devices` - List connected Android devices

**Examples:**
```bash
# Format code
./scripts/dev.sh format

# Run all checks
./scripts/dev.sh check-all

# Build and install
./scripts/dev.sh install

# View logs
./scripts/dev.sh adb-logs
```

### 🏗️ Build Script (`build.sh`)

Quick build with interactive install:

```bash
./scripts/build.sh
```

Features:
- Builds debug APK
- Shows APK size
- Offers to install if device connected

### 🧪 Test Script (`test.sh`)

Run tests with device detection:

```bash
./scripts/test.sh
```

Features:
- Runs unit tests
- Optionally runs instrumentation tests if device connected
- Color-coded output

### 🔍 Lint Script (`lint.sh`)

Check and fix code style:

```bash
# Check code style
./scripts/lint.sh check

# Auto-fix issues
./scripts/lint.sh fix
```

## Quick Start

```bash
# Install dependencies (first time)
brew install ktlint  # macOS

# Format code before committing
./scripts/dev.sh format

# Run all checks
./scripts/dev.sh check-all

# Build and install to device
./scripts/dev.sh install
```

## CI/CD Integration

Use in CI pipelines:

```bash
# Run all checks (exits with error on failure)
./scripts/dev.sh check-all

# Build release
./scripts/dev.sh build-release
```

## Requirements

- Bash shell
- Android SDK with `adb`
- Gradle wrapper (`./gradlew`)
- Optional: `ktlint` for formatting

## Script Organization

```
app/scripts/
├── dev.sh      # Main development script
├── build.sh    # Quick build script
├── test.sh     # Test runner
└── lint.sh     # Code style checker/fixer
```

All scripts are executable and can be run from any directory within the project.
