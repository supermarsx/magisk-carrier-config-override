# Installation Guide - CarrierConfig Override Manager

Complete installation instructions for setting up and using the CCO Manager.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation Methods](#installation-methods)
- [Initial Setup](#initial-setup)
- [Verification](#verification)
- [Troubleshooting](#troubleshooting)
- [Uninstallation](#uninstallation)

---

## Prerequisites

Before installing CCO Manager, ensure your device meets these requirements:

### Device Requirements

- **Device**: Samsung smartphone (Galaxy S/A series, Fold, Flip)
- **Android Version**: Android 13 (API 33) or higher
- **Firmware**: One UI 5.0 or later recommended
- **Architecture**: ARM64 (arm64-v8a)
- **Storage**: 100+ MB free space

### Root Access

- **Root**: Required (Magisk 24.0+ or KernelSU)
- **Magisk Manager**: Latest version installed and working
- **Root Shell**: Verified working (test with `su` command)

### Carrier Support

- **SIM Card**: Active SIM required for testing
- **Carrier**: Any carrier (unlocked device recommended)
- **Network**: LTE/5G capability for IMS features

---

## Installation Methods

### Method 1: Direct APK Install (Recommended)

1. **Download the APK**
   ```bash
   # Download from releases page
   wget https://github.com/yourusername/cco-manager/releases/latest/download/cco-manager.apk
   ```

2. **Transfer to device**
   ```bash
   adb push cco-manager.apk /sdcard/Download/
   ```

3. **Install APK**
   - Open file manager on device
   - Navigate to Downloads folder
   - Tap `cco-manager.apk`
   - Grant install from unknown sources if prompted
   - Tap "Install"

4. **Grant Root Access**
   - Open CCO Manager
   - Magisk prompt will appear
   - Tap "Grant" to allow root access
   - Choose "Always allow" for convenience

### Method 2: ADB Install

```bash
# Connect device via USB debugging
adb devices

# Install APK
adb install cco-manager.apk

# Launch app
adb shell am start -n com.supermarx.carrierconfig/.MainActivity

# Grant root (Magisk prompt on device)
```

### Method 3: Build from Source

```bash
# Clone repository
git clone https://github.com/yourusername/cco-manager.git
cd cco-manager

# Build APK
cd app
./scripts/build.sh

# Install
adb install app/build/outputs/apk/release/app-release.apk
```

---

## Initial Setup

### 1. First Launch

When you first open CCO Manager:

1. **Root Permission Prompt**
   - Magisk will ask for root access
   - Tap "Grant" to continue
   - Recommended: Enable "Remember choice"

2. **Welcome Screen**
   - Review app features and requirements
   - Tap "Get Started"

3. **Permissions Request**
   - Storage: For exporting reports and configs
   - Phone: For reading SIM and network status
   - Grant all required permissions

### 2. Dashboard Overview

The Dashboard displays:

- **Device Info**: Model, Android version, One UI version
- **SIM Status**: Carrier, network type, registration
- **IMS Status**: VoLTE/VoWiFi availability
- **WFC UI**: Settings activity detection
- **Blocker Analysis**: Identified issues and recommendations

Pull down to refresh status at any time.

### 3. Initial Diagnostics

Run your first diagnostic scan:

1. Tap **"Run Diagnostics"** button on Dashboard
2. Wait for scan to complete (5-10 seconds)
3. Review blocker analysis
4. Check recommendations for enabling WFC

### 4. Configure Settings

Navigate to Settings (bottom nav):

1. **General Settings**
   - Enable auto-refresh (recommended)
   - Configure notifications

2. **Appearance**
   - Select theme (Dark/AMOLED)
   - Adjust glass effect strength

3. **Advanced**
   - Enable debug mode for detailed logs
   - Set export directory

---

## Verification

### Verify Root Access

```bash
# Check root status in Dashboard
# Device Info card should show "Root: Yes"

# Or verify via terminal
adb shell su -c whoami
# Should output: root
```

### Verify App Functionality

1. **Dashboard**: All cards display data (not "Unknown")
2. **Diagnostics**: Logs appear in Logcat tab
3. **CarrierConfig**: Prerequisites show green checkmarks
4. **Settings**: All options save and persist

### Test Basic Features

```bash
# Export diagnostic report
1. Go to Dashboard
2. Tap "Export Report"
3. Check /sdcard/CCO/reports/ for file

# View logs
1. Go to Diagnostics tab
2. Select "Logcat" sub-tab
3. Choose "IMS" filter
4. Logs should appear
```

---

## Troubleshooting

### Root Access Issues

**Problem**: "Root access denied" error

**Solutions**:
1. Open Magisk Manager
2. Check if CCO Manager is in granted list
3. If denied, remove and re-grant
4. Reboot device
5. Try again

### App Crashes on Launch

**Problem**: App force closes immediately

**Solutions**:
1. Check Android version (must be 13+)
2. Clear app data: Settings > Apps > CCO Manager > Storage > Clear Data
3. Reinstall APK
4. Check logcat: `adb logcat | grep CCO`

### No Data in Dashboard

**Problem**: All cards show "Unknown" or empty

**Solutions**:
1. Grant Phone permission: Settings > Apps > CCO Manager > Permissions
2. Insert active SIM card
3. Pull down to refresh
4. Check if root access granted
5. Restart app

### IMS Status Always "Not Registered"

**Problem**: IMS shows not registered despite working VoLTE

**Solutions**:
1. Enable IMS registration: `*#*#4636#*#*` > Phone Info > Turn on VoLTE
2. Wait 30 seconds for registration
3. Refresh Dashboard
4. Check carrier config: Some carriers hide IMS status

### WFC Settings Not Found

**Problem**: "Settings Activity: Not Found" despite WFC support

**Solutions**:
1. Check if WFC supported by carrier
2. Verify firmware: Some CSC codes disable WFC UI
3. Try Method 1 (CarrierConfig Override) to expose UI
4. Check Settings > Connections > Wi-Fi Calling manually

### Export Reports Fail

**Problem**: "Export failed" error

**Solutions**:
1. Grant Storage permission
2. Check available storage: Need 10+ MB free
3. Try different export directory in Settings
4. Check SELinux: `adb shell getenforce` (Permissive works best)

### Logs Not Appearing

**Problem**: Diagnostics > Logcat shows empty

**Solutions**:
1. Grant root access
2. Enable debug mode in Settings
3. Filter by category (try "All")
4. Check if logcat accessible: `adb shell su -c logcat -d | head`

---

## Uninstallation

### Standard Uninstall

1. **Remove App**
   ```bash
   adb uninstall com.supermarx.carrierconfig
   ```

2. **Clean Up Data**
   ```bash
   # Remove exports and configs
   adb shell rm -rf /sdcard/CCO/
   
   # Remove internal data (requires root)
   adb shell su -c rm -rf /data/data/com.supermarx.carrierconfig/
   ```

3. **Revert Changes** (if CarrierConfig deployed)
   - Open app before uninstalling
   - Go to CarrierConfig tab
   - Tap "Revert Override"
   - Wait for success message
   - Reboot device

### Complete Removal

```bash
# Remove all traces
adb shell su -c "pm uninstall com.supermarx.carrierconfig && \
  rm -rf /sdcard/CCO/ && \
  rm -rf /data/data/com.supermarx.carrierconfig/ && \
  rm -rf /data/adb/cco/"

# Reboot
adb reboot
```

---

## Next Steps

After successful installation:

1. **Read Documentation**
   - [User Guide](docs/README.md)
   - [Quick Reference](QUICKREF.md)
   - [Troubleshooting Guide](docs/TROUBLESHOOTING.md)

2. **Join Community**
   - [XDA Thread](https://forum.xda-developers.com/)
   - [GitHub Discussions](https://github.com/yourusername/cco-manager/discussions)
   - [Telegram Group](https://t.me/cco_manager)

3. **Report Issues**
   - [Issue Tracker](https://github.com/yourusername/cco-manager/issues)
   - Include device model, Android version, logs
   - Export diagnostic report for detailed info

---

## Support

Need help? Check these resources:

- **Quick Reference**: [QUICKREF.md](QUICKREF.md)
- **Troubleshooting**: [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md)
- **GitHub Issues**: Report bugs and feature requests
- **XDA Forum**: Community support and discussions

**Safety Reminder**: Always backup your data before making system modifications. Use at your own risk.
