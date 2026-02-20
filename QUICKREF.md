# CCO Manager Quick Reference Guide

## 🚀 Getting Started

### First Time Setup
1. Install APK on rooted Samsung device
2. Grant root access when prompted
3. Open app → Dashboard loads device status
4. Check "Blocker Analysis" card for recommendations

### Deploy WFC Override (Method 1)

#### Quick Deploy (Recommended)
```
Dashboard → Method 1 → Presets Tab
→ Select "Full WFC Enablement"
→ Deploy Tab → Deploy Override
→ Reboot device
→ Settings → Connections → Wi-Fi Calling ✓
```

#### Custom Configuration
```
Dashboard → Method 1 → Keys Tab
→ Tap "+" to add custom keys
→ Review selection
→ Deploy Tab → Deploy Override
→ Reboot device
```

#### Revert Changes
```
Method 1 → Deploy Tab
→ Revert Override
→ Reboot device
```

---

## 📱 App Structure

### Navigation
```
Dashboard (Home)
├─ Method 1 (CarrierConfig Override)
│  ├─ Presets Tab
│  ├─ Keys Tab
│  └─ Deploy Tab
├─ Method 2 (Runtime Hooks) [Coming Soon]
└─ Settings [Coming Soon]
```

### Dashboard Cards
1. **Device Info** - Model, One UI version, kernel, root status
2. **SIM Info** - Carrier, MCC/MNC, network type
3. **IMS Status** - VoLTE, VoWiFi, registration
4. **WFC UI Status** - Settings activity detection
5. **Blocker Analysis** - Intelligent detection & recommendations

### Actions
- **Run Diagnostics** - Refresh all status cards
- **Open WFC Settings** - Launch Wi-Fi Calling settings (if available)
- **Export Report** - Generate diagnostic report

---

## 🎯 Method 1: CarrierConfig Override

### How It Works
1. App generates XML from selected CarrierConfig keys
2. XML saved to `/data/adb/svtt/active/override.xml`
3. Magisk module bind-mounts XML to system path
4. System reads override instead of default config
5. WFC UI appears based on override keys

### Presets

| Preset | Purpose | Keys Modified | Recommended |
|--------|---------|---------------|-------------|
| **Expose WFC UI** | Show settings only | 2 keys | Basic |
| **WFC Default Enabled** | Auto-enable on boot | 3 keys | Medium |
| **Editable WFC Mode** | Allow mode selection | 4 keys | Medium |
| **Wi-Fi Preferred** | Default to Wi-Fi | 5 keys | Advanced |
| **Wi-Fi Only** | Force Wi-Fi calling | 6 keys | Advanced |
| **Full WFC Enablement** | Complete unlock | 8 keys | ⭐ Recommended |

### Key CarrierConfig Keys

```xml
<!-- Core WFC Keys -->
carrier_wfc_ims_available_bool = true
editable_wfc_mode_bool = true
carrier_default_wfc_ims_enabled_bool = true
carrier_default_wfc_ims_roaming_enabled_bool = true

<!-- Mode Control -->
carrier_default_wfc_ims_mode_int = 1  # 0=Cell, 1=WiFi Preferred, 2=WiFi Only
carrier_wfc_supports_wifi_only_bool = true

<!-- UI Control -->
use_wfc_home_network_mode_in_roaming_network_bool = false

<!-- Advanced -->
carrier_promote_wfc_on_call_fail_bool = true
```

### Prerequisites Checklist
- ✅ Root access granted
- ✅ Magisk 24+ installed and working
- ✅ Valid CarrierConfig path detected
- ✅ Keys selected (at least 1)

### Deployment Process
```
1. Prerequisites Check
   ├─ Verify root access
   ├─ Check Magisk installation
   └─ Detect CarrierConfig path

2. XML Generation
   ├─ Build XML from selected keys
   ├─ Validate syntax
   └─ Preview (Keys tab)

3. Backup Current Config
   ├─ Check if override exists
   ├─ Create timestamped backup
   └─ Store in /data/adb/svtt/backups/

4. Deploy Override
   ├─ Write XML to /data/adb/svtt/active/
   ├─ Set permissions (644)
   └─ Log deployment

5. User Action Required
   └─ Reboot device for changes to take effect
```

---

## 📊 Diagnostics Export

### Report Contents
- Device information (model, OS, root)
- SIM information (carrier, network)
- IMS status (registration, features)
- WFC UI status (activity detection)
- Blocker analysis (detection results)
- Timestamp and metadata

### Export Location
```
/sdcard/Android/data/com.svtt.carrierconfig/files/svtt_reports/
└─ YYYY-MM-DD_HH-MM-SS/
   ├─ diagnostic_report.json  # Machine-readable
   └─ diagnostic_report.txt   # Human-readable
```

### Sharing Reports
1. Export Report from Dashboard
2. Navigate to export location using file manager
3. Share JSON or TXT file via email/messaging
4. Attach to GitHub issues for support

---

## 🔧 Troubleshooting

### WFC UI Not Appearing After Deploy

**Checklist**:
1. Did you reboot after deployment? (Required)
2. Is Magisk module enabled?
3. Check Deploy tab → Prerequisites all green?
4. Try "Full WFC Enablement" preset
5. Export diagnostic report for analysis

**Manual Verification**:
```bash
# Check if override exists
adb shell su -c "ls -la /data/vendor/carrierconfig/"

# Verify XML content
adb shell su -c "cat /data/adb/svtt/active/override.xml"

# Check bind-mount status (requires Magisk module)
adb shell mount | grep carrierconfig
```

### Deployment Fails

**Common Causes**:
- No root access → Grant root permission
- Magisk not installed → Install Magisk 24+
- No valid path found → Device may not support CarrierConfig override
- Permissions denied → Re-grant root access

### App Crashes on Launch

**Solutions**:
1. Clear app data: Settings → Apps → SVTT → Clear Data
2. Reinstall APK
3. Check logcat: `adb logcat | grep svtt`
4. Export and share crash logs

### Blocker Analysis Shows "Unknown"

**Reasons**:
- Insufficient permissions → Grant all requested permissions
- Detection heuristics uncertain → Try deployment anyway
- Device model not recognized → May still work

---

## 🛡️ Safety Tips

### Before Deploying
1. ✅ **Backup device** - Full system backup recommended
2. ✅ **Export diagnostic report** - For restore reference
3. ✅ **Test on non-critical device** - If possible
4. ✅ **Know how to revert** - Practice revert process

### After Deploying
1. ✅ **Test emergency calls** - Verify 911/112 works
2. ✅ **Check carrier billing** - Ensure no extra charges
3. ✅ **Monitor IMS status** - Use Dashboard to check health
4. ✅ **Keep backup available** - In case revert needed

### Emergency Revert
```
Method 1: Via App
└─ Method 1 → Deploy Tab → Revert Override → Reboot

Method 2: Via ADB
└─ adb shell su -c "rm /data/adb/svtt/active/override.xml"
   adb reboot

Method 3: Via Recovery
└─ Boot to recovery → ADB shell → rm override.xml → reboot
```

---

## 📁 File Locations

### App Data
```
/data/data/com.supermarsx.carrierconfig/
├─ files/
│  └─ cco_reports/           # Exported diagnostic reports
└─ shared_prefs/             # App settings (future)
```

### CCO System Data
```
/data/adb/cco/
├─ active/
│  └─ override.xml           # Active override configuration
├─ backups/
│  └─ [timestamp].xml        # Backup history
└─ logs/                     # Deployment logs (future)
```

### CarrierConfig Paths (Samsung)
```
Priority 1: /data/vendor/carrierconfig/override.xml
Priority 2: /data/vendor/carrierconfig/override_carrier.xml
Priority 3: /data/misc/carrierconfig/override.xml
Priority 4: /data/user_de/0/com.android.phone/files/carrierconfig_override.xml
```

---

## 🎨 UI Reference

### Status Colors
- **Green** - Active, Working, Success
- **Red** - Inactive, Blocked, Error
- **Yellow** - Warning, Uncertain
- **Gray** - Unknown, Not Detected
- **Cyan** - Info, Primary Action
- **Purple** - Secondary Action

### Button Types
- **Primary** (Cyan) - Main actions (Deploy, Export)
- **Secondary** (Purple) - Alternative actions (Revert)
- **Outlined** (Transparent) - Tertiary actions (Cancel)

### Card Types
- **Default** - Standard information display
- **Elevated** - Important status/warnings
- **Outlined** - Less emphasis, grouped info
- **Emphasized** - Critical actions required

---

## 🔑 Key Terminology

| Term | Meaning |
|------|---------|
| **WFC** | Wi-Fi Calling |
| **VoWiFi** | Voice over Wi-Fi (same as WFC) |
| **VoLTE** | Voice over LTE |
| **IMS** | IP Multimedia Subsystem (infrastructure for VoLTE/VoWiFi) |
| **CarrierConfig** | Android's carrier configuration system |
| **Override** | Custom configuration that replaces default |
| **Bind-mount** | Magisk technique to replace files without modifying /system |
| **Entitlement** | Carrier permission check for VoWiFi |
| **One UI** | Samsung's Android skin |

---

## 📞 Support Resources

### Documentation
- [Main README](README.md) - Project overview
- [App README](app/README.md) - Technical details
- [Design Spec](docs/spec-design.md) - UI/UX design system
- [Bootstrap TODO](todo-bootstrap.md) - Implementation roadmap
- [Progress Summary](PROGRESS.md) - Development status

### Community
- **GitHub Issues** - Bug reports and feature requests
- **GitHub Discussions** - General questions and help
- **Wiki** (Coming Soon) - Detailed guides and tutorials

### Before Asking for Help
1. Export diagnostic report from app
2. Check troubleshooting section above
3. Search existing GitHub issues
4. Include: Device model, One UI version, carrier, exact steps

---

**Last Updated**: February 4, 2026  
**Version**: 1.0.0-alpha  
**Status**: Milestone 1 & 2 Complete
