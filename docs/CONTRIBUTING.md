# Contributing to CCO

Thank you for your interest in contributing to the CarrierConfig Override Manager!

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [How to Contribute](#how-to-contribute)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Submitting Changes](#submitting-changes)
- [Areas Needing Help](#areas-needing-help)

## Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help others learn and grow
- Keep discussions on topic

## How to Contribute

### Reporting Bugs

1. **Search existing issues** first
2. **Create a new issue** with:
   - Clear, descriptive title
   - Device model and firmware version
   - Steps to reproduce
   - Expected vs actual behavior
   - Logs (redacted for privacy)
   - Screenshots if applicable

### Suggesting Features

1. Check if already proposed in Issues/Discussions
2. Open a new issue with `[Feature Request]` prefix
3. Describe use case and benefits
4. Consider implementation complexity

### Contributing Code

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Test thoroughly
5. Commit with clear messages
6. Push to your fork
7. Open a Pull Request

## Development Setup

### Prerequisites

- Android Studio (latest stable)
- Android SDK 33-35
- Kotlin 1.9+
- ADB installed
- Rooted test device
- Git

### Clone and Build

```bash
# Clone repository
git clone https://github.com/YOUR_USERNAME/magisk-carrier-config-override.git
cd magisk-carrier-config-override

# Build Android app
cd cco-app
./gradlew assembleDebug

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk

# Package Magisk module
cd ../cco-carrierconfig
zip -r cco-carrierconfig.zip . -x "*.md" -x ".git/*"
```

### Project Structure

```
magisk-carrier-config-override/
├── cco-app/              # Android application
│   ├── app/
│   │   └── src/main/
│   │       ├── java/      # Kotlin source
│   │       └── res/       # Resources
│   └── build.gradle.kts
├── cco-carrierconfig/    # Magisk module
│   ├── module.prop
│   ├── service.sh
│   └── post-fs-data.sh
├── cco-entitlement/      # Frida instrumentation
│   ├── frida/
│   │   └── agent.js
│   └── shared/
│       └── profiles.json
├── ccoctl/               # CLI utility
│   └── ccoctl            # Python script
└── docs/                  # Documentation
```

## Coding Standards

### Kotlin/Android

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions focused and concise
- Use Compose for UI (no XML layouts)

#### Example

```kotlin
/**
 * Checks if Wi-Fi Calling is available on the device
 * 
 * @param context Application context
 * @return true if WFC settings activity exists
 */
fun isWfcAvailable(context: Context): Boolean {
    val intent = Intent(Settings.ACTION_WIFI_CALLING_SETTINGS)
    val activities = context.packageManager.queryIntentActivities(intent, 0)
    return activities.isNotEmpty()
}
```

### Shell Scripts (Magisk)

- Use POSIX-compliant syntax
- Add error handling
- Log all operations
- Include comments for complex logic

#### Example

```bash
#!/system/bin/sh

CCO_DATA="/data/adb/cco"
LOG_FILE="$CCO_DATA/logs/module.log"

log_msg() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" >> "$LOG_FILE"
}

log_msg "Starting operation..."
```

### JavaScript (Frida)

- Use ES6+ syntax
- Add console logging for debugging
- Handle errors gracefully
- Comment hook purposes

#### Example

```javascript
/**
 * Hook IMS entitlement check
 * Forces return value to true for WFC availability
 */
Java.perform(() => {
    const ImsManager = Java.use("com.sec.ims.ImsManager");
    
    ImsManager.isWfcEntitled.implementation = function() {
        console.log("[CCO] Hooking isWfcEntitled");
        return true;  // Force entitled
    };
});
```

### Python (CLI)

- Follow PEP 8
- Type hints for functions
- Docstrings for modules and functions
- Handle exceptions properly

## Testing Guidelines

### Before Submitting

1. **Test on real device**:
   - Fresh install test
   - Upgrade path test
   - Uninstall test

2. **Test multiple scenarios**:
   - With and without root
   - Single and dual SIM
   - Different One UI versions (if possible)

3. **Check logs**:
   - No errors in logcat
   - Module logs show success
   - No ANR/crashes

4. **Verify reversibility**:
   - Changes can be reverted
   - System returns to normal state
   - No persistent issues after uninstall

### Test Checklist

- [ ] Code compiles without errors
- [ ] App installs successfully
- [ ] No crashes on launch
- [ ] Root access works
- [ ] Magisk module loads
- [ ] CarrierConfig overrides apply
- [ ] UI renders correctly
- [ ] Permissions requested appropriately
- [ ] Logs are clean
- [ ] Uninstall is clean

## Submitting Changes

### Pull Request Process

1. **Update documentation** if needed
2. **Add/update tests** if applicable
3. **Follow commit message format**:
   ```
   type(scope): brief description
   
   Longer explanation if needed
   
   Fixes #123
   ```
   
   Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

4. **Keep PRs focused**:
   - One feature/fix per PR
   - Avoid mixing changes
   - Break large changes into smaller PRs

5. **Respond to review feedback**:
   - Address all comments
   - Make requested changes
   - Re-request review when ready

### Commit Message Examples

```bash
# Good
feat(dashboard): add dual SIM support to info card
fix(magisk): correct bind mount path detection
docs(install): add troubleshooting for root access

# Bad
Update files
Fix stuff
WIP
```

## Areas Needing Help

### High Priority

- **Device Testing**:
  - Test on various Samsung models
  - Document device-specific CarrierConfig paths
  - Verify One UI version compatibility

- **Hook Profiles**:
  - Create profiles for different One UI versions
  - Test on carrier-branded firmware
  - Document method signatures

- **Documentation**:
  - Device-specific guides
  - Video tutorials
  - Translations

### Medium Priority

- **Features**:
  - LSPosed module implementation
  - Record/replay mode for hooks
  - Profile sharing/import
  - Auto-update mechanism

- **UI/UX**:
  - Additional themes
  - Accessibility improvements
  - Better error messages

### Nice to Have

- **Automation**:
  - Automated testing
  - CI/CD improvements
  - Release automation

- **Tools**:
  - Diagnostic script improvements
  - Log analyzer
  - Configuration validator

## Development Tips

### Debugging

```bash
# Real-time logs
adb logcat | grep -i cco

# Magisk module logs
adb shell "su -c 'tail -f /data/adb/cco/logs/module.log'"

# Crash logs
adb logcat -b crash

# Frida debugging
frida -U -n com.sec.imsservice -l agent.js
```

### Testing Magisk Scripts

```bash
# Test service.sh without rebooting
adb shell "su -c '/data/adb/modules/cco-carrierconfig/service.sh'"

# Check bind mount
adb shell "mount | grep override"

# Test configuration
adb shell "dumpsys carrier_config | grep -i wfc"
```

### Building Locally

```bash
# Debug build
cd cco-app
./gradlew assembleDebug

# Release build (requires signing)
./gradlew assembleRelease

# Install directly
./gradlew installDebug
```

## Resources

- [Android Developer Docs](https://developer.android.com/)
- [Magisk Documentation](https://topjohnwu.github.io/Magisk/)
- [Frida Documentation](https://frida.re/docs/)
- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)

## Questions?

- **Discussions**: For questions and general discussion
- **Issues**: For bugs and specific problems
- **Discord/Matrix**: (if available)

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to CCO! 🎉
