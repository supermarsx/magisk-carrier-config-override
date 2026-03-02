# Installation Guide

Complete installation instructions for the CarrierConfig Override Manager.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Component Installation](#component-installation)
- [Post-Installation Setup](#post-installation-setup)
- [Verification](#verification)
- [Troubleshooting](#troubleshooting)
- [Uninstallation](#uninstallation)

---

## Prerequisites

### Device Requirements

- **Device**: Samsung smartphone (Galaxy S/A series, Fold, Flip)
- **Android Version**: Android 13–15 (API 33–35)
- **Firmware**: One UI 5/6/7
- **Architecture**: ARM64 (arm64-v8a)
- **Storage**: 100+ MB free space

### Root Access

- **Root**: Required (Magisk 24.0+ or KernelSU)
- **Magisk Manager**: Latest version installed and working
- **Root Shell**: Verified working (test with `su` command)

### Recommended

- Device backup before installation
- Non-critical/test device for first-time use
- USB debugging enabled
- ADB installed on computer (for advanced features)

---

## Component Installation

### 1. Android App

#### Option A: From Release APK (Recommended)

1. **Download the APK** from [Releases](https://github.com/supermarsx/magisk-carrier-config-override/releases)

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
   - Magisk prompt will appear — tap "Grant"
   - Choose "Always allow" for convenience

#### Option B: ADB Install

```bash
adb devices
adb install cco-manager.apk
adb shell am start -n com.supermarsx.carrierconfig/.MainActivity
```

#### Option C: Build from Source

```bash
git clone https://github.com/supermarsx/magisk-carrier-config-override.git
cd magisk-carrier-config-override/app
./scripts/build.sh
adb install app/build/outputs/apk/release/app-release.apk
```

### 2. Magisk Module

#### Via CCO App (Recommended)

1. Open CCO app
2. Navigate to CarrierConfig tab
3. Tap "Install Magisk Module"
4. Follow on-screen prompts
5. Reboot when complete

#### Manual Installation

```bash
cd module
zip -r cco-carrierconfig.zip . -x "*.md" -x ".git/*" -x "tests/*" -x "scripts/*"

# Install via Magisk Manager:
# 1. Open Magisk Manager
# 2. Tap "Modules" → "Install from storage"
# 3. Select cco-carrierconfig.zip
# 4. Reboot
```

### 3. Frida Backend (Optional — Method 2)

#### Install Frida Server

```bash
# Download for ARM64 (most modern Samsung devices)
wget https://github.com/frida/frida/releases/latest/download/frida-server-android-arm64.xz
unxz frida-server-android-arm64.xz

# Push to device
adb push frida-server-android-arm64 /data/local/tmp/frida-server
adb shell "chmod 755 /data/local/tmp/frida-server"

# Start Frida server (run after each reboot)
adb shell "su -c '/data/local/tmp/frida-server &'"
```

#### Install Python Frida Client (on computer)

```bash
pip3 install frida-tools
```

### 4. LSPosed Module (Optional — Method 2)

1. Install LSPosed framework (follow [LSPosed docs](https://github.com/LSPosed/LSPosed))
2. Build and install CCO LSPosed module:

   ```bash
   cd instrumentation/lsposed
   ./gradlew assembleRelease
   adb install -r build/outputs/apk/release/lsposed-release.apk
   ```

3. Enable in LSPosed Manager → select target scope → reboot

### 5. CLI Utility (Optional)

```bash
chmod +x cli/ccoctl

# Optional: Add to PATH
sudo cp cli/ccoctl /usr/local/bin/
```

---

## Post-Installation Setup

### First Launch

1. **Root Permission Prompt** — Magisk will ask for root access. Tap "Grant" and enable "Remember choice."
2. **Permissions** — Grant Phone and Storage permissions when prompted.
3. **Dashboard** — Review device info, SIM status, IMS status, and WFC UI detection.

### Initial Diagnostics

1. Tap **"Run Diagnostics"** on the Dashboard
2. Wait for scan to complete (5–10 seconds)
3. Review blocker analysis and recommendations

### Configure Settings

- **General**: Enable auto-refresh, configure notifications
- **Appearance**: Select theme (Dark/AMOLED), adjust glass effect strength
- **Advanced**: Enable debug mode for detailed logs, set export directory

---

## Verification

### Verify Root Access

```bash
adb shell su -c whoami
# Should output: root
```

The Dashboard's Device Info card should show "Root: Yes".

### Verify All Components

```bash
# Check app installation
adb shell "pm list packages | grep supermarsx"

# Check Magisk module status
adb shell "ls -la /data/adb/modules/cco*"

# Check Frida (if installed)
frida-ps -U
```

### Verify App Functionality

1. **Dashboard**: All cards display data (not "Unknown")
2. **Diagnostics**: Logs appear in Logcat tab
3. **CarrierConfig**: Prerequisites show green checkmarks
4. **Settings**: All options save and persist

---

## Troubleshooting

### Installation Failed

- Ensure "Install from Unknown Sources" is enabled
- Check device storage space (minimum 50MB free)
- Try uninstalling old version first

### Root Access Denied

1. Open Magisk Manager
2. Check if CCO Manager is in granted list
3. If denied, remove and re-grant
4. Reboot device and try again

### App Crashes on Launch

1. Check Android version (must be 13+)
2. Clear app data: Settings → Apps → CCO Manager → Storage → Clear Data
3. Reinstall APK
4. Check logcat: `adb logcat | grep CCO`

### No Data in Dashboard

1. Grant Phone permission: Settings → Apps → CCO Manager → Permissions
2. Insert active SIM card
3. Pull down to refresh
4. Check if root access is granted

### Magisk Module Not Loading

```bash
adb shell "cat /data/adb/magisk.log | grep cco"
adb shell "ls -la /data/adb/modules"
```

### Frida Not Connecting

```bash
adb shell "ps | grep frida"
adb devices
frida-ps -U
```

For more troubleshooting, see the [Troubleshooting Guide](TROUBLESHOOTING.md).

---

## Uninstallation

### Remove App

```bash
adb uninstall com.supermarsx.carrierconfig
# Or: Settings → Apps → CCO Manager → Uninstall
```

### Remove Magisk Module

1. Open Magisk Manager → Modules → CCO → Uninstall → Reboot

### Revert CarrierConfig Changes

Before uninstalling, open the app → CarrierConfig tab → "Revert Override" → Reboot.

### Complete Cleanup

```bash
adb shell su -c "pm uninstall com.supermarsx.carrierconfig && \
  rm -rf /sdcard/CCO/ && \
  rm -rf /data/data/com.supermarsx.carrierconfig/ && \
  rm -rf /data/adb/cco/"
adb reboot
```

---

## Next Steps

- [Safety Guidelines](SAFETY.md) — Read before making system changes
- [Documentation Index](README.md) — Full docs navigation
- [Troubleshooting](TROUBLESHOOTING.md) — Detailed problem solving
- [Export/Import Guide](EXPORT_IMPORT_GUIDE.md) — Backup and restore configurations
