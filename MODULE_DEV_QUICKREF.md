# 🚀 CCO Module Development Quick Reference

## One-Command Build

```bash
cd module && ./scripts/dev.sh all
```

This runs: **Lint → Test → Integration → Package** ✅

---

## Individual Commands

```bash
# Test only
./scripts/dev.sh test

# Lint and format
./scripts/dev.sh lint

# Integration tests
./scripts/dev.sh integration

# Package module
./scripts/dev.sh package

# Clean build artifacts
./scripts/dev.sh clean
```

---

## Manual Script Usage

```bash
# Run unit tests
./scripts/test.sh

# Lint and auto-fix
./scripts/lint.sh

# Check only (no changes)
./scripts/lint.sh --check-only

# Integration tests
./scripts/integration-test.sh

# Package with tests
./scripts/package.sh

# Package without tests (faster)
./scripts/package.sh --skip-tests
```

---

## Test Results Location

```
dist/
├── cco-carrierconfig-1.0.0.zip         # Flashable module
├── cco-carrierconfig-1.0.0.zip.sha256  # Checksum
└── cco-carrierconfig-1.0.0-RELEASE_NOTES.txt
```

---

## Device Testing

```bash
# Push to device
adb push dist/cco-carrierconfig-*.zip /sdcard/

# Flash via adb (alternative)
adb shell su -c 'magisk --install-module /sdcard/cco-carrierconfig-*.zip'
adb reboot

# Check logs after boot
adb shell cat /data/adb/cco/logs/module.log

# Check mount status
adb shell mount | grep carrierconfig
```

---

## Deploying Profiles

```bash
# Deploy generic profile
adb shell "su -c 'cp /data/adb/modules/cco-carrierconfig/profiles/generic_wfc_enable.xml /data/adb/cco/active/override.xml && chmod 644 /data/adb/cco/active/override.xml'"

# Deploy aggressive profile
adb shell "su -c 'cp /data/adb/modules/cco-carrierconfig/profiles/aggressive_enable.xml /data/adb/cco/active/override.xml && chmod 644 /data/adb/cco/active/override.xml'"

# Reboot to apply
adb reboot
```

---

## Troubleshooting

```bash
# Check module is loaded
adb shell su -c 'ls -la /data/adb/modules/cco-carrierconfig'

# View logs
adb shell cat /data/adb/cco/logs/module.log

# Check bind mount
adb shell mount | grep override

# Disable module temporarily
adb shell "su -c 'touch /data/adb/cco/disable'"
adb reboot

# Re-enable module
adb shell "su -c 'rm /data/adb/cco/disable'"
adb reboot
```

---

## File Structure Reference

```
module/
├── *.sh                    # Main scripts
├── module.prop             # Version info
├── common/functions.sh     # Utilities
├── profiles/*.xml          # CarrierConfig profiles
└── scripts/                # Development tools
    ├── dev.sh              # Main CLI
    ├── test.sh             # Unit tests
    ├── integration-test.sh # E2E tests
    ├── lint.sh             # Linting
    └── package.sh          # Packaging
```

---

## Version Bumping

1. Edit `module/module.prop`:
   ```properties
   version=1.0.1
   versionCode=2
   ```

2. Update `module/CHANGELOG.md`

3. Rebuild:
   ```bash
   ./scripts/dev.sh all
   ```

---

## Test Coverage

- **62 unit tests:** Structure, syntax, XML, docs, safety
- **8 integration tests:** Mounting, backup, profiles, logging
- **XML validation:** All 3 profiles validated
- **Linting:** Shell scripts + XML files

---

## ShellCheck Warnings

These warnings are **expected and safe** on Android:
- `SC3030/SC3043/SC3054` - POSIX sh arrays/local (Android uses bash/mksh)
- `SC2129` - Redirect style preference
- `SC2015` - Intentional `&&` `||` pattern

Config: `.shellcheckrc` suppresses these

---

## Common Issues

### "Permission denied" when running scripts
```bash
chmod +x scripts/*.sh
```

### Tests fail on macOS vs Linux
- Line endings differ (CRLF vs LF)
- Run `./scripts/lint.sh` to normalize

### Module doesn't work on device
1. Check logs: `/data/adb/cco/logs/module.log`
2. Try aggressive profile
3. Verify SELinux isn't blocking
4. Check mount: `mount | grep carrierconfig`

---

## CI/CD Example (GitHub Actions)

```yaml
name: Build Module
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Install deps
        run: sudo apt-get install -y shellcheck libxml2-utils zip
      - name: Make executable
        run: chmod +x module/scripts/*.sh
      - name: Build
        run: cd module && ./scripts/dev.sh all
      - name: Upload
        uses: actions/upload-artifact@v3
        with:
          name: cco-module
          path: dist/*.zip
```

---

## Distribution Checklist

- [ ] Version bumped in `module.prop`
- [ ] `CHANGELOG.md` updated
- [ ] All tests pass (`./scripts/dev.sh all`)
- [ ] Tested on physical device
- [ ] Release notes generated
- [ ] SHA256 checksum verified
- [ ] GitHub release created
- [ ] XDA thread updated (if applicable)

---

**Last Updated:** 2026-02-04  
**Module Version:** 1.0.0
