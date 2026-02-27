# Installation Guide

Complete installation instructions for the CarrierConfig Override Manager.

## Prerequisites

### Required
- Samsung Galaxy device running One UI 5/6/7 (Android 13-15)
- Root access (Magisk 24.0+)
- USB debugging enabled
- ADB installed on computer (for advanced features)

### Recommended
- Device backup
- Understanding of Android system modification risks
- Non-critical/test device for first-time use

## Component Installation

### 1. Android App (cco-app)

#### Option A: From Release APK
1. Download `cco-app-release.apk` from [Releases](https://github.com/YOUR_USERNAME/magisk-carrier-config-override/releases)
2. Enable "Install from Unknown Sources" in Android settings
3. Install APK on device
4. Grant root access when prompted
5. Grant required permissions (Phone, Storage)

#### Option B: Build from Source
```bash
cd cco-app
./gradlew assembleRelease
adb install app/build/outputs/apk/release/app-release.apk
```

### 2. Magisk Module (cco-carrierconfig)

#### Via CCO App (Recommended)
1. Open CCO app
2. Navigate to CarrierConfig tab
3. Tap "Install Magisk Module"
4. Follow on-screen prompts
5. Reboot when complete

#### Manual Installation
```bash
# Create module zip
cd cco-carrierconfig
zip -r cco-carrierconfig.zip . -x "*.md" -x ".git/*"

# Install via Magisk Manager
# 1. Open Magisk Manager
# 2. Tap "Modules" → "Install from storage"
# 3. Select cco-carrierconfig.zip
# 4. Reboot
```

### 3. Frida Backend (Optional - Method 2)

#### Install Frida Server
```bash
# Download Frida server for your architecture
# arm64 for most modern Samsung devices
wget https://github.com/frida/frida/releases/download/16.1.11/frida-server-16.1.11-android-arm64.xz
unxz frida-server-16.1.11-android-arm64.xz

# Push to device
adb push frida-server-16.1.11-android-arm64 /data/local/tmp/frida-server
adb shell "chmod 755 /data/local/tmp/frida-server"

# Start Frida server (run after each reboot)
adb shell "su -c '/data/local/tmp/frida-server &'"
```

#### Install Python Frida Client (on computer)
```bash
pip3 install frida-tools
```

### 4. CLI Utility (Optional)

```bash
cd ccoctl
chmod +x ccoctl

# Optional: Install system-wide
sudo cp ccoctl /usr/local/bin/
```

## Post-Installation Setup

### First Launch
1. Open CCO app
2. Grant root access when prompted
3. Grant phone state permission when prompted
4. Complete initial device scan
5. Review dashboard for device status

### Verify Installation
```bash
# Check Magisk module status
adb shell "ls -la /data/adb/cco"

# Check app installation
adb shell "pm list packages | grep cco"

# Check Frida (if installed)
frida-ps -U
```

## Permissions

The app requires the following permissions:

- **Root Access**: For CarrierConfig deployment and system queries
- **Phone State**: To read SIM and carrier information
- **Network State**: To monitor IMS status
- **Storage**: To export reports and logs

## Troubleshooting Installation

### "Installation failed"
- Ensure "Install from Unknown Sources" is enabled
- Check device storage space (minimum 50MB free)
- Try uninstalling old version first

### "Root access denied"
- Open Magisk Manager and verify root is working
- Grant CCO superuser access in Magisk
- Reboot and try again

### Magisk module not loading
```bash
# Check Magisk logs
adb shell "cat /data/adb/magisk.log | grep cco"

# Verify module is enabled
adb shell "ls -la /data/adb/modules"
```

### Frida not connecting
```bash
# Verify Frida server is running
adb shell "ps | grep frida"

# Check USB connection
adb devices

# Test connection
frida-ps -U
```

## Uninstallation

### Remove App
```bash
adb uninstall com.supermarsx.carrierconfig
# Or: Settings → Apps → CCO → Uninstall
```

### Remove Magisk Module
1. Open Magisk Manager
2. Tap on CCO module
3. Tap "Uninstall"
4. Reboot

### Clean All Data
```bash
# Remove CCO data directory
adb shell "su -c 'rm -rf /data/adb/cco'"
```

## Next Steps

After installation:
1. Read [Safety Guidelines](SAFETY.md)
2. Follow [Quick Start Guide](README.md#quick-start)
3. Review [Troubleshooting Guide](TROUBLESHOOTING.md)
4. Check device-specific notes (if available)

## Support

For installation issues:
- Check [Troubleshooting Guide](TROUBLESHOOTING.md)
- Search [GitHub Issues](https://github.com/YOUR_USERNAME/magisk-carrier-config-override/issues)
- Ask in [Discussions](https://github.com/YOUR_USERNAME/magisk-carrier-config-override/discussions)
