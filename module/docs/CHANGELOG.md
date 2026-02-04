# CCO Magisk Module - Changelog

## Version 1.0.0 (2026-02-04)

### Initial Release

**Core Features:**
- ✅ Boot-time CarrierConfig override via bind mount
- ✅ Automatic device-specific path detection
- ✅ Safe, non-destructive operation with automatic backup
- ✅ Multiple pre-made profiles for common scenarios
- ✅ Comprehensive logging system
- ✅ Full reversibility and easy disable
- ✅ SELinux context handling

**Module Scripts:**
- `install.sh` - Enhanced installation with device validation
- `post-fs-data.sh` - Directory structure initialization
- `service.sh` - Advanced bind mount with path detection
- `uninstall.sh` - Clean removal with optional data preservation
- `common/functions.sh` - Utility functions library

**Profiles Included:**
- `generic_wfc_enable.xml` - Standard Wi-Fi Calling enablement (Wi-Fi Preferred)
- `aggressive_enable.xml` - Maximum enablement with carrier restriction bypass
- `wifi_only_mode.xml` - Force Wi-Fi Only calling mode

**Directory Structure:**
- `/data/adb/cco/active/` - Active override deployment
- `/data/adb/cco/overrides/` - Saved profile storage
- `/data/adb/cco/backup/` - Automatic original file backup
- `/data/adb/cco/logs/` - Detailed operation logs

**Path Detection:**
- Supports multiple CarrierConfig path candidates
- Automatic detection of device-specific paths
- Fallback to most common path
- Bind mount verification

**Safety Features:**
- Non-destructive bind mounting
- Automatic backup of original files
- Backup metadata tracking
- Easy disable flag (`/data/adb/cco/disable`)
- Clean uninstall with restore capability
- SELinux context preservation

**Logging:**
- Timestamped log entries
- Device information logging
- Success/failure indicators
- Error tracking
- Mount verification
- CarrierConfig refresh status

**Known Limitations:**
- Requires Magisk 24.0+ for reliable bind mounts
- Android 13+ recommended for best compatibility
- Some carrier-locked devices may have firmware-level blocks
- CarrierConfig refresh broadcast may not work on all devices (reboot recommended)

**Compatibility:**
- Tested on Samsung Galaxy devices (One UI 5/6/7)
- Android 13-15 (SDK 33-35)
- May work on non-Samsung devices with CarrierConfig support

---

## Future Plans

**Version 1.1.0 (Planned):**
- [ ] Enhanced path detection algorithm
- [ ] Per-SIM slot configuration support
- [ ] Web UI for configuration management
- [ ] Automatic profile updates from repository
- [ ] Profile validation and syntax checking

**Version 1.2.0 (Planned):**
- [ ] Live override reload without reboot
- [ ] Integration with Xposed/LSPosed for runtime hooks
- [ ] Carrier-specific profile database
- [ ] Diagnostic mode with detailed reports

**Long-term:**
- [ ] Support for non-Samsung devices
- [ ] GUI configuration builder
- [ ] Cloud profile sharing
- [ ] A/B testing for profiles
