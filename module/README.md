# CCO CarrierConfig Override - Magisk Module

Boot-time CarrierConfig override system for enabling Wi-Fi Calling on Samsung devices.

## Overview

This Magisk module applies CarrierConfig overrides at boot time using bind-mount technology. It enables Wi-Fi Calling (VoWiFi) and VoLTE features that may be blocked or restricted by your carrier.

## Features

- ✅ **Boot-time activation** - Overrides apply automatically on every boot
- 🔍 **Device path detection** - Automatically finds correct CarrierConfig paths
- 🛡️ **Safe bind-mounting** - Non-destructive, preserves original files
- 📋 **Multiple profiles** - Pre-made configurations for common scenarios
- 📊 **Comprehensive logging** - Detailed logs for troubleshooting
- ↩️ **Full reversibility** - Easy to disable or uninstall
- 🔒 **SELinux aware** - Handles security contexts appropriately

## Requirements

- Rooted Android device with **Magisk 24.0+**
- **Android 13+** (SDK 33+) recommended
- **Samsung device** (One UI 5/6/7) - optimized but may work on others
- CCO Manager app (recommended for easy configuration)

## Installation

### Method 1: Via Magisk Manager (Recommended)
1. Download the module ZIP
2. Open Magisk Manager
3. Tap **Modules** → **Install from storage**
4. Select the CCO module ZIP
5. Reboot when prompted

### Method 2: Via ADB
```bash
adb push cco-carrierconfig.zip /sdcard/
adb shell su -c 'magisk --install-module /sdcard/cco-carrierconfig.zip'
adb reboot
```

## Quick Start

1. **Install the module** (see above)
2. **Choose a profile** from `profiles/` directory:
   - `generic_wfc_enable.xml` - Standard Wi-Fi Calling enablement
   - `aggressive_enable.xml` - Maximum enablement, bypass restrictions
   - `wifi_only_mode.xml` - Force Wi-Fi Only calling mode
3. **Deploy the profile**:
   ```bash
   cp profiles/generic_wfc_enable.xml /data/adb/cco/active/override.xml
   chmod 644 /data/adb/cco/active/override.xml
   ```
4. **Reboot device**
5. **Check Settings** → Connections → Wi-Fi Calling

## Using CCO Manager App

The easiest way to use this module:

1. Install CCO Manager app
2. Open app and go to **CarrierConfig** tab
3. Select a profile (Generic / Aggressive / Wi-Fi Only)
4. Tap **Deploy Profile**
5. Reboot when prompted
6. Wi-Fi Calling should now appear in Settings

## Directory Structure

```
/data/adb/cco/
├── active/           # Active configuration
│   └── override.xml  # Currently deployed profile
├── overrides/        # Saved profiles (from app)
│   ├── profile1.xml
│   └── profile2.xml
├── backup/           # Original system files
│   ├── override_original.xml
│   └── backup_info.txt
└── logs/             # Module logs
    ├── module.log
    └── uninstall.log
```

## How It Works

### Boot Process

1. **post-fs-data.sh** - Creates `/data/adb/cco` directory structure
2. **service.sh** - After boot:
   - Detects device's CarrierConfig path
   - Backs up original override file (if exists)
   - Bind-mounts `/data/adb/cco/active/override.xml` to system path
   - Triggers CarrierConfig refresh broadcast
   - Logs all operations

### Supported Paths

Module automatically detects and uses the correct path for your device:

- `/data/vendor/carrierconfig/override.xml` (most common)
- `/data/vendor/carrierconfig/override_carrier.xml`
- `/data/misc/carrierconfig/override.xml`
- `/data/user_de/0/com.android.phone/files/carrierconfig_override.xml`

## Available Profiles

See `profiles/README.md` for detailed information about each profile.

| Profile | Mode | Description |
|---------|------|-------------|
| `generic_wfc_enable.xml` | Wi-Fi Preferred | Standard enablement for most carriers |
| `aggressive_enable.xml` | Wi-Fi Preferred | Maximum enablement, bypasses restrictions |
| `wifi_only_mode.xml` | Wi-Fi Only | Forces all calls through Wi-Fi |

## Creating Custom Profiles

1. Copy an existing profile from `profiles/` directory
2. Modify the XML values:
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <carrier_config>
       <!-- Enable Wi-Fi Calling -->
       <boolean name="carrier_wfc_ims_available_bool" value="true" />
       <boolean name="carrier_default_wfc_ims_enabled_bool" value="true" />

       <!-- Set calling mode: 0=Cellular, 1=Wi-Fi Preferred, 2=Wi-Fi Only -->
       <int name="carrier_default_wfc_ims_mode_int" value="1" />

       <!-- Add more keys as needed -->
   </carrier_config>
   ```
3. Save to `/data/adb/cco/active/override.xml`
4. Reboot

### Common Configuration Keys

**Boolean Keys:**
- `carrier_wfc_ims_available_bool` - Enable Wi-Fi Calling feature
- `carrier_default_wfc_ims_enabled_bool` - Default enabled state
- `editable_wfc_mode_bool` - Allow user to change mode
- `carrier_volte_available_bool` - Enable VoLTE
- `require_entitlement_checks_bool` - Carrier entitlement checks

**Integer Keys:**
- `carrier_default_wfc_ims_mode_int` - Default calling mode:
  - `0` = Cellular Preferred
  - `1` = Wi-Fi Preferred
  - `2` = Wi-Fi Only
- `carrier_default_wfc_ims_roaming_mode_int` - Roaming mode (same values)

## Troubleshooting

### Override not taking effect

**1. Check logs:**
```bash
cat /data/adb/cco/logs/module.log
# or via adb:
adb shell su -c 'cat /data/adb/cco/logs/module.log'
```

**2. Verify active override file:**
```bash
ls -l /data/adb/cco/active/override.xml
# Should show: -rw-r--r-- ... override.xml
```

**3. Check module status in Magisk:**
- Module should be enabled (not grayed out)
- Check for any error messages

**4. Verify bind mount:**
```bash
mount | grep carrierconfig
# Should show bind mount from /data/adb/cco/active/override.xml
```

### Wi-Fi Calling still not showing

1. **Try aggressive profile:** Deploy `aggressive_enable.xml` instead
2. **Check SIM support:** Verify your SIM/carrier actually supports Wi-Fi Calling
3. **Check IMS registration:** Settings → About Phone → Status
4. **CSC restrictions:** Some Samsung CSCs completely disable the feature
5. **Firmware limitations:** Some carrier-locked firmwares remove Wi-Fi Calling entirely

### Device issues after installation

**Temporary disable:**
```bash
# Create disable flag
touch /data/adb/cco/disable
reboot
```

**Or disable via Magisk Manager:**
- Open Magisk Manager
- Toggle off CCO module
- Reboot

**Complete removal:**
- Remove module in Magisk Manager
- Reboot
- Optional: Manually delete `/data/adb/cco`

## Logs

Module maintains comprehensive logs at `/data/adb/cco/logs/module.log`

**View logs:**
```bash
# Last 50 lines
tail -50 /data/adb/cco/logs/module.log

# Search for errors
grep ERROR /data/adb/cco/logs/module.log

# Monitor during boot (via adb)
adb shell tail -f /data/adb/cco/logs/module.log
```

**Log entries include:**
- Device information
- Path detection results
- Bind mount operations
- SELinux context operations
- CarrierConfig refresh attempts
- Success/failure indicators

## Safety & Reversibility

### Safe by Design

- **Non-destructive:** Uses bind mounts, never modifies system files directly
- **Automatic backup:** Original files backed up before first override
- **Easy disable:** Single flag file disables module without uninstalling
- **Clean uninstall:** Restores original files and unmounts overrides

### Backup Information

Original files are backed up to `/data/adb/cco/backup/` with metadata:
- `override_original.xml` - Original override file (if existed)
- `backup_info.txt` - Backup date, device info, original path

### Reverting Changes

**Method 1: Disable module**
```bash
touch /data/adb/cco/disable
reboot
```

**Method 2: Delete override**
```bash
rm /data/adb/cco/active/override.xml
reboot
```

**Method 3: Uninstall module**
- Remove via Magisk Manager
- Original files automatically restored

## Advanced Usage

### Testing Different Profiles

```bash
# Deploy generic profile
cp profiles/generic_wfc_enable.xml /data/adb/cco/active/override.xml

# Test (reboot required)
reboot

# If not working, try aggressive
cp profiles/aggressive_enable.xml /data/adb/cco/active/override.xml
reboot
```

### Monitoring Bind Mount

```bash
# Check if mounted
mountpoint /data/vendor/carrierconfig/override.xml

# View mount details
mount | grep carrierconfig

# Verify file content
cat /data/vendor/carrierconfig/override.xml
```

### Manual CarrierConfig Refresh

```bash
# After deploying new override (without reboot)
am broadcast -a android.telephony.action.CARRIER_CONFIG_CHANGED

# May not always work - reboot is most reliable
```

## Compatibility

### Tested Devices
- Samsung Galaxy S21/S22/S23/S24 series
- Samsung Galaxy Z Fold 3/4/5
- Samsung Galaxy Z Flip 3/4/5
- Samsung Galaxy A series (select models)

### Known Issues
- Some carrier-locked devices may have firmware-level blocks
- Verizon devices may require additional steps
- T-Mobile may need specific profile adjustments
- AT&T often works with generic profile

## Support

**Module Issues:**
- Check `/data/adb/cco/logs/module.log` first
- GitHub Issues: https://github.com/supermarsx/magisk-carrier-config-override/issues

**General Help:**
- XDA Thread: [TBD]
- GitHub Discussions: https://github.com/supermarsx/magisk-carrier-config-override/discussions

## Contributing

Contributions welcome! Especially:
- Device-specific profiles
- Carrier-specific configurations
- Bug reports and fixes
- Documentation improvements

## License

MIT License - See main repository for details

## Credits

- Magisk by topjohnwu
- Samsung IMS research community
- XDA developers

---

**Version:** 1.0.0
**Last Updated:** 2026-02-04
**Author:** supermarsx
