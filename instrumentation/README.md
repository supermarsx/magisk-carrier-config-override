# CCO Instrumentation — Runtime Hooks System

Runtime instrumentation for simulating carrier entitlement checks on Samsung devices. Two backends: **Frida** (dynamic, for testing) and **LSPosed** (persistent, for daily use).

## Architecture

```text
instrumentation/
├── frida/                          # Frida agent and hooks
│   ├── agent-complete.js          # Main agent with RPC, event batching, stats
│   ├── agent-enhanced.js          # Enhanced agent with module loader
│   ├── agent.js                   # Simple agent
│   └── hooks/                     # Modular hook implementations
│       ├── ims.js                 # IMS service hooks (15 hook points)
│       ├── carrierconfig.js       # CarrierConfig hooks
│       ├── settings.js            # Settings provider hooks
│       ├── telephony.js           # Telephony manager hooks
│       ├── diagnostics.js         # Diagnostic collection
│       ├── recording.js           # Record & replay engine
│       ├── ipc.js                 # IPC/event logging
│       └── utils.js               # Data sanitization, formatting
├── lsposed/                       # LSPosed Xposed module
│   ├── src/main/java/...         # Kotlin hook implementations
│   ├── build.gradle               # Build configuration
│   └── README.md                  # Module-specific docs
├── shared/
│   └── profiles.json              # Hook profiles database (8 profiles)
├── cco-instrument                 # Python CLI tool
└── frida-launcher                 # Bash launcher script
```

## Quick Start

### Frida Backend (Recommended for Testing)

**Requirements**: Rooted device, Frida server, `frida-tools` on PC.

```bash
# 1. Install frida-tools
pip install frida-tools

# 2. Push Frida server to device
adb push frida-server-arm64 /data/local/tmp/
adb shell chmod 755 /data/local/tmp/frida-server-arm64

# 3. Start Frida server
adb shell "su -c '/data/local/tmp/frida-server-arm64 &'"

# 4. Launch instrumentation (pick one)
./instrumentation/frida-launcher -i                                    # Interactive
./instrumentation/cco-instrument frida start --profile oneui6_generic  # CLI
frida -U com.sec.imsservice -l instrumentation/frida/agent-complete.js # Manual
```

### LSPosed Backend (Recommended for Daily Use)

**Requirements**: LSPosed framework installed.

```bash
# 1. Build and install CCO LSPosed module
cd instrumentation/lsposed
./gradlew assembleRelease
adb install -r build/outputs/apk/release/lsposed-release.apk

# 2. Configure in LSPosed Manager:
#    - Enable "CCO Entitlement Module"
#    - Select scope: com.sec.imsservice, com.android.phone, com.android.settings
#    - Reboot device
```

## Hook Profiles

### Available Profiles

| Profile ID | Description | Target Devices |
| --- | --- | --- |
| `oneui6_generic` | Generic Samsung One UI 6 | Android 13/14, One UI 6.x |
| `oneui5_generic` | Generic Samsung One UI 5 | Android 13, One UI 5.x |
| `oneui4_generic` | Generic Samsung One UI 4 | Android 12, One UI 4.x |
| `aggressive_bypass` | Maximum hook coverage | All devices (stubborn carriers) |
| `tmobile_us` | T-Mobile specific | T-Mobile US (310260, 310490) |
| `att_us` | AT&T specific | AT&T US (310410) |
| `verizon_us` | Verizon specific | Verizon US (311480) |
| `custom_record` | User-recorded profile | Populated from recordings |

### Profile Structure

```json
{
  "id": "oneui6_generic",
  "name": "Generic Samsung One UI 6",
  "description": "Generic hook profile for Samsung devices",
  "oneui_versions": ["6.0", "6.1"],
  "android_versions": ["13", "14"],
  "targets": [
    {
      "package": "com.sec.imsservice",
      "class": "com.sec.ims.ImsManager",
      "method": "isWfcEntitled",
      "signature": "()Z",
      "return_value": true,
      "description": "Primary WFC entitlement check"
    }
  ],
  "carrier_config_overrides": {
    "carrier_wfc_ims_available_bool": true,
    "editable_wfc_mode_bool": true
  }
}
```

### Creating Custom Profiles

**Via Recording** (recommended):

```bash
frida -U com.sec.imsservice -l agent-complete.js
# In Frida console:
> rpc.exports.startRecording()
# Trigger entitlement checks (open Settings → Wi-Fi Calling)
> const session = rpc.exports.stopRecording()
> const json = rpc.exports.exportRecording()
```

**Manual**: Edit `shared/profiles.json`, add targets with method signatures and return values.

## Hook Targets

### IMS Service Hooks (`com.sec.imsservice`)

| Class | Method | Description |
| --- | --- | --- |
| `ImsManager` | `isWfcEntitled()` | Primary WFC entitlement — forced `true` |
| `ImsManager` | `isVolteProvisioned()` | VoLTE provisioning check |
| `ImsFeature` | `isVowifiEnabled()` | VoWiFi feature availability |
| `IVolteServiceModuleInternal` | `isVowifiEnabled()` | VoWiFi service module check |
| `ImsRegistration` | `hasService(String)` | IMS service availability |
| `ImsSettings` | `getBoolean()` | IMS settings — forced on WFC keys |
| `VoWifiManager` | `getVoWiFiMode()` | VoWiFi mode query |

### CarrierConfig Hooks

| Method | Forced Keys |
| --- | --- |
| `CarrierConfigManager.getConfigForSubId(int)` | `carrier_wfc_ims_available_bool=true`, `editable_wfc_mode_bool=true`, `carrier_default_wfc_ims_enabled_bool=true`, `carrier_default_wfc_ims_roaming_enabled_bool=true`, `carrier_wfc_supports_wifi_only_bool=true` |

### Settings Hooks (`android.provider.Settings`)

| Class | Method | Keys Intercepted |
| --- | --- | --- |
| `Settings.Global` | `getInt()` | `wfc_ims_enabled`, `wfc_ims_mode` |
| `Settings.Global` | `getString()` | Various WFC settings |
| `Settings.System` | `getInt()` | Legacy WFC settings |

### Telephony Hooks

SIM state monitoring, network type detection, carrier info, subscription management.

### Settings UI Hooks (`com.android.settings`)

`WifiCallingSettings.isWifiCallingSupported(Context)` — controls settings page availability.

## Frida Features

### RPC Commands

```bash
./frida-launcher rpc getStats                                          # Hook statistics
./frida-launcher rpc updateConfig --params '{"features":{"autoBypass":true}}'
./frida-launcher rpc dumpCarrierConfig                                 # Dump config
./frida-launcher rpc toggleHooks --params "true"                       # Enable/disable
```

### Event Streaming

Events batched every 1 second:

```json
{
  "type": "entitlement_check",
  "method": "ImsManager.isWfcEntitled",
  "args": [0],
  "result": false,
  "forced": true,
  "timestamp": 1234567890
}
```

### Configuration

```javascript
{
  features: {
    autoBypass: true,       // Auto-bypass entitlement checks
    captureEvents: true,    // Capture hook events
    diagnostics: true       // Collect diagnostics
  },
  logging: {
    level: "info",          // debug, info, warn, error
    console: true,
    file: false
  },
  modules: {
    ims: true,
    carrierconfig: true,
    telephony: true,
    settings: true,
    diagnostics: true
  }
}
```

Update at runtime via RPC: `rpc.exports.updateConfig({...})`

### Diagnostics Collection

```bash
./frida-launcher rpc getReport       # Full diagnostic snapshot
./frida-launcher rpc exportDiagnostics  # Save to /sdcard/cco-diagnostics.json
```

## CLI Tools

### cco-instrument (Python)

```bash
./cco-instrument profiles                                # List profiles
./cco-instrument frida start --profile oneui6_generic    # Start Frida session
./cco-instrument lsposed install                         # Install LSPosed module
./cco-instrument device info                             # Show device info
./cco-instrument ps                                      # List processes
```

### frida-launcher (Bash)

```bash
./frida-launcher -i                    # Interactive mode
./frida-launcher com.sec.imsservice    # Attach to IMS service
./frida-launcher -s com.android.phone  # Spawn Phone process
./frida-launcher -l                    # List running processes
```

## Safety

- **Frida**: Runtime-only — hooks cleared on session stop, no system modification
- **LSPosed**: Module can be disabled/removed, reboot to revert
- **Data sanitization**: IMSI, IMEI, phone numbers masked in hook output

## Troubleshooting

### Frida server not found

```bash
./frida-launcher install-server
# Or manually download from https://github.com/frida/frida/releases
```

### Frida not connecting

```bash
adb shell "ps | grep frida"                        # Check if running
adb shell "su -c 'killall frida-server'"           # Restart
adb shell "su -c '/data/local/tmp/frida-server-arm64 &'"
frida-ps -U                                        # Test connection
```

### App crashes on attach

Try spawning instead of attaching:

```bash
./frida-launcher launch --spawn
```

### Hooks not working

1. Check profile compatibility: `./cco-instrument device info`
2. Enable debug logging in config
3. Try `aggressive_bypass` profile
4. Use recording mode to capture actual method calls

### LSPosed module not loading

1. Verify enabled in LSPosed Manager
2. Check scope includes target packages
3. Reboot device
4. Check LSPosed logs

## Development

### Adding New Frida Hooks

1. Create hook file in `frida/hooks/`:

   ```javascript
   function install(config, logEvent, log) {
       const MyClass = Java.use("com.example.MyClass");
       MyClass.myMethod.implementation = function() {
           const result = this.myMethod();
           logEvent("custom", "MyClass.myMethod", [], result);
           return config.forceValue ? true : result;
       };
   }
   module.exports = { install };
   ```

2. Import in `agent-enhanced.js` or `agent-complete.js`
3. Update profile in `shared/profiles.json`

### Adding New LSPosed Hooks

1. Create hook class in `lsposed/src/main/java/.../hooks/`
2. Use `XposedBridge.hookAllMethods()` pattern
3. Call from `CCOXposedModule.kt`

### Performance

- **Frida**: ~50MB memory per agent, minimal CPU when idle
- **LSPosed**: ~10MB per hooked process, near-zero overhead

## References

- [Frida Documentation](https://frida.re/docs/)
- [LSPosed GitHub](https://github.com/LSPosed/LSPosed)
- [Android IMS Architecture](https://source.android.com/docs/core/connect/ims)
- [LSPosed Module README](lsposed/README.md)

## License

MIT License — see repository root.
