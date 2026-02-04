# ccoctl - CarrierConfig Override CLI

Command-line utility for interacting with CCO (CarrierConfig Override) on Android devices.

## Overview

`ccoctl` provides a comprehensive CLI interface for:
- Device diagnostics and status queries
- Configuration deployment
- Log collection and analysis
- Frida instrumentation management
- Device testing
- Report generation

## Installation

```bash
# Add to PATH
export PATH="$PATH:/path/to/magisk-carrier-config-override/cli"

# Or create symlink
ln -s /path/to/magisk-carrier-config-override/cli/ccoctl /usr/local/bin/ccoctl
```

## Prerequisites

- **Python 3.7+**
- **ADB** (Android Debug Bridge)
- **Rooted Android device** with Magisk
- **CCO app** installed (optional for some commands)
- **frida-tools** for instrumentation: `pip install frida-tools`

## Commands

### devices - List Connected Devices

List all connected Android devices.

```bash
ccoctl devices
```

Output:
```
Connected devices:
  - RF8R41XXXXXX
  - emulator-5554
```

Use with multiple devices:
```bash
ccoctl -d RF8R41XXXXXX <command>
```

### status - Comprehensive Status

Show complete CCO status including device, app, module, and IMS.

```bash
ccoctl status
```

Output:
```
=== CCO Status ===

Device:
  Model: Samsung Galaxy S21
  Android: 13 (SDK 33)
  Rooted: Yes

CCO App:
  Installed: Yes

Magisk Module:
  Installed: Yes
  Active Profile: expose_wfc_ui

IMS Status:
  Registered: Yes
  VoLTE: Available
  VoWiFi: Available
```

### info - Device Information

Show detailed device information.

```bash
# Basic info
ccoctl info

# Include IMS status
ccoctl info --ims
```

Output includes:
- Manufacturer, model, device
- Android version and SDK
- Build fingerprint
- Security patch level
- Root status
- IMS registration (with `--ims`)

### deploy - Deploy Configuration

Deploy a CarrierConfig preset to the device.

```bash
ccoctl deploy <preset_name>
```

Examples:
```bash
# Deploy expose_wfc_ui preset
ccoctl deploy expose_wfc_ui

# Deploy generic preset
ccoctl deploy generic

# Deploy custom preset
ccoctl deploy custom_att
```

**Note:** Reboot required after deployment for changes to take effect.

### export - Export Report

Export diagnostic report from device.

```bash
# Export to default location
ccoctl export

# Custom output path
ccoctl export -o /path/to/report.json
```

Report includes:
- Device information
- IMS status
- CarrierConfig state
- Settings values
- Test results

### dumpsys - System Dumps

Show Android system service dumps.

```bash
# IMS service dump
ccoctl dumpsys ims

# CarrierConfig dump
ccoctl dumpsys carrier_config
```

### logs - Log Collection

Fetch and display logs from device.

```bash
# Default: CCO and Frida logs
ccoctl logs

# Specific log types
ccoctl logs --cco        # CCO app only
ccoctl logs --frida      # Frida only
ccoctl logs --ims        # IMS logs

# Last N lines
ccoctl logs -t 100

# Clear logs
ccoctl logs --clear
```

Stream logs in real-time (Ctrl+C to stop):
```bash
ccoctl logs
```

### frida - Frida Instrumentation

Launch Frida instrumentation session.

```bash
# Launch with default script
ccoctl frida

# Custom script
ccoctl frida -s /path/to/script.js

# Pause on start
ccoctl frida --pause

# Debug mode
ccoctl frida --debug
```

See [Instrumentation Documentation](../instrumentation/INSTRUMENTATION.md) for details.

### test - Device Testing

Run automated tests on device.

```bash
# Run all tests
ccoctl test

# Specific category
ccoctl test -c root        # Root access test
ccoctl test -c module      # Module installation test
ccoctl test -c config      # CarrierConfig test
ccoctl test -c ims         # IMS availability test
```

Output:
```
=== Running Device Tests ===

[✓] Root Access
[✓] Module Installed
[✓] Carrier Config
[✓] IMS Available

Results: 4/4 passed
```

### diagnose - Comprehensive Diagnostics

Run full diagnostic suite and generate report.

```bash
# Run diagnostics
ccoctl diagnose

# Save to file
ccoctl diagnose -o report.json

# Include logs
ccoctl diagnose --logs -o full_report.json
```

Report includes:
- Device information
- IMS status
- Test results
- Logs (if `--logs`)
- Timestamp

## Usage Examples

### Daily Workflow

```bash
# Check status
ccoctl status

# Deploy config if needed
ccoctl deploy expose_wfc_ui

# Reboot device
adb reboot

# Wait for boot
adb wait-for-device

# Verify deployment
ccoctl status
```

### Debugging Session

```bash
# Start log streaming
ccoctl logs &

# Launch instrumentation
ccoctl frida

# In another terminal, monitor status
watch -n 5 ccoctl status
```

### Troubleshooting

```bash
# Full diagnostic
ccoctl diagnose -o diagnostic_$(date +%Y%m%d_%H%M%S).json --logs

# Check specific components
ccoctl test -c root
ccoctl test -c module
ccoctl test -c ims

# Dump system info
ccoctl dumpsys ims > ims_dump.txt
ccoctl dumpsys carrier_config > config_dump.txt
```

### Multiple Devices

```bash
# List devices
ccoctl devices

# Target specific device
ccoctl -d RF8R41XXXXXX status
ccoctl -d RF8R41XXXXXX deploy expose_wfc_ui

# Batch operation
for device in $(ccoctl devices | grep -v "Connected" | awk '{print $2}'); do
  echo "Deploying to $device"
  ccoctl -d $device deploy expose_wfc_ui
done
```

## Integration

### CI/CD Pipeline

```yaml
# .github/workflows/device-test.yml
- name: Deploy and Test
  run: |
    ccoctl deploy expose_wfc_ui
    adb reboot
    adb wait-for-device
    ccoctl diagnose -o $GITHUB_WORKSPACE/report.json
    ccoctl test
```

### Scripting

```bash
#!/bin/bash
# deploy.sh - Automated deployment

set -e

echo "Checking device..."
if ! ccoctl status | grep -q "Rooted: Yes"; then
  echo "Error: Device not rooted"
  exit 1
fi

echo "Deploying config..."
ccoctl deploy expose_wfc_ui

echo "Rebooting..."
adb reboot
adb wait-for-device
sleep 10

echo "Verifying..."
ccoctl test || {
  echo "Tests failed!"
  ccoctl diagnose -o failed_deploy_$(date +%Y%m%d_%H%M%S).json --logs
  exit 1
}

echo "✓ Deployment successful!"
```

## Resources

- [CCO Documentation](../README.md)
- [Instrumentation Guide](../instrumentation/INSTRUMENTATION.md)
- [Module Documentation](../module/docs/README.md)
- [ADB Documentation](https://developer.android.com/studio/command-line/adb)

## License

Same as CCO project.
