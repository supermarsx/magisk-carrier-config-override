# CCO Instrumentation

Runtime instrumentation framework for Samsung VoWiFi/VoLTE debugging and bypass.

## Overview

The instrumentation system uses **Frida** to hook into Android telephony APIs at runtime, allowing you to:

- Monitor IMS registration status
- Intercept CarrierConfig queries
- Override entitlement checks
- Capture telephony events
- Collect comprehensive diagnostics

## Architecture

```
instrumentation/
├── frida/
│   ├── agent-enhanced.js       # Main Frida agent
│   └── hooks/
│       ├── ims.js              # IMS service hooks
│       ├── carrierconfig.js    # CarrierConfig hooks
│       ├── telephony.js        # Telephony manager hooks
│       ├── settings.js         # Settings provider hooks
│       ├── diagnostics.js      # Diagnostic collection
│       └── utils.js            # Shared utilities
├── frida-launcher              # Python launcher script
└── INSTRUMENTATION.md          # This file
```

### Components

**agent-enhanced.js** - Main agent with:
- RPC exports for runtime control
- Event batching system (1s intervals)
- Statistics tracking
- Module loader for hook categories
- Configuration hot-reload
- Multi-level logging

**Hook Modules:**

1. **ims.js** (15 hook points)
   - `ImsManager.isWfcEntitled` - Force entitlement
   - `ImsManager.isVolteProvisioned` - VoLTE status
   - `ImsFeature.isVowifiEnabled` - Feature availability
   - `ImsRegistry.isRegistered` - Registration state
   - `VoWifiManager` queries
   - `ImsSettings` interception

2. **carrierconfig.js**
   - `CarrierConfigManager.getConfigForSubId` - Config interception
   - `PersistableBundle` modification
   - Force WFC/VoLTE config keys
   - Config change notifications

3. **telephony.js**
   - SIM state monitoring
   - Network type detection
   - Carrier information
   - Subscription management
   - Service state tracking

4. **settings.js**
   - Settings.Global/System/Secure hooks
   - WFC settings interception
   - VoLTE settings modification
   - ContentResolver queries

5. **diagnostics.js**
   - Device information collection
   - Telephony state snapshot
   - IMS state queries
   - System metrics
   - Report generation

6. **utils.js**
   - Data sanitization (IMSI, IMEI masking)
   - Formatting utilities
   - Stack trace capture
   - Java/JS conversion

## Usage

### Prerequisites

1. **Rooted device** with Magisk
2. **frida-tools** installed: `pip install frida-tools`
3. **frida-server** running on device

### Quick Start

```bash
# 1. Install frida-server (one-time setup)
./instrumentation/frida-launcher install-server

# 2. Launch instrumentation
./instrumentation/frida-launcher launch --spawn

# 3. Or use ccoctl
./cli/ccoctl frida
```

### Launcher Options

```bash
# Spawn app with instrumentation
./frida-launcher launch --spawn

# Attach to running app
./frida-launcher launch --no-spawn

# Pause on start for manual inspection
./frida-launcher launch --spawn --pause

# Load custom config
./frida-launcher launch --config config.json
```

### RPC Commands

The agent exports RPC methods for runtime control:

```bash
# Get statistics
./frida-launcher rpc getStats

# Update configuration
./frida-launcher rpc updateConfig --params '{"features":{"autoBypass":true}}'

# Dump CarrierConfig
./frida-launcher rpc dumpCarrierConfig

# Toggle hooks on/off
./frida-launcher rpc toggleHooks --params "true"
```

### Using ccoctl

```bash
# Launch Frida with CCO app
ccoctl frida

# Custom script
ccoctl frida -s path/to/script.js

# Pause on start
ccoctl frida --pause

# Debug mode
ccoctl frida --debug
```

## Configuration

Default configuration in agent:

```javascript
{
  features: {
    autoBypass: true,        // Auto-bypass entitlement checks
    captureEvents: true,     // Capture hook events
    diagnostics: true        // Collect diagnostics
  },
  logging: {
    level: "info",           // debug, info, warn, error
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

Update at runtime:

```javascript
// Via Frida REPL
rpc.exports.updateConfig({
  features: { autoBypass: false },
  logging: { level: "debug" }
});
```

## Event Streaming

Events are batched and sent every 1 second:

```javascript
{
  type: "entitlement_check",
  method: "ImsManager.isWfcEntitled",
  args: [0],
  result: false,
  forced: true,
  timestamp: 1234567890,
  metadata: {
    subId: 0,
    original: false
  }
}
```

Subscribe to events in Python:

```python
import frida

def on_message(message, data):
    if message['type'] == 'send':
        event = message['payload']
        print(f"Event: {event['type']} - {event['method']}")

script.on('message', on_message)
```

## Statistics

Track hook activity:

```javascript
rpc.exports.getStats()
// Returns:
{
  hooksCalled: 1234,
  bytesModified: 567890,
  methodsIntercepted: 45,
  errorsEncountered: 2,
  uptime: 3600000,
  modules: {
    ims: { installed: 15, called: 234 },
    carrierconfig: { installed: 8, called: 89 }
  }
}
```

## Diagnostics

Comprehensive diagnostic collection:

```javascript
rpc.exports.getReport()
// Returns:
{
  timestamp: 1234567890,
  diagnostics: {
    device: { manufacturer, model, androidVersion, ... },
    telephony: { simState, networkType, operator, ... },
    ims: { registered, volteEnabled, wfcEnabled, ... },
    settings: { wfcEnabled, volteEnabled, ... },
    system: { memory, uptime, pid, ... }
  },
  summary: { device, carrier, wfcEnabled, ... }
}
```

Export to file:

```javascript
rpc.exports.exportDiagnostics()
// Saves to /sdcard/cco-diagnostics.json
```

## Hook Details

### IMS Hooks

| Hook | Purpose | Bypass |
|------|---------|--------|
| `ImsManager.isWfcEntitled` | Check VoWiFi entitlement | Force `true` |
| `ImsManager.isVolteProvisioned` | Check VoLTE provisioning | Monitor only |
| `ImsFeature.isVowifiEnabled` | Check feature availability | Monitor only |
| `VoWifiManager.getVoWiFiMode` | Get VoWiFi mode | Monitor only |
| `ImsSettings.getBoolean` | Read IMS settings | Force on WFC keys |

### CarrierConfig Hooks

Forced keys:
```javascript
{
  'carrier_wfc_ims_available_bool': true,
  'editable_wfc_mode_bool': true,
  'carrier_default_wfc_ims_enabled_bool': true,
  'carrier_default_wfc_ims_roaming_enabled_bool': true
}
```

### Telephony Hooks

- **SIM state** - UNKNOWN, ABSENT, READY, etc.
- **Network type** - GPRS, EDGE, LTE, NR, etc.
- **Operator** - MCC/MNC, carrier name
- **Subscriptions** - Active SIMs, default sub

### Settings Hooks

Track and modify:
- `wfc_ims_enabled` (Global)
- `wfc_ims_mode` (0=WiFi only, 1=Cellular preferred, 2=WiFi preferred)
- `volte_vt_enabled` (Global)
- Samsung-specific settings

## Troubleshooting

### frida-server not found

```bash
# Download from GitHub releases
wget https://github.com/frida/frida/releases/download/16.x.x/frida-server-16.x.x-android-arm64.xz
unxz frida-server-*.xz
mv frida-server-* frida-server-android-arm64

# Install
./frida-launcher install-server
```

### App crashes on attach

Try spawning instead:
```bash
./frida-launcher launch --spawn
```

### Permission denied

Ensure frida-server has proper permissions:
```bash
adb shell su -c 'chmod 755 /data/local/tmp/frida-server'
```

### Hooks not working

Check if hooks are enabled:
```bash
./frida-launcher rpc getStats
# Look for "hooksInstalled" counts
```

Toggle hooks:
```bash
./frida-launcher rpc toggleHooks --params "true"
```

## Development

### Adding New Hooks

1. Create hook file in `hooks/`:

```javascript
const hooks = {
    installed: [],
    // ... state
};

function install(config, logEvent, log) {
    // Install hooks
    hooks.installed.push("ClassName.method");
}

module.exports = { install, hooks };
```

2. Load in `agent-enhanced.js`:

```javascript
const myHooks = require('./hooks/my-hooks.js');
myHooks.install(config, logEvent, log);
```

### Testing Hooks

Use Frida REPL:

```javascript
// List loaded classes
Java.enumerateLoadedClasses({
  onMatch: (name) => {
    if (name.indexOf("ImsManager") >= 0) {
      console.log(name);
    }
  },
  onComplete: () => {}
});

// Test method availability
Java.use("android.telephony.ims.ImsManager").isWfcEnabled;
```

## Security Considerations

- **Sensitive data** - Hooks sanitize IMSI, IMEI, phone numbers
- **Logging** - Disable file logging in production
- **Root required** - frida-server needs root access
- **Permissions** - Instrument only apps you own/control

## Resources

- [Frida Documentation](https://frida.re/docs/)
- [Frida CodeShare](https://codeshare.frida.re/)
- [Android Telephony APIs](https://developer.android.com/reference/android/telephony/package-summary)
- [Samsung IMS Documentation](https://developer.samsung.com/)

## License

Same as CCO project.
