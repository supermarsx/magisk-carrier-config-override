# Changelog

All notable changes to CCO will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- LSPosed module backend for Method 2
- Record/replay mode for entitlement traces
- Profile sharing and import/export
- Multi-language support
- Auto-update mechanism
- Advanced CarrierConfig editor

## [1.0.0] - TBD

### Added
- Initial release
- Android app with glassmorphism dark theme
- Dashboard showing device/SIM/IMS/WFC status
- Magisk module for CarrierConfig override (Method 1)
- Frida instrumentation for entitlement simulation (Method 2)
- CLI utility (ccoctl) for ADB-friendly operations
- Comprehensive documentation (Install, Safety, Troubleshooting)
- GitHub Actions CI/CD
- Device blocker detection logic
- Root status checking
- SIM information display
- IMS status monitoring

### Components
- **cco-app**: Android application (Jetpack Compose + MVVM)
- **cco-carrierconfig**: Magisk module with bind-mount system
- **cco-entitlement**: Frida scripts and hook profiles
- **ccoctl**: Python CLI utility

### Supported Devices
- Samsung Galaxy devices (One UI 5/6/7)
- Android 13-15 (API 33-35)
- Rooted with Magisk 24.0+

### Known Limitations
- LSPosed backend not yet implemented
- Limited device-specific testing
- No OTA update handling
- Manual Frida server setup required
- Single profile per deployment

## Pre-release Development

### 2026-02-04 - Bootstrap Phase
- Project structure created
- Core architecture implemented
- Initial documentation written
- CI/CD workflows configured

---

## Version History Legend

- **Added**: New features
- **Changed**: Changes in existing functionality
- **Deprecated**: Soon-to-be removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security improvements

---

## Links
- [GitHub Releases](https://github.com/YOUR_USERNAME/magisk-carrier-config-override/releases)
- [Documentation](https://github.com/YOUR_USERNAME/magisk-carrier-config-override/tree/main/docs)
