# CCO Entitlement Instrumentation Bundle

Runtime instrumentation for simulating entitlement responses on Samsung devices.

## Overview

This bundle provides two backend implementations for runtime entitlement simulation:
- **Frida backend**: Dynamic instrumentation (requires Frida server)
- **LSPosed backend**: Xposed module (persistent across reboots)

## Architecture

```
cco-entitlement/
├── frida/                  # Frida scripts
│   ├── agent.js           # Main Frida agent
│   ├── hooks/             # Hook implementations
│   └── profiles/          # Device-specific profiles
├── lsposed/               # LSPosed module
│   ├── src/               # Java/Kotlin source
│   └── build.gradle       # Build config
└── shared/                # Shared hook logic
    └── profiles.json      # Hook profile database
```

## Frida Backend

### Requirements
- Rooted Android device
- Frida server running on device
- CCO app with Frida support

### Usage

1. **Start Frida server** on device:
   ```bash
   adb push frida-server-arm64 /data/local/tmp/
   adb shell "chmod 755 /data/local/tmp/frida-server-arm64"
   adb shell "/data/local/tmp/frida-server-arm64 &"
   ```

2. **Deploy hooks via CCO app**:
   - Open app → Entitlement tab
   - Select profile (e.g., "Generic Samsung IMS")
   - Tap "Start Session"

3. **Monitor live events**:
   - View entitlement requests and responses in real-time
   - Export trace for record/replay

### Hook Targets

The Frida agent hooks into:
- `com.sec.imsservice` - Samsung IMS service
- `com.samsung.android.ims` - IMS framework
- Carrier-specific entitlement packages

### Example Hook

```javascript
// Hook isWfcEntitled check
Java.perform(() => {
    const ImsManager = Java.use("com.sec.ims.ImsManager");
    ImsManager.isWfcEntitled.implementation = function() {
        console.log("[CCO] isWfcEntitled called, forcing TRUE");
        return true;
    };
});
```

## LSPosed Backend

### Requirements
- LSPosed framework installed
- CCO LSPosed module

### Installation

1. Install LSPosed framework
2. Install CCO LSPosed module APK
3. Enable module in LSPosed Manager
4. Select target apps (IMS services, Settings)
5. Reboot

### Scope Configuration

Target packages:
- `com.sec.imsservice`
- `com.android.settings` (for Settings UI)
- Detected carrier entitlement apps

## Profiles

Hook profiles are device and firmware-specific. Each profile contains:
- Target package names
- Hook method signatures
- Expected return values
- One UI version compatibility

### Profile Format

```json
{
  "id": "oneui6_generic",
  "name": "Generic Samsung One UI 6",
  "oneui_versions": ["6.0", "6.1"],
  "targets": [
    {
      "package": "com.sec.imsservice",
      "class": "com.sec.internal.ims.entitlement.EntitlementCheck",
      "method": "isWfcEntitled",
      "signature": "()Z",
      "return_value": true
    }
  ]
}
```

### Creating Custom Profiles

Use **Record mode** in CCO app:
1. Start recording session
2. Navigate to Wi-Fi Calling settings
3. Observe blocked entitlement checks
4. Save trace as custom profile
5. Replay profile on subsequent runs

## Safety

- **Runtime only**: Frida hooks are temporary (cleared on session stop)
- **Reversible**: LSPosed module can be disabled
- **No system modification**: All changes are in-memory

## Troubleshooting

### Frida not connecting
```bash
# Check Frida server is running
adb shell "ps | grep frida"

# Test connection
frida-ps -U
```

### Hooks not applied
```bash
# Check target package is running
adb shell "ps | grep sec.imsservice"

# Check Frida agent logs
# (view in CCO app Event Stream)
```

### LSPosed module not loading
1. Verify module is enabled in LSPosed Manager
2. Check scope includes target packages
3. Reboot device
4. Check LSPosed logs

## Development

### Adding new hooks

1. Identify target method via reverse engineering
2. Add hook to `frida/hooks/` directory
3. Update profile in `shared/profiles.json`
4. Test on target device/firmware

### Testing

```bash
# Test Frida script directly
frida -U -f com.sec.imsservice -l frida/agent.js
```

## License

MIT License - See repository root
