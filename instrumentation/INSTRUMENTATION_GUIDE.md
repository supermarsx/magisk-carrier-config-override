# CCO Instrumentation - Complete Guide

## Overview

The CCO Instrumentation Bundle provides two methods for runtime entitlement simulation:

1. **Frida Backend**: Dynamic instrumentation with recording/replay capabilities
2. **LSPosed Backend**: Persistent Xposed module for automatic hooks

## Architecture

```
instrumentation/
├── frida/                          # Frida agent and hooks
│   ├── agent-complete.js          # Main agent with RPC
│   ├── agent.js                   # Simple agent
│   └── hooks/                     # Modular hook implementations
│       ├── ims.js                 # IMS service hooks
│       ├── carrierconfig.js       # CarrierConfig hooks
│       ├── settings.js            # Settings provider hooks
│       ├── telephony.js           # Telephony hooks
│       ├── recording.js           # Record & replay engine
│       └── ipc.js                 # IPC/event logging
├── lsposed/                       # LSPosed Xposed module
│   ├── src/main/java/...          # Kotlin hook implementations
│   ├── build.gradle               # Build configuration
│   └── README.md                  # Module-specific docs
├── shared/                        # Shared resources
│   └── profiles.json              # Hook profiles database
├── cco-instrument                 # Python CLI tool
└── frida-launcher                 # Bash launcher script
```

## Quick Start

### Method 1: Frida (Recommended for Testing)

**Requirements:**
- Rooted Android device
- Frida server on device
- Frida Python tools on PC

**Setup:**

1. **Install Frida Python tools**:
   ```bash
   pip install frida-tools
   ```

2. **Push Frida server to device**:
   ```bash
   # Download frida-server-arm64 for your architecture
   adb push frida-server-arm64 /data/local/tmp/
   adb shell chmod 755 /data/local/tmp/frida-server-arm64
   ```

3. **Start Frida server**:
   ```bash
   adb shell "su -c '/data/local/tmp/frida-server-arm64 &'"
   ```

4. **Launch instrumentation**:
   ```bash
   # Using launcher script
   ./instrumentation/frida-launcher -i
   
   # Or using CLI tool
   ./instrumentation/cco-instrument frida start --profile oneui6_generic
   
   # Or manually
   frida -U com.sec.imsservice -l instrumentation/frida/agent-complete.js
   ```

### Method 2: LSPosed (Recommended for Daily Use)

**Requirements:**
- LSPosed framework installed
- CCO LSPosed module APK

**Setup:**

1. **Install LSPosed framework** (follow LSPosed docs)

2. **Build and install CCO module**:
   ```bash
   cd instrumentation/lsposed
   ./gradlew assembleRelease
   adb install -r build/outputs/apk/release/lsposed-release.apk
   ```

3. **Configure in LSPosed Manager**:
   - Open LSPosed Manager
   - Enable "CCO Entitlement Module"
   - Select scope:
     - `com.sec.imsservice` (Samsung IMS Service)
     - `com.android.phone` (Phone)
     - `com.android.settings` (Settings)
   - Reboot device

## Features

### Frida Features

#### 1. Real-time Hook Injection
- Inject hooks without rebooting
- Enable/disable hooks dynamically
- Test different profiles instantly

#### 2. Recording & Replay
Record a session:
```javascript
// Via Frida RPC
rpc.exports.startRecording()
// ... trigger entitlement checks ...
const session = rpc.exports.stopRecording()
const json = rpc.exports.exportRecording()
```

Replay a session:
```javascript
rpc.exports.loadRecording(sessionJson)
rpc.exports.startReplay()
// Now all hooked methods return recorded values
```

#### 3. Event Logging
All hook interceptions are logged with:
- Timestamp
- Method called
- Original return value
- Forced return value
- Thread ID

#### 4. IPC Communication
Real-time event streaming to CCO app or external tools.

#### 5. Profile Management
Switch profiles dynamically:
```javascript
rpc.exports.updateConfig({
    profile: "aggressive_bypass",
    forceEntitled: true,
    features: {
        autoBypass: true,
        eventLogging: true
    }
})
```

### LSPosed Features

#### 1. Persistent Hooks
- Hooks active after reboot
- No external tools required
- Minimal performance impact

#### 2. Automatic Target Detection
- Hooks IMS services automatically
- Detects carrier-specific apps
- Hooks Settings UI for WFC controls

#### 3. CarrierConfig Override
- Intercepts `CarrierConfigManager.getConfigForSubId()`
- Forces WFC-related booleans to `true`
- Modifies config bundles in-place

#### 4. Settings Provider Hooks
- Intercepts `Settings.Global` queries
- Forces WFC settings enabled
- Ensures UI shows correct state

## Hook Profiles

### Available Profiles

| Profile ID | Description | Target Devices |
|------------|-------------|----------------|
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

1. **Via Recording**:
   ```bash
   # Start recording session
   frida -U com.sec.imsservice -l agent-complete.js
   
   # In Frida console:
   > rpc.exports.startRecording()
   
   # Trigger entitlement checks (open Settings → Wi-Fi Calling)
   # Stop recording
   > const session = rpc.exports.stopRecording()
   > const json = rpc.exports.exportRecording()
   
   # Save to file
   > send({type: 'save', data: json})
   ```

2. **Manual Creation**:
   - Edit `shared/profiles.json`
   - Add new profile with target hooks
   - Specify method signatures and return values

## CLI Tools

### cco-instrument (Python)

Comprehensive management tool:

```bash
# List profiles
./cco-instrument profiles

# Start Frida session
./cco-instrument frida start --profile oneui6_generic

# Install LSPosed module
./cco-instrument lsposed install

# Show device info
./cco-instrument device info

# List processes
./cco-instrument ps
```

### frida-launcher (Bash)

Quick Frida launcher with interactive mode:

```bash
# Interactive mode
./frida-launcher -i

# Attach to IMS service
./frida-launcher com.sec.imsservice

# Spawn Phone process
./frida-launcher -s com.android.phone

# List running processes
./frida-launcher -l
```

## Hook Targets

### IMS Service Hooks

**Package**: `com.sec.imsservice`

| Class | Method | Description |
|-------|--------|-------------|
| `ImsManager` | `isWfcEntitled()` | Primary WFC entitlement check |
| `ImsManager` | `isVolteProvisioned()` | VoLTE provisioning check |
| `ImsFeature` | `isVowifiEnabled()` | VoWiFi feature availability |
| `IVolteServiceModuleInternal` | `isVowifiEnabled()` | VoWiFi service module check |
| `ImsRegistration` | `hasService(String)` | IMS service availability |

### CarrierConfig Hooks

**Package**: `android.telephony.CarrierConfigManager`

| Method | Description |
|--------|-------------|
| `getConfigForSubId(int)` | Get config for subscription, modify in-place |
| `getConfig()` | Get default config, modify in-place |

**Modified Keys**:
- `carrier_wfc_ims_available_bool` → `true`
- `carrier_default_wfc_ims_enabled_bool` → `true`
- `editable_wfc_mode_bool` → `true`
- `editable_wfc_roaming_mode_bool` → `true`
- `carrier_wfc_supports_wifi_only_bool` → `true`

### Settings Hooks

**Package**: `android.provider.Settings`

| Class | Method | Keys Intercepted |
|-------|--------|------------------|
| `Settings.Global` | `getInt()` | `wfc_ims_enabled`, `wfc_ims_mode` |
| `Settings.Global` | `getString()` | Various WFC settings |
| `Settings.System` | `getInt()` | Legacy WFC settings |

### Phone App Hooks

**Package**: `com.android.phone`

| Class | Method | Description |
|-------|--------|-------------|
| `PhoneUtils` | `isWfcModeEditable()` | WFC mode editability |

### Settings UI Hooks

**Package**: `com.android.settings`

| Class | Method | Description |
|-------|--------|-------------|
| `WifiCallingSettings` | `isWifiCallingSupported(Context)` | Settings page availability |

## Troubleshooting

### Frida Issues

**"Failed to spawn: unable to find process"**
- Ensure package name is correct
- Try attaching instead of spawning: remove `-f` flag

**"Unable to connect to remote frida-server"**
- Check Frida server is running: `adb shell ps | grep frida`
- Restart server: `adb shell "su -c 'killall frida-server && /data/local/tmp/frida-server-arm64 &'"`

**"Error: Script is destroyed"**
- Normal when app restarts
- Re-attach with Frida

### LSPosed Issues

**"Module not taking effect"**
1. Verify module is enabled in LSPosed Manager
2. Check scope includes target packages
3. Reboot device
4. Check Xposed logs in LSPosed Manager

**"App crashes on start"**
- Check LSPosed logs for errors
- Try disabling specific hooks
- Verify module compatibility with device

### Hook Not Working

1. **Check profile compatibility**:
   ```bash
   ./cco-instrument device info
   # Compare with profile requirements
   ```

2. **Enable debug logging**:
   - Frida: Event logging enabled by default
   - LSPosed: Check module logs in LSPosed Manager

3. **Try aggressive profile**:
   ```bash
   ./cco-instrument frida start --profile aggressive_bypass
   ```

4. **Record and analyze**:
   - Use recording mode to capture actual method calls
   - Create custom profile based on recordings

## Advanced Usage

### Custom Hook Development

**Adding new Frida hook**:

1. Create hook file in `frida/hooks/`:
   ```javascript
   // my-custom-hooks.js
   function install(config, logEvent, log) {
       try {
           const MyClass = Java.use("com.example.MyClass");
           MyClass.myMethod.implementation = function() {
               const result = this.myMethod();
               logEvent("custom", "MyClass.myMethod", [], result);
               return config.forceValue ? true : result;
           };
       } catch (e) {
           log("error", "Failed to hook MyClass", {error: e.message});
       }
   }
   module.exports = { install };
   ```

2. Import in `agent-complete.js`:
   ```javascript
   const myHooks = require('./hooks/my-custom-hooks.js');
   // ...
   myHooks.install(config, logEvent, log);
   ```

**Adding new LSPosed hook**:

1. Create hook class in `lsposed/src/main/java/.../hooks/`:
   ```kotlin
   object MyHooks {
       fun install(lpparam: XC_LoadPackage.LoadPackageParam, config: HookConfig) {
           try {
               val myClass = XposedHelpers.findClass(
                   "com.example.MyClass",
                   lpparam.classLoader
               )
               
               XposedBridge.hookAllMethods(myClass, "myMethod", object : XC_MethodHook() {
                   override fun afterHookedMethod(param: MethodHookParam) {
                       if (config.forceValue) {
                           param.result = true
                       }
                   }
               })
           } catch (e: Throwable) {
               XposedBridge.log("Failed to hook MyClass: ${e.message}")
           }
       }
   }
   ```

2. Call from `CCOXposedModule.kt`:
   ```kotlin
   MyHooks.install(lpparam, config)
   ```

### Integration with CCO App

The CCO app includes Frida integration:

```kotlin
// In your code
val fridaManager = FridaManager(context)
val profileManager = ProfileManager(context, gson)

// Check status
val status = fridaManager.getStatus()
println("Frida running: ${status.isRunning}")

// Find profile for device
val profile = profileManager.findProfileForDevice(
    oneuiVersion = "6.1",
    androidVersion = "14",
    carrier = "310260"
)

// Start session
fridaManager.startSession(
    target = "com.sec.imsservice",
    profile = profile?.id ?: "oneui6_generic"
).collect { message ->
    println(message)
}
```

## Performance Considerations

### Frida
- **Memory**: ~50MB per agent
- **CPU**: Minimal when hooks not triggered
- **Battery**: Negligible with event logging disabled

### LSPosed
- **Memory**: ~10MB per hooked process
- **CPU**: Near-zero overhead
- **Battery**: No measurable impact

## Security Notes

⚠️ **Important**:
- Both methods require root access
- Frida server runs with root privileges
- LSPosed has system-level access
- Only use on devices you own
- Hooks can modify system behavior

## Contributing

To add device-specific profiles:

1. Record a session on target device
2. Analyze hooks that fired
3. Create profile in `shared/profiles.json`
4. Test with both Frida and LSPosed
5. Submit pull request

## References

- [Frida Documentation](https://frida.re/docs/)
- [LSPosed GitHub](https://github.com/LSPosed/LSPosed)
- [Android IMS Architecture](https://source.android.com/docs/core/connect/ims)
- [Samsung IMS Service Analysis](https://github.com/samsung-ims-research)

## License

See [license.md](../license.md) for details.
