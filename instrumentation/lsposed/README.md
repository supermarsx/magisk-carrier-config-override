# CCO LSPosed Module

Xposed module for persistent Samsung VoWiFi/VoLTE enablement across reboots.

## Overview

This LSPosed module hooks into the Android telephony stack to bypass carrier restrictions on VoWiFi and VoLTE. Unlike Frida (which requires manual session starts), LSPosed hooks persist automatically after device boot.

## Features

- **Automatic activation** - Hooks active on boot
- **IMS entitlement bypass** - Force isWfcEntitled to true
- **CarrierConfig modification** - Override carrier restrictions
- **Settings interception** - Force WFC/VoLTE settings
- **Telephony monitoring** - Track SIM and network state
- **Runtime configuration** - JSON config file support

## Requirements

- **Android 10+** (SDK 29+)
- **LSPosed framework** installed and activated
- **Root access** (Magisk recommended)
- **Samsung device** with One UI 5+

## Installation

### 1. Install LSPosed Framework

If not already installed:

1. Install Magisk
2. Install LSPosed via Magisk modules
3. Reboot device

### 2. Install CCO Xposed Module

```bash
# Build APK
./gradlew assembleRelease

# Install
adb install build/outputs/apk/release/cco-xposed-release.apk
```

### 3. Activate Module in LSPosed

1. Open LSPosed Manager
2. Go to Modules tab
3. Enable "CCO Xposed Module"
4. Select scope:
   - ✓ System Framework (android)
   - ✓ Phone (com.android.phone)
   - ✓ Samsung IMS Service (com.sec.imsservice)
   - ✓ Samsung IMS (com.samsung.android.ims)
5. Reboot device

## Configuration

Create config file at `/data/adb/cco/xposed_config.json`:

```json
{
  "features": {
    "autoBypass": true,
    "captureEvents": false,
    "diagnostics": false
  },
  "logging": {
    "level": "info",
    "console": true,
    "file": false
  },
  "modules": {
    "ims": true,
    "carrierconfig": true,
    "telephony": true,
    "settings": true
  }
}
```

### Configuration Options

**features:**
- `autoBypass` - Automatically bypass entitlement checks (default: true)
- `captureEvents` - Log hook events (default: false)
- `diagnostics` - Collect diagnostic data (default: false)

**logging:**
- `level` - Log level: "debug", "info", "warn", "error" (default: "info")
- `console` - Log to Logcat (default: true)
- `file` - Log to file (default: false)

**modules:**
- `ims` - Enable IMS hooks (default: true)
- `carrierconfig` - Enable CarrierConfig hooks (default: true)
- `telephony` - Enable Telephony hooks (default: true)
- `settings` - Enable Settings hooks (default: true)

## Hook Coverage

### IMS Hooks

| Hook | Method | Action |
|------|--------|--------|
| ImsManager | isWfcEnabledByUser | Force true |
| ImsManager | isVtEnabledByUser | Force true |
| ImsManager | isEnhanced4gLteModeSettingEnabledByUser | Force true |
| ImsFeature | isVowifiEnabled | Force true |
| ImsFeature | isVolteEnabled | Force true |
| VoWiFiManager | isVoWiFiEnabled | Force true |
| ImsSettings | getBoolean (WFC keys) | Force true |
| EntitlementManager | hasEntitlement | Force true |

### CarrierConfig Hooks

| Hook | Key | Forced Value |
|------|-----|--------------|
| CarrierConfigManager | carrier_wfc_ims_available_bool | true |
| CarrierConfigManager | editable_wfc_mode_bool | true |
| CarrierConfigManager | carrier_default_wfc_ims_enabled_bool | true |
| CarrierConfigManager | carrier_default_wfc_ims_roaming_enabled_bool | true |
| PersistableBundle | getBoolean | Modified on read |

### Settings Hooks

| Hook | Key | Forced Value |
|------|-----|--------------|
| Settings.Global | wfc_ims_enabled | 1 |
| Settings.Global | wfc_ims_mode | 2 (WiFi preferred) |
| Settings.Global | wfc_ims_roaming_enabled | 1 |
| Settings.Global | volte_vt_enabled | 1 |

### Samsung IMS Hooks

| Hook | Method | Action |
|------|--------|--------|
| com.sec.ims.ImsManager | isWfcEntitled | Force true |
| com.sec.ims.ImsManager | isVolteProvisioned | Force true |
| com.sec.ims.ImsManager | isWfcEnabled | Force true |
| com.sec.ims.settings.ImsSettings | getBoolean | Force true on WFC keys |

## Logging

### View Logs

```bash
# All CCO logs
adb logcat -s CCO-Xposed:*

# Specific modules
adb logcat -s CCO-Xposed:I

# Debug level
adb logcat -s CCO-Xposed:D
```

### Log Format

```
[CCO-Xposed] I: Module initialized: v1.0.0 (1)
[CCO-Xposed] I: Hooking android system package
[CCO-Xposed] I: ✓ Hooked: ImsManager.isWfcEnabledByUser
[CCO-Xposed] D: → ImsManager.isWfcEnabledByUser: true [FORCED]
```

## Troubleshooting

### Module Not Loading

**Check LSPosed status:**
```bash
adb shell su -c 'ls -la /data/adb/lspd'
```

**Verify module is enabled:**
1. Open LSPosed Manager
2. Check "CCO Xposed Module" is toggled on
3. Check scope apps are selected

### Hooks Not Working

**Check logs:**
```bash
adb logcat -s CCO-Xposed:* | grep "Hooked"
```

Should see:
```
[CCO-Xposed] I: ✓ Hooked: ImsManager.isWfcEnabledByUser
[CCO-Xposed] I: ✓ Hooked: CarrierConfigManager.getConfigForSubId
```

**Verify scope:**
- Ensure "System Framework (android)" is in scope
- Ensure "Phone (com.android.phone)" is in scope
- Ensure Samsung IMS packages are in scope

### Settings Not Persisting

**Check config file:**
```bash
adb shell su -c 'cat /data/adb/cco/xposed_config.json'
```

**Reset config:**
```bash
adb shell su -c 'rm /data/adb/cco/xposed_config.json'
# Module will recreate with defaults on next hook
```

### Force Reboot

If hooks cause issues:
```bash
# Safe mode (disables all Xposed modules)
adb reboot recovery
# Or hold Volume Down during boot
```

## Development

### Build

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install and test
./gradlew installDebug
adb reboot
```

### Adding New Hooks

1. Create hook class in `hooks/` package
2. Implement hook logic using XposedHelpers
3. Register in `CCOXposedModule.kt`

Example:
```kotlin
class MyHooks(
    private val lpparam: XC_LoadPackage.LoadPackageParam,
    private val logger: CCOLogger,
    private val configManager: ConfigManager
) {
    fun install() {
        val clazz = XposedHelpers.findClass("com.example.MyClass", lpparam.classLoader)
        
        XposedHelpers.findAndHookMethod(
            clazz,
            "myMethod",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    // Hook logic
                    param.result = true
                }
            }
        )
    }
}
```

### Testing

Test on device:
```bash
# Install module
adb install build/outputs/apk/debug/cco-xposed-debug.apk

# Activate in LSPosed Manager

# Reboot
adb reboot

# Check logs
adb logcat -s CCO-Xposed:*

# Test WFC
adb shell am start -a android.settings.WIFI_CALLING_SETTINGS
```

## Comparison: LSPosed vs Frida

| Feature | LSPosed | Frida |
|---------|---------|-------|
| Persistence | ✓ Auto on boot | ✗ Manual session |
| Performance | ✓ Lower overhead | ✗ Higher overhead |
| Development | ✗ Requires rebuild | ✓ Live editing |
| Debugging | ✗ Limited REPL | ✓ Full REPL |
| Scope | ✓ System-wide | ✗ Per-process |
| Safety | ✓ Safe mode available | ✗ Must kill process |

**Use LSPosed when:**
- You want persistent hooks after reboot
- You need system-wide modifications
- You have a stable hook configuration

**Use Frida when:**
- You're actively developing hooks
- You need to debug and iterate quickly
- You want to capture events for analysis

## Security Considerations

- **Root required** - Module needs root for system hooks
- **System modification** - Modifies telephony behavior
- **Carrier implications** - May violate carrier ToS
- **Testing only** - Use on test devices
- **Backup** - Create full backup before use

## Known Issues

1. **Samsung firmware updates** may break hooks (class names change)
2. **Some carriers** have additional server-side checks
3. **Multi-SIM** devices may need per-slot configuration
4. **One UI 7** has different IMS package structure

## Roadmap

- [ ] Per-SIM configuration
- [ ] UI for config management
- [ ] Hook profile system
- [ ] Event capture to file
- [ ] Integration with CCO app
- [ ] One UI 7 support

## Resources

- [LSPosed Documentation](https://github.com/LSPosed/LSPosed)
- [Xposed API Reference](https://api.xposed.info/)
- [CCO Project](https://github.com/supermarsx/carrierconfig-override)

## License

Same as CCO project.

## Author

supermarsx

## Support

- Open an issue on GitHub
- Check LSPosed logs for errors
- Provide device info and logs when reporting issues
