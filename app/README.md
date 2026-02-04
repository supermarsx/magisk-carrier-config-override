# CCO Manager - Android App

CarrierConfig Override Manager - Main Android Application

## Project Structure

```
app/
├── build.gradle.kts           # App build configuration
├── proguard-rules.pro         # ProGuard rules
└── src/main/
    ├── AndroidManifest.xml    # App manifest
    ├── java/com/supermarsx/carrierconfig/
    │   ├── CCOApplication.kt # Application class
    │   ├── ui/
    │   │   ├── MainActivity.kt
    │   │   ├── theme/         # Glassmorphism theme
    │   │   │   ├── Color.kt
    │   │   │   ├── Type.kt
    │   │   │   └── Theme.kt
    │   │   ├── components/    # Reusable glass components
    │   │   │   ├── GlassCard.kt
    │   │   │   ├── GlassButton.kt
    │   │   │   └── StatusChip.kt
    │   │   ├── screens/
    │   │   │   ├── dashboard/
    │   │   │   │   ├── DashboardScreen.kt
    │   │   │   │   └── DashboardViewModel.kt
    │   │   │   └── method1/
    │   │   │       ├── Method1Screen.kt
    │   │   │       └── Method1ViewModel.kt
    │   │   └── navigation/
    │   │       └── NavGraph.kt
    │   ├── data/
    │   │   ├── model/
    │   │   │   ├── DashboardModels.kt
    │   │   │   ├── Method1Models.kt
    │   │   │   └── DiagnosticReport.kt
    │   │   └── repository/
    │   │       ├── DeviceInfoRepository.kt
    │   │       ├── SimInfoRepository.kt
    │   │       ├── ImsStatusRepository.kt
    │   │       ├── WfcUiStatusRepository.kt
    │   │       ├── BlockerDetectionService.kt
    │   │       ├── PresetRepository.kt
    │   │       ├── OverrideXmlBuilder.kt
    │   │       ├── MagiskRepository.kt
    │   │       └── DiagnosticsRepository.kt
    │   ├── di/
    │   │   └── AppModule.kt
    │   └── receiver/
    │       └── CliCommandReceiver.kt
    └── res/
        ├── values/
        │   ├── strings.xml
        │   └── themes.xml
        └── xml/
            ├── backup_rules.xml
            └── data_extraction_rules.xml
```

## Features Implemented

### ✅ Milestone 1: Diagnostics Core [COMPLETE]

- **Glassmorphism Dark Theme**
  - Deep dark backgrounds with gradient overlays
  - Frosted glass surfaces with blur effects
  - Cyan/purple accent colors
  - High contrast typography
  
- **Reusable Glass Components**
  - `GlassCard` - Card with glassmorphism effect
  - `GlassButton` - Primary, secondary, outlined buttons
  - `StatusChip` - Status indicators with colors
  - `GlassIconButton` - Icon buttons with glass effect

- **Dashboard Screen**
  - Device info card (model, One UI version, root status)
  - SIM info cards (carrier, MCC/MNC, network type)
  - IMS status card (registered, VoLTE, VoWiFi)
  - WFC UI status card (Settings activity detection)
  - Blocker analysis card (intelligent detection)
  - Action buttons (diagnostics, WFC settings, export)

- **Data Layer**
  - Device info repository (system properties, root check)
  - SIM info repository (telephony manager integration)
  - IMS status repository (dumpsys ims parser)
  - WFC UI status repository (intent resolver)
  - Blocker detection service (heuristic analysis)

- **Architecture**
  - MVVM pattern with Jetpack Compose
  - Hilt dependency injection
  - Coroutines & Flow for async operations
  - Navigation Compose
  - libsu for root operations

### ✅ Milestone 2: Method 1 - CarrierConfig Overrides [COMPLETE]

- **Preset System**
  - 6 predefined presets:
    - Expose WFC UI
    - WFC Default Enabled
    - Editable WFC Mode
    - Wi-Fi Preferred
    - Wi-Fi Only
    - Full WFC Enablement
  - Custom key support
  - Key catalog with descriptions

- **CarrierConfig Keys**
  - Complete WFC key definitions
  - VoLTE key definitions
  - Type-safe key models (boolean, int, string)
  - Category organization

- **XML Builder**
  - Generates valid CarrierConfig XML
  - Supports all key types
  - XML validation
  - Proper escaping

- **Magisk Integration**
  - Magisk detection
  - Module status checking
  - Override path detection (multiple Samsung variants)
  - Data directory management
  - Backup system
  - Override deployment
  - Revert functionality

- **Method 1 UI**
  - **Presets Tab**: Browse and select presets
  - **Keys Tab**: View selected keys, add custom keys
  - **Deploy Tab**: Prerequisites check, deployment, revert
  - Real-time status updates
  - Clear error messaging

### ✅ Diagnostics Export System [COMPLETE]

- **Report Generation**
  - JSON format (machine-readable)
  - Text format (human-readable)
  - Complete system state capture
  - Timestamp and metadata

- **Export Functionality**
  - Save to external storage
  - Organized directory structure
  - Multiple report history
  - Share capabilities

## Build & Run

1. **Prerequisites**
   - Android Studio Hedgehog or later
   - JDK 17
   - Android SDK 33+

2. **Build**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Install**
   ```bash
   ./gradlew installDebug
   ```

4. **Run**
   - Open in Android Studio
   - Select device/emulator
   - Run 'app' configuration

## Dependencies

- **Jetpack Compose** - Modern UI toolkit
- **Material 3** - Design components
- **Hilt** - Dependency injection
- **Navigation Compose** - Navigation
- **Room** - Local database (future)
- **DataStore** - Preferences (future)
- **WorkManager** - Background tasks (future)
- **libsu** - Root access
- **Timber** - Logging

## Permissions Required

- `READ_PHONE_STATE` - For SIM/telephony info
- `ACCESS_NETWORK_STATE` - For network status
- Root access - For dumpsys, logcat, system modifications

## Next Steps

### Milestone 3: Method 2 Implementation
- [ ] Frida script structure
- [ ] Hook profiles (One UI versions)
- [ ] Session controller
- [ ] Live event stream
- [ ] LSPosed backend support

### Milestone 4: Advanced Features
- [ ] Full diagnostics screen with log viewing
- [ ] CLI utility integration
- [ ] Profile database (Room)
- [ ] Settings screen
- [ ] Update checker
- [ ] Backup/restore configuration

## Usage

### Dashboard
1. Launch the app
2. View device, SIM, IMS, and WFC status
3. Check blocker analysis for recommendations
4. Export diagnostic report for troubleshooting

### Method 1: CarrierConfig Overrides
1. Navigate to Method 1 from Dashboard
2. **Presets Tab**: Select a preset (e.g., "Full WFC Enablement")
3. **Keys Tab**: Review selected keys, add custom keys if needed
4. **Deploy Tab**:
   - Verify prerequisites (root, Magisk)
   - Tap "Deploy Override"
   - Reboot device for changes to take effect
5. **To Revert**: Use "Revert Override" button and reboot

### Export Reports
- From Dashboard, tap "Export Report"
- Reports saved to `/sdcard/Android/data/com.svtt.carrierconfig/files/svtt_reports/`
- Contains JSON and text formats

## Technical Details

### CarrierConfig Override Mechanism
1. App generates XML from selected keys
2. XML saved to `/data/adb/svtt/active/override.xml`
3. Magisk module bind-mounts XML at boot
4. System reads override instead of default config
5. WFC UI appears based on override keys

### Supported Samsung Paths
- `/data/vendor/carrierconfig/override.xml`
- `/data/vendor/carrierconfig/override_carrier.xml`
- `/data/misc/carrierconfig/override.xml`
- `/data/user_de/0/com.android.phone/files/carrierconfig_override.xml`

### Key CarrierConfig Keys
- `carrier_wfc_ims_available_bool` - Enables WFC feature
- `editable_wfc_mode_bool` - Allows mode selection
- `carrier_default_wfc_ims_mode_int` - Default mode (0/1/2)
- `carrier_wfc_supports_wifi_only_bool` - Wi-Fi Only support

## Design System

The app uses a glassmorphism dark theme with:
- **Background**: Deep dark (#0A0E14) with gradients
- **Glass surfaces**: Semi-transparent white (10-30%) with blur
- **Primary accent**: Cyan (#00D9FF)
- **Secondary accent**: Purple (#B24BF3)
- **Success**: Neon green (#00FF88)
- **Warning**: Amber (#FFB020)
- **Error**: Red-pink (#FF3366)

All specifications in [spec-design.md](../spec-design.md).

## License

See LICENSE file for details.

---

**Status**: Milestone 1 & 2 Complete ✅  
**Version**: 1.0.0-alpha  
**Last Updated**: February 4, 2026
