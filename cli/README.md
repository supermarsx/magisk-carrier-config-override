# ccoctl — CLI Utility

Command-line interface for the CarrierConfig Override Manager. Provides ADB-friendly access to CCO functionality for diagnostics, deployment, instrumentation, and automation.

## Installation

### Prerequisites

- Python 3.7+
- ADB (Android Debug Bridge)
- Connected Android device with CCO app installed
- `frida-tools` for instrumentation commands: `pip install frida-tools`

### Setup

```bash
chmod +x ccoctl

# Optional: Add to PATH
sudo cp ccoctl /usr/local/bin/
# Or: export PATH="$PATH:/path/to/magisk-carrier-config-override/cli"
```

## Commands

### devices — List Connected Devices

```bash
ccoctl devices
```

Use `-d SERIAL` with any command to target a specific device when multiple are connected.

### status — Comprehensive Status

```bash
ccoctl status
```

Output includes device info, CCO app installed status, Magisk module status, active profile, and IMS registration.

### info — Device Information

```bash
ccoctl info          # Basic info
ccoctl info --ims    # Include IMS status
```

Output: manufacturer, model, Android version, build fingerprint, security patch, root status, IMS registration (with `--ims`).

### deploy — Deploy Configuration

```bash
ccoctl deploy <preset_name>
```

Available presets: `expose_wfc_ui`, `wfc_default_enabled`, `editable_wfc_mode`, `wifi_preferred`, `wifi_only`, `generic`

**Note:** Reboot required after deployment.

### export — Export Report

```bash
ccoctl export                     # Default location
ccoctl export -o report.json      # Custom output path
```

Report includes device info, IMS status, CarrierConfig state, settings values, test results.

### dumpsys — System Dumps

```bash
ccoctl dumpsys ims              # IMS service dump
ccoctl dumpsys carrier_config   # CarrierConfig dump
```

### logs — Log Collection

```bash
ccoctl logs              # CCO and Frida logs (streaming)
ccoctl logs --cco        # CCO app only
ccoctl logs --frida      # Frida only
ccoctl logs --ims        # IMS logs
ccoctl logs -t 100       # Last 100 lines
ccoctl logs --clear      # Clear logs
```

### frida — Frida Instrumentation

```bash
ccoctl frida                         # Launch with default script
ccoctl frida -s /path/to/script.js   # Custom script
ccoctl frida --pause                 # Pause on start
ccoctl frida --debug                 # Debug mode
```

See the [Instrumentation Guide](../instrumentation/README.md) for details.

### test — Device Testing

```bash
ccoctl test              # Run all tests
ccoctl test -c root      # Root access test
ccoctl test -c module    # Module installation test
ccoctl test -c config    # CarrierConfig test
ccoctl test -c ims       # IMS availability test
```

### diagnose — Comprehensive Diagnostics

```bash
ccoctl diagnose                            # Run diagnostics
ccoctl diagnose -o report.json             # Save to file
ccoctl diagnose --logs -o full_report.json # Include logs
```

## Usage Examples

### Daily Workflow

```bash
ccoctl status
ccoctl deploy expose_wfc_ui
adb reboot
adb wait-for-device
ccoctl status
```

### CI/CD Integration

```yaml
# .github/workflows/device-test.yml
- name: Deploy and Test
  run: |
    ccoctl deploy expose_wfc_ui
    adb reboot && adb wait-for-device
    ccoctl diagnose -o $GITHUB_WORKSPACE/report.json
    ccoctl test
```

### Scripting

```bash
#!/bin/bash
set -e

ccoctl status | grep -q "Rooted: Yes" || { echo "Not rooted"; exit 1; }
ccoctl deploy expose_wfc_ui
adb reboot && adb wait-for-device && sleep 30
ccoctl test || {
  ccoctl diagnose -o failed_$(date +%Y%m%d_%H%M%S).json --logs
  exit 1
}
echo "Deployment successful"
```

### Batch Operations (Multiple Devices)

```bash
for device in $(ccoctl devices | grep -v "Connected" | awk '{print $2}'); do
  echo "Deploying to $device"
  ccoctl -d $device deploy expose_wfc_ui
done
```

## Output Format

All structured output is JSON:

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
adb devices
# Enable USB debugging: Settings → Developer Options → USB Debugging
```

### "CCO app not installed"

```bash
adb install cco-app.apk
```

### Permission denied

Check device screen for ADB authorization prompt.

## License

MIT License — see repository root.
