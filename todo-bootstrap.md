# Bootstrap TODO — CarrierConfig Override Manager (CCO)

Implementation checklist for the CCO Manager.

**Status**: Milestones 1 & 2 Complete ✅ | M4 ~70% Complete 🚧  
**Last Updated**: February 4, 2026

**Recent Updates** (Feb 4, 2026):
- ✅ Added Settings & Preferences system with DataStore
- ✅ Implemented Export/Import functionality with kotlinx-serialization
- ✅ Created About screen with app information
- ✅ Built comprehensive diagnostics backend (Logcat, Dumpsys, Connectivity Tests)
- ✅ Added Dashboard FAB with quick actions
- ✅ Created build automation scripts (dev.sh, build.sh, test.sh, lint.sh)
- ✅ Integrated all screens into navigation system

---

## 📋 Project Setup & Infrastructure

### Repository & Development Environment

- [x] Initialize Git repository structure
  - [x] Set up `.gitignore` (Android, Magisk, Frida, IDE)
  - [ ] Create branch strategy (main, develop, feature/*)
  - [ ] Set up issue templates
  - [ ] Configure PR templates
  
- [x] Development Scripts ✅
  - [x] app/scripts/dev.sh (main development automation)
  - [x] app/scripts/build.sh (quick build with size display)
  - [x] app/scripts/test.sh (test runner with device detection)
  - [x] app/scripts/lint.sh (code style checking)
  - [x] Comprehensive script documentation
  
- [ ] Set up CI/CD pipeline
  - [ ] GitHub Actions / GitLab CI configuration
  - [ ] Automated APK builds
  - [ ] Magisk module packaging
  - [ ] Lint and test automation
  
- [x] Documentation structure
  - [x] README.md (main project overview)
  - [x] PROGRESS.md (development status)
  - [x] QUICKREF.md (quick reference guide)
  - [x] spec-design.md (design system)
  - [x] app/README.md (technical details)
  - [x] app/scripts/README.md (scripts documentation) ✅
  - [ ] INSTALL.md (installation guide)
  - [ ] SAFETY.md (safety guidelines)
  - [ ] CONTRIBUTING.md
  - [x] LICENSE file
  - [ ] CHANGELOG.md

---

## 📱 A) Android App: `cco-manager`

### Project Setup

- [x] Create Android Studio project
  - [x] Choose minimum SDK (API 33 for Android 13)
  - [x] Set up Gradle build files
  - [x] Configure ProGuard/R8 rules
  - [x] Set up app signing config
  
- [x] Set up architecture
  - [x] Implement MVVM pattern
  - [x] Set up dependency injection (Hilt)
  - [ ] Configure Room database (planned for profiles)
  - [ ] Set up WorkManager for background tasks (planned)
  - [x] Implement Repository pattern
  
- [x] Add dependencies
  - [x] Jetpack Compose UI toolkit
  - [x] Material 3 components
  - [x] Navigation Compose
  - [x] Lifecycle & ViewModel
  - [x] Coroutines & Flow
  - [ ] Room database (planned for hook profiles)
  - [x] DataStore preferences ✅
  - [x] kotlinx-serialization ✅
  - [x] Gson for JSON
  - [x] libsu 5.2.2 for root operations
  - [ ] accompanist (system UI controller) (planned)

### UI Theme & Design System

- [x] Implement glassmorphism dark theme
  - [x] Define color palette (see spec-design.md)
  - [x] Create Material 3 theme extension
  - [x] Implement blur effects & transparency
  - [x] Design elevation system
  - [x] Create gradient overlays
  - [x] Define typography scale
  - [x] Set up icon system
  
- [x] Create reusable components
  - [x] GlassCard composable (4 variants)
  - [x] GlassButton variants (primary, secondary, outlined)
  - [x] StatusChip (5 status types)
  - [ ] GlassTextField input fields (planned)
  - [ ] InfoPanel with blur background (planned)
  - [ ] LoadingIndicator with glassmorphism (planned)
  - [ ] NavigationBar with glass effect (planned)
  - [ ] TopAppBar with transparency (using standard for now)
  - [ ] BottomSheet with blur (planned)
  - [ ] Dialog with glassmorphism (using AlertDialog for now)
  - [ ] Toggle switches with glass styling (planned)
  - [ ] Dropdown menus with blur (planned)

### Milestone 1: Diagnostics Core ✅ COMPLETE

#### Dashboard Screen

- [x] UI Layout
  - [x] Main container with glassmorphism background
  - [x] Device info card with expandable details
  - [x] SIM info cards (with multi-SIM support)
  - [x] IMS status section with indicators
  - [x] WFC UI status section with confidence
  - [x] Blocker analysis card with recommendations
  - [x] Action button row (Diagnostics, WFC Settings, Export)
  - [x] Pull-to-refresh functionality
  
- [x] Business Logic
  - [x] DeviceInfoRepository (model, manufacturer, One UI, kernel, root)
  - [x] SimInfoRepository (TelephonyManager integration)
  - [x] SimInfoRepository (TelephonyManager integration)
  - [x] ImsStatusRepository (dumpsys ims parser)
  - [x] WfcUiStatusRepository (Settings activity detection)
  - [x] BlockerDetectionService (heuristic logic)
  - [x] DashboardViewModel
  
- [x] Features
  - [x] Real-time status updates
  - [x] Pull-to-refresh
  - [ ] Auto-refresh toggle (planned)
  - [x] Diagnostic scan button ("Run Diagnostics")
  - [x] Open WFC Settings shortcut
  - [x] Export report button (JSON + text)

#### Diagnostics Export

- [x] Report Models
  - [x] DiagnosticReport data class
  - [x] JSON serialization (toJson())
  - [x] Text formatting (toText())
  
- [x] Export Functionality
  - [x] DiagnosticsRepository
  - [x] Export to external storage
  - [x] Timestamped directory structure
  - [x] Dual format output (JSON + TXT)
  - [x] Success/error feedback dialogs
  - [x] File path display

### Milestone 2: Method 1 — CarrierConfig Overrides ✅ COMPLETE

#### Data Models

- [x] CarrierConfig key models
  - [x] CarrierConfigKey (type-safe definition)
  - [x] CarrierConfigPreset (preset with keys)
  - [x] DeploymentConfig (deployment parameters)
  - [x] Method1State (UI state management)
  - [x] WfcConfigKeys (WFC constants)
  - [x] VoLteConfigKeys (VoLTE constants)

#### UI Screens

- [x] UI Layout — Presets Tab
  - [x] Preset cards with descriptions and icons
  - [x] Select preset action
  - [x] Active preset indicator
  - [x] 6 predefined presets displayed
  - [x] Smooth tab transitions
  
- [x] UI Layout — Keys Tab
  - [x] Selected keys list display
  - [x] Key type, name, and value display
  - [x] Empty state when no keys selected
  - [ ] Add custom key button (planned)
  - [ ] Edit/delete key actions (planned)
  - [ ] Search/filter keys (planned)
  - [ ] Import/export keys JSON (planned)
  
- [x] UI Layout — Deploy Tab
  - [x] Prerequisites checklist (Root, Magisk, Path)
  - [x] Status indicators with icons
  - [x] Deploy Override button
  - [x] Revert Override button
  - [x] Success/error dialogs
  - [x] Reboot prompt
  - [x] Progress indicators
  - [ ] SIM slot selector (planned for dual-SIM)
  
#### Business Logic

- [x] PresetRepository
  - [x] 6 predefined configurations
    - [x] Expose WFC UI
    - [x] WFC Default Enabled
    - [x] Editable WFC Mode
    - [x] Wi-Fi Preferred
    - [x] Wi-Fi Only
    - [x] Full WFC Enablement
  - [x] Preset validation
  - [x] Custom key support foundation
  
- [x] OverrideXmlBuilder
  - [x] Type-safe XML generation
  - [x] Boolean, int, string, array support
  - [x] Proper escaping
  - [x] XML validation
  - [x] Pretty-printed output
  
- [x] MagiskRepository
  - [x] Magisk detection
  - [x] Module status checking
  - [x] Multi-path detection (4 Samsung paths)
  - [x] Path validation
  - [x] SVTT data directory management
  - [x] Override deployment
  - [x] Backup system with rotation
  - [x] Revert functionality
  - [x] Root command execution via libsu
  
- [x] Method1ViewModel
  - [x] State management
  - [x] Preset selection
  - [x] Prerequisites validation
  - [x] Deploy/revert orchestration
  - [x] Result handling

#### Navigation Integration

- [x] NavGraph updates
  - [x] Dashboard → Method 1 route
  - [x] Back stack management
  - [ ] Deep linking (planned)

### Milestone 3: Method 2 — Entitlement Simulation ⏳ PLANNED

- [ ] UI Layout — Profiles Tab
  - [ ] Profile selector cards
  - [ ] Profile details view
  - [ ] Active profile indicator
  - [ ] Import/export profile
  - [ ] Create custom profile
  
- [ ] UI Layout — Hooks Tab
  - [ ] Hook enable/disable toggles
  - [ ] Package scope selector
  - [ ] Detected package list
  - [ ] Hook target details
  - [ ] Hook configuration options
  
- [ ] UI Layout — Session Tab
  - [ ] Start/stop session button
  - [ ] Session status indicator
  - [ ] Live event stream (scrollable list)
  - [ ] Event filtering options
  - [ ] Export trace button
  - [ ] Clear session button
  
- [ ] Business Logic
  - [ ] ProfileRepository (hook profiles DB)
  - [ ] HookConfigRepository
  - [ ] FridaSessionManager
  - [ ] LSPosedDetector
  - [ ] PackageScanner (detect entitlement packages)
  - [ ] EventStreamHandler
  - [ ] TraceRecorder
  - [ ] TraceReplayEngine
  - [ ] Method2ViewModel
  
- [ ] Hook Profiles
  - [ ] Generic Samsung IMS profile
  - [ ] One UI 5 profile
  - [ ] One UI 6 profile
  - [ ] One UI 6.1 profile
  - [ ] One UI 7 profile
  - [ ] Carrier-specific profiles

### 4.4 Diagnostics & Logs Screen

- [x] UI Layout ✅
  - [x] 3-tab interface (Logs, Dumpsys, Tests)
  - [x] Preview cards for each tab
  - [x] Coming soon indicators
  - [x] Glassmorphic design
  
- [x] Backend Implementation ✅
  - [x] LogcatRepository (live monitoring + snapshots)
  - [x] DumpsysRepository (ims, phone, carrier_config, telecom, connectivity)
  - [x] ConnectivityTestRepository (comprehensive test suite)
  - [x] DiagnosticsViewModel (state management)
  - [x] Export functionality integration
  
- [x] Features Implemented ✅
  - [x] Logcat filtering (CarrierConfig, IMS, Telephony, WFC, All)
  - [x] Live logcat monitoring
  - [x] Log level filtering (Verbose, Debug, Info, Warning, Error, Fatal)
  - [x] Dumpsys snapshots (multiple services)
  - [x] IMS info extraction from dumpsys
  - [x] Connectivity tests (Network, DNS, Internet, Wi-Fi Calling, IMS, Cellular)
  - [x] Export to JSON + TXT
  
- [ ] Remaining UI Integration
  - [ ] Connect DiagnosticsScreen to ViewModel
  - [ ] Implement log viewer with scrolling
  - [ ] Add syntax highlighting for logs
  - [ ] Implement dumpsys viewer
  - [ ] Create test results display
  - [ ] Add export buttons with feedback

### Settings & About Screen

- [x] Settings Screen ✅
  - [x] General settings (auto-refresh, notifications)
  - [x] Appearance settings (theme, glass effect)
  - [x] Advanced settings (debug mode, export directory, cache)
  - [x] Backup & Data (export/import)
  - [x] Danger Zone (reset settings)
  
- [x] About Screen ✅
  - [x] App version display
  - [x] Feature list with cards
  - [x] Requirements checklist
  - [x] Developer information
  - [x] Legal disclaimer
  
- [x] Backend Implementation ✅
  - [x] PreferencesManager (DataStore)
  - [x] SettingsViewModel (state management)
  - [x] ExportRepository (configuration export/import)
  - [x] Snackbar feedback system
  
- [ ] Remaining Features
  - [ ] Theme selector dialog
  - [ ] Glass strength selector
  - [ ] Directory picker
  - [ ] File picker for import
  - [ ] Cache size calculation
  - [ ] Update checker

### Core Services & Utilities

- [ ] Root Operations
  - [ ] RootShell wrapper (libsu)
  - [ ] SuFileManager
  - [ ] ServiceRestarter
  - [ ] SystemPropertiesReader
  
- [ ] Telephony Integration
  - [ ] TelephonyManagerWrapper
  - [ ] CarrierConfigManagerWrapper
  - [ ] ImsManagerWrapper (reflection)
  - [ ] SubscriptionInfoReader
  
- [ ] File Operations
  - [ ] XmlParser for override files
  - [ ] JsonParser for profiles/traces
  - [ ] FileEncryption for sensitive data
  - [ ] CacheManager
  
- [ ] System Integration
  - [ ] IntentHandler for CLI utility
  - [ ] BroadcastReceiver for system events
  - [ ] WorkManager jobs for periodic checks
  - [ ] NotificationManager for status updates

### Testing

- [ ] Unit Tests
  - [ ] ViewModel tests
  - [ ] Repository tests
  - [ ] Parser tests
  - [ ] Utility function tests
  
- [ ] Integration Tests
  - [ ] Database tests
  - [ ] File operation tests
  - [ ] Root command tests
  
- [ ] UI Tests
  - [ ] Navigation tests
  - [ ] Screen interaction tests
  - [ ] Component tests
  
- [ ] Device Testing Matrix
  - [ ] Single SIM device
  - [ ] Dual SIM device
  - [ ] One UI 5 device
  - [ ] One UI 6 device
  - [ ] One UI 6.1 device
  - [ ] One UI 7 device
  - [ ] OXM multi-CSC firmware
  - [ ] Carrier-specific firmware

---

## 🔧 B) Magisk Module: `cco-carrierconfig`

### Module Structure

- [ ] Create module directory structure
  - [ ] `module.prop` metadata file
  - [ ] `service.sh` (late-start service)
  - [ ] `post-fs-data.sh` (early boot)
  - [ ] `uninstall.sh` (cleanup script)
  - [ ] `system/` (empty, for bind-mount)
  - [ ] `common/` (shared utilities)
  
- [ ] `module.prop` configuration
  - [ ] id: cco-carrierconfig
  - [ ] name: CCO CarrierConfig Override
  - [ ] version & versionCode
  - [ ] author & description
  - [ ] updateJson URL

### post-fs-data.sh Implementation

- [ ] Create required directories
  - [ ] `/data/adb/cco/`
  - [ ] `/data/adb/cco/active/`
  - [ ] `/data/adb/cco/overrides/`
  - [ ] `/data/adb/cco/logs/`
  - [ ] `/data/adb/cco/backups/`
  
- [ ] Set directory permissions
  - [ ] chmod 755 on directories
  - [ ] Set ownership (root:root)
  
- [ ] SELinux context setup
  - [ ] restorecon on target paths
  - [ ] Handle permissive mode if needed
  
- [ ] Logging setup
  - [ ] Initialize module.log
  - [ ] Set log rotation

### service.sh Implementation

- [ ] Wait for data partition mount
  - [ ] Loop with timeout
  - [ ] Verify mount points
  
- [ ] Path detection logic
  - [ ] Detect device model
  - [ ] Check candidate paths:
    - [ ] `/data/vendor/carrierconfig/override.xml`
    - [ ] `/data/vendor/carrierconfig/override_carrier.xml`
    - [ ] `/data/misc/carrierconfig/override.xml`
    - [ ] `/data/user_de/0/com.android.phone/files/carrierconfig_override.xml`
  - [ ] Use heuristics to find active path
  - [ ] Save detected path for future use
  
- [ ] Override file deployment
  - [ ] Check if override file exists
  - [ ] Validate XML structure
  - [ ] Create backup of original (if exists)
  - [ ] Bind-mount override file
  - [ ] Verify mount success
  
- [ ] Service restart (optional)
  - [ ] Check safety flags
  - [ ] Restart com.android.phone if configured
  - [ ] Restart IMS service if configured
  
- [ ] Logging
  - [ ] Log all operations
  - [ ] Log errors with details
  - [ ] Timestamp all entries

### uninstall.sh Implementation

- [ ] Unmount bind-mounts
  - [ ] Find all CCO mounts
  - [ ] Unmount safely
  
- [ ] Restore backups
  - [ ] Restore original configs if backed up
  
- [ ] Remove data directories
  - [ ] Remove `/data/adb/cco/`
  - [ ] Preserve logs option
  
- [ ] Clean up SELinux contexts

### Module Utilities

- [ ] Path detection script
  - [ ] Device fingerprint parser
  - [ ] Known path database
  - [ ] Runtime path scanning
  
- [ ] XML validator
  - [ ] Schema validation
  - [ ] Syntax checking
  
- [ ] Backup manager
  - [ ] Timestamped backups
  - [ ] Restore function
  
- [ ] Log rotator
  - [ ] Max file size
  - [ ] Keep last N logs

### Module Packaging

- [ ] Create installer script
  - [ ] Flash from Magisk Manager
  - [ ] Update existing installation
  - [ ] Version migration
  
- [ ] Module update mechanism
  - [ ] updateJson format
  - [ ] Changelog integration
  - [ ] Download & verify
  
- [ ] Module signing
  - [ ] Sign module ZIP
  - [ ] Verify signature on install

### Testing

- [ ] Test on various Samsung devices
  - [ ] S21 series
  - [ ] S22 series
  - [ ] S23 series
  - [ ] S24 series
  - [ ] A series devices
  - [ ] Fold/Flip devices
  
- [ ] Test scenarios
  - [ ] Fresh install
  - [ ] Module update
  - [ ] Override enable/disable
  - [ ] Uninstall
  - [ ] Multiple reboots
  - [ ] System updates
  
- [ ] Path detection testing
  - [ ] Verify on each firmware
  - [ ] Log detected paths
  - [ ] Validate mount points

---

## 🔌 C) Instrumentation Bundle: `cco-entitlement`

### Frida Backend

- [ ] Frida Scripts
  - [ ] Main agent script structure
  - [ ] Hook loader
  - [ ] Event dispatcher
  - [ ] Session manager
  
- [ ] Hook Implementations
  - [ ] IMS entitlement hooks
    - [ ] isWfcEntitled hook
    - [ ] isVoWiFiEnabled hook
    - [ ] getEntitlementStatus hook
  - [ ] CarrierConfig hooks
    - [ ] getBoolean override
    - [ ] getInt override
    - [ ] getString override
  - [ ] Settings hooks
    - [ ] WFC availability checks
    - [ ] UI rendering gates
  
- [ ] Profile System
  - [ ] Profile loader
  - [ ] Generic Samsung profile
  - [ ] One UI version-specific profiles
  - [ ] Carrier-specific profiles
  
- [ ] Method Signature Detection
  - [ ] Package scanner
  - [ ] Method finder (by name/signature)
  - [ ] Version mapper
  - [ ] Signature database (JSON)
  
- [ ] Event Logging
  - [ ] Event serializer
  - [ ] Send to app via IPC
  - [ ] Buffer management
  
- [ ] Record & Replay
  - [ ] Trace recorder
  - [ ] Trace file format (JSON)
  - [ ] Replay engine
  - [ ] Deterministic behavior

### LSPosed Backend (Optional)

- [ ] Xposed Module Structure
  - [ ] Module manifest
  - [ ] Hook entry point
  - [ ] Scope configuration
  
- [ ] Hook Implementations
  - [ ] Same hooks as Frida but using Xposed APIs
  - [ ] BeforeHook handlers
  - [ ] AfterHook handlers
  - [ ] ReplaceHook handlers
  
- [ ] Profile Management
  - [ ] Load profile from file
  - [ ] Runtime profile switching
  
- [ ] IPC with App
  - [ ] Broadcast receiver
  - [ ] ContentProvider for config
  - [ ] Service for session control

### Detection & Compatibility

- [ ] Package Detection
  - [ ] Scan for IMS packages
  - [ ] Detect carrier entitlement apps
  - [ ] Identify One UI version
  
- [ ] Method Mapping Database
  - [ ] JSON format for signatures
  - [ ] Version-specific mappings
  - [ ] Fallback strategies
  
- [ ] Compatibility Matrix
  - [ ] One UI 5 methods
  - [ ] One UI 6 methods
  - [ ] One UI 6.1 methods
  - [ ] One UI 7 methods
  - [ ] Carrier plugin methods

### Session Management

- [ ] Frida Server Management
  - [ ] Detect Frida server
  - [ ] Start/stop server (if embedded)
  - [ ] Connect to server
  
- [ ] Process Attachment
  - [ ] Attach to target processes
  - [ ] Handle process restarts
  - [ ] Graceful detachment
  
- [ ] Session State
  - [ ] Active/inactive tracking
  - [ ] Event counters
  - [ ] Error handling

### Testing

- [ ] Hook Testing
  - [ ] Test each hook individually
  - [ ] Test hook combinations
  - [ ] Verify return value manipulation
  
- [ ] Profile Testing
  - [ ] Test each profile on target devices
  - [ ] Verify entitlement simulation
  - [ ] Check for side effects
  
- [ ] Stability Testing
  - [ ] Long-running sessions (30+ min)
  - [ ] Multiple attach/detach cycles
  - [ ] Memory leak checks
  - [ ] Crash recovery

---

## 🖥️ D) CLI Utility: `ccoctl` (Optional)

### Command Structure

- [ ] `ccoctl status` - Show current status
  - [ ] Device info
  - [ ] SIM info
  - [ ] IMS status
  - [ ] WFC UI status
  
- [ ] `ccoctl scan` - Run diagnostic scan
  - [ ] Execute full diagnostics
  - [ ] Output report
  
- [ ] `ccoctl export` - Export report
  - [ ] Specify output path
  - [ ] Choose format (json/txt/zip)
  
- [ ] `ccoctl method1` - Method 1 commands
  - [ ] `apply <preset>` - Apply preset
  - [ ] `deploy` - Deploy overrides
  - [ ] `revert` - Revert overrides
  
- [ ] `ccoctl method2` - Method 2 commands
  - [ ] `start <profile>` - Start session
  - [ ] `stop` - Stop session
  - [ ] `status` - Session status
  - [ ] `trace` - Export trace

### Implementation

- [ ] Script structure
  - [ ] Shell script (bash/sh)
  - [ ] Python script option
  - [ ] Argument parser
  
- [ ] ADB Integration
  - [ ] Send intents to app
  - [ ] Read app outputs
  - [ ] File pull/push
  
- [ ] Output Formatting
  - [ ] Plain text output
  - [ ] JSON output option
  - [ ] Color support (if terminal)
  
- [ ] Error Handling
  - [ ] Check for adb
  - [ ] Check device connection
  - [ ] Validate app installation

### Testing

- [ ] Test all commands
- [ ] Test on different platforms (Linux, macOS, Windows)
- [ ] Test error scenarios
- [ ] Test with/without device

---

## 📊 Diagnostics System

### Core Diagnostic Logic

- [ ] DeviceInfo Collector
  - [ ] Model name
  - [ ] Build fingerprint
  - [ ] One UI version
  - [ ] Android version
  - [ ] Kernel version
  - [ ] Root status
  
- [ ] SIM Info Collector
  - [ ] Slot number
  - [ ] Carrier name
  - [ ] MCC/MNC
  - [ ] Network type
  - [ ] Roaming status
  - [ ] SIM state
  
- [ ] IMS Status Collector
  - [ ] Parse `dumpsys ims`
  - [ ] IMS registration state
  - [ ] VoLTE availability
  - [ ] VoWiFi availability
  - [ ] IMS features list
  
- [ ] WFC UI Status Collector
  - [ ] Check Settings activity
  - [ ] Check page population
  - [ ] Check toggle presence
  - [ ] Launch and inspect
  
- [ ] CarrierConfig Collector
  - [ ] Parse `dumpsys carrier_config`
  - [ ] Extract relevant keys
  - [ ] Override detection

### Blocker Detection Heuristics

- [ ] Rule Engine
  - [ ] Define rule structure
  - [ ] Rule evaluation engine
  - [ ] Priority/confidence scoring
  
- [ ] Rules Implementation
  - [ ] IMS not registered → IMS blocker
  - [ ] CarrierConfig WFC unavailable → CarrierConfig blocker
  - [ ] Empty Settings page + IMS registered → CSC/Entitlement blocker
  - [ ] Entitlement calls return false → Entitlement blocker
  - [ ] Multiple blockers detected → Combined blocker
  
- [ ] Confidence Scoring
  - [ ] High confidence indicators
  - [ ] Medium confidence indicators
  - [ ] Low confidence indicators
  - [ ] Suggest next steps

### Report Generation

- [ ] JSON Report Format
  - [ ] Device section
  - [ ] SIM section
  - [ ] IMS section
  - [ ] WFC UI section
  - [ ] CarrierConfig section
  - [ ] Blocker analysis section
  - [ ] Timestamp
  
- [ ] Text Report Format
  - [ ] Human-readable sections
  - [ ] Clear formatting
  - [ ] Actionable recommendations
  
- [ ] Log Collection
  - [ ] Logcat radio buffer
  - [ ] Logcat main buffer
  - [ ] dumpsys outputs
  - [ ] getprop output
  
- [ ] ZIP Packaging
  - [ ] Include all reports
  - [ ] Include logs
  - [ ] Include metadata
  - [ ] Privacy filtering

### Privacy & Security

- [ ] Redaction Rules
  - [ ] Phone numbers (regex)
  - [ ] IMSI numbers
  - [ ] ICCID numbers
  - [ ] Personal identifiers
  
- [ ] Optional Full Logs
  - [ ] User opt-in for unredacted
  - [ ] Warning dialog
  - [ ] Separate file

---

## 🧪 Testing & QA

### Test Scenarios

- [ ] Scenario 1: Baseline empty WFC menu
  - [ ] Fresh device
  - [ ] No modifications
  - [ ] Document behavior
  
- [ ] Scenario 2: Method 1 preset application
  - [ ] Apply "Expose WFC UI"
  - [ ] Verify menu population
  - [ ] Reboot test
  
- [ ] Scenario 3: Method 1 revert
  - [ ] Remove overrides
  - [ ] Verify menu returns to empty
  
- [ ] Scenario 4: Method 2 session
  - [ ] Start entitlement session
  - [ ] Verify menu population
  - [ ] No Method 1 active
  
- [ ] Scenario 5: Method 2 session stop
  - [ ] Stop session
  - [ ] Verify menu returns to empty
  
- [ ] Scenario 6: Dual SIM behavior
  - [ ] Test SIM1 only
  - [ ] Test SIM2 only
  - [ ] Test both SIMs
  
- [ ] Scenario 7: Roaming behavior
  - [ ] Test in-network
  - [ ] Test roaming
  - [ ] Test mode changes
  
- [ ] Scenario 8: System update survival
  - [ ] Apply modifications
  - [ ] Perform OTA update
  - [ ] Verify modifications persist

### Device Testing Matrix

- [ ] Samsung S21 (One UI 5)
- [ ] Samsung S22 (One UI 6)
- [ ] Samsung S23 (One UI 6.1)
- [ ] Samsung S24 (One UI 6.1)
- [ ] Samsung S25 (One UI 7)
- [ ] Samsung A54 (mid-range)
- [ ] Samsung Z Fold 5 (foldable)
- [ ] Samsung Z Flip 5 (flip)

### Firmware Variants

- [ ] OXM (multi-CSC)
- [ ] Carrier-specific (Verizon, AT&T, T-Mobile, etc.)
- [ ] International unlocked

### Acceptance Criteria

- [ ] 1-click report export works
- [ ] Export contains all necessary info
- [ ] Overrides apply and revert cleanly
- [ ] No data loss on revert
- [ ] Instrumentation session stable for 5+ minutes
- [ ] No system crashes
- [ ] No bootloops
- [ ] Logs properly redact sensitive info
- [ ] UI is responsive and smooth
- [ ] Glassmorphism effects render properly

---

## 📚 Documentation

### User Documentation

- [ ] INSTALL.md
  - [ ] Prerequisites
  - [ ] App installation
  - [ ] Magisk module installation
  - [ ] Frida setup (if using Method 2)
  - [ ] LSPosed setup (if using Method 2)
  - [ ] First-time setup wizard
  
- [ ] USER_GUIDE.md
  - [ ] Dashboard explanation
  - [ ] Method 1 guide
  - [ ] Method 2 guide
  - [ ] Diagnostics guide
  - [ ] Troubleshooting
  
- [ ] SAFETY.md
  - [ ] What this toolkit does
  - [ ] What it doesn't do
  - [ ] Risks and limitations
  - [ ] Emergency calling considerations
  - [ ] Backup recommendations
  - [ ] How to revert
  
- [ ] FAQ.md
  - [ ] Common questions
  - [ ] Known issues
  - [ ] Workarounds

### Developer Documentation

- [ ] ARCHITECTURE.md
  - [ ] System overview
  - [ ] Component interactions
  - [ ] Data flow diagrams
  
- [ ] API.md
  - [ ] Internal APIs
  - [ ] CLI utility API
  - [ ] IPC mechanisms
  
- [ ] HOOKS.md
  - [ ] Hook catalog
  - [ ] Method signatures
  - [ ] Profile format
  
- [ ] PROFILES.md
  - [ ] Profile structure
  - [ ] Creating custom profiles
  - [ ] Testing profiles

### Per-Device Notes

- [ ] One UI 5 notes
- [ ] One UI 6 notes
- [ ] One UI 6.1 notes
- [ ] One UI 7 notes
- [ ] Known device-specific issues
- [ ] Workarounds by device

---

## 🚀 Deployment & Distribution

### Release Preparation

- [ ] Version numbering scheme
- [ ] Semantic versioning
- [ ] Release notes template
- [ ] Changelog format
  
### Distribution Channels

- [ ] GitHub Releases
  - [ ] APK upload
  - [ ] Magisk module ZIP
  - [ ] Source code archives
  
- [ ] XDA Forums thread
  - [ ] Create thread
  - [ ] Maintain thread
  
- [ ] Telegram channel (optional)
  - [ ] Update announcements
  - [ ] Support channel

### Update Mechanism

- [ ] App update checker
  - [ ] Check GitHub releases API
  - [ ] Notify user of updates
  - [ ] Download and install
  
- [ ] Magisk module updates
  - [ ] updateJson endpoint
  - [ ] Auto-update in Magisk Manager

---

## 🎯 Milestones Summary

### Milestone 1: Diagnostics Core [PRIORITY]
**Goal:** Basic app with diagnostics and reporting

- [ ] App project setup with glassmorphism theme
- [ ] Dashboard screen
- [ ] Diagnostics collectors
- [ ] Report export
- [ ] WFC settings shortcut

**Deliverable:** Usable diagnostic tool

### Milestone 2: Method 1 Implementation
**Goal:** CarrierConfig override functionality

- [ ] Magisk module structure
- [ ] Override XML builder
- [ ] Module installer in app
- [ ] Preset system
- [ ] Deploy/revert flow

**Deliverable:** Working Method 1 solution

### Milestone 3: Method 2 Frida Backend
**Goal:** Runtime entitlement simulation

- [ ] Frida script structure
- [ ] Basic hook profiles
- [ ] Session controller in app
- [ ] Live event stream
- [ ] Trace export

**Deliverable:** Working Method 2 solution

### Milestone 4: Profiles & Advanced Features
**Goal:** Comprehensive profile support

- [ ] Profile database
- [ ] Record/replay system
- [ ] LSPosed backend
- [ ] CLI utility
- [ ] Advanced diagnostics

**Deliverable:** Complete toolkit with all features

---

## ✅ Definition of Done

For each component:

- [ ] Code is written and follows best practices
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] UI tests pass (if applicable)
- [ ] Code is reviewed
- [ ] Documentation is updated
- [ ] Tested on at least 2 real devices
- [ ] No critical bugs
- [ ] Performance is acceptable
- [ ] Memory leaks checked
- [ ] Accessibility considered
- [ ] Security reviewed
- [ ] Privacy requirements met

---

## 📝 Notes

- **Start with Milestone 1** for quick value delivery
- **Glassmorphism theme** should be implemented early to establish visual identity
- **Test frequently** on real Samsung devices
- **Document everything** as you go
- **Backup strategy** is critical before modifying system behavior
- **Emergency revert** must always be available
- **No network calls** unless explicitly needed for updates
- **Privacy first** — redact by default

---

**Last Updated:** February 4, 2026
**Version:** 1.0
**Status:** Bootstrap phase
