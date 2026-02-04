# ccoctl - CLI Utility

Command-line interface for the CarrierConfig Override Manager.

## Overview

`ccoctl` is a Python-based CLI tool that provides ADB-friendly access to CCO functionality, suitable for automation and CI/CD integration.

## Installation

### Prerequisites
- Python 3.7+
- ADB (Android Debug Bridge)
- Connected Android device with CCO app installed

### Install

```bash
# Make executable
chmod +x ccoctl

# Optional: Add to PATH
sudo cp ccoctl /usr/local/bin/
```

## Usage

### List connected devices
```bash
ccoctl devices
```

### Show device information
```bash
ccoctl info

# Include IMS status
ccoctl info --ims
```

### Deploy CarrierConfig preset
```bash
ccoctl deploy expose_wfc_ui
```

Available presets:
- `expose_wfc_ui` - Make WFC settings visible
- `wfc_default_enabled` - Enable WFC by default
- `editable_wfc_mode` - Allow mode changes
- `wifi_preferred` - Set Wi-Fi preferred mode
- `wifi_only` - Set Wi-Fi only mode

### Export diagnostic report
```bash
ccoctl export -o report.json
```

### View dumpsys output
```bash
# IMS status
ccoctl dumpsys ims

# CarrierConfig
ccoctl dumpsys carrier_config
```

### Multiple devices

When multiple devices are connected, specify target:
```bash
ccoctl -d DEVICE_SERIAL info
```

## Examples

### Full diagnostic workflow
```bash
# Check device
ccoctl info --ims

# Deploy configuration
ccoctl deploy expose_wfc_ui

# Reboot device (manual)
adb reboot

# Verify after reboot
ccoctl info --ims

# Export report
ccoctl export -o diagnostic_report.json
```

### CI/CD Integration
```bash
#!/bin/bash
# Automated testing script

# Wait for device
adb wait-for-device

# Get device info
ccoctl info > device_info.txt

# Deploy preset
ccoctl deploy expose_wfc_ui || exit 1

# Reboot and wait
adb reboot
adb wait-for-device

# Export results
sleep 30  # Wait for system to stabilize
ccoctl export -o results.json

# Parse results (using jq)
cat results.json | jq '.ims_status.vowifi_available'
```

## Output Format

All structured output is in JSON format for easy parsing:

```json
{
  "device": {
    "manufacturer": "samsung",
    "model": "SM-G991B",
    "android_version": "14"
  },
  "ims_status": {
    "registered": true,
    "vowifi_available": true
  }
}
```

## Troubleshooting

### "No devices connected"
```bash
# Check ADB connection
adb devices

# Enable USB debugging on device
# Settings → Developer Options → USB Debugging
```

### "CCO app not installed"
```bash
# Install CCO app
adb install cco-app.apk
```

### Permission denied
```bash
# Grant ADB permissions on device
# Check device screen for authorization prompt
```

## Requirements

- Python 3.7+
- `adb` in PATH
- Android device with:
  - USB debugging enabled
  - CCO app installed
  - Root access (for full functionality)

## License

MIT License - See repository root
