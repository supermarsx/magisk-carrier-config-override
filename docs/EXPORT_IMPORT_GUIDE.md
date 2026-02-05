# Export & Import Guide

**CCO Manager** - Comprehensive Configuration Export/Import  
**Last Updated**: February 5, 2026

---

## 📦 Overview

CCO Manager provides comprehensive export/import functionality for all major app configurations, allowing you to:
- **Backup** your settings and configurations
- **Share** custom presets and profiles with others
- **Migrate** configurations between devices
- **Version control** your setup

---

## 🎯 What Can Be Exported/Imported

### 1. **App Settings & Preferences** ✅
**Location**: Settings → Backup & Data

**What's Included**:
- General settings (auto-refresh, notifications)
- Appearance settings (theme, glass strength)
- Advanced settings (debug mode, export directory)
- Backup preferences (auto-backup, frequency)

**Format**: JSON  
**File Extension**: `.json`  
**Use Case**: Backup all app preferences, migrate settings to new device

**How to Export**:
1. Open **Settings** screen
2. Scroll to **Backup & Data** section
3. Tap **Export Configuration**
4. File saved to `/sdcard/CCO/exports/config_[timestamp].json`

**How to Import**:
1. Open **Settings** screen
2. Scroll to **Backup & Data** section
3. Tap **Import Configuration**
4. Select JSON file from file picker
5. Settings automatically applied

---

### 2. **CarrierConfig Presets** ✅ NEW!
**Location**: CarrierConfig screen → Top bar actions

**What's Included**:
- Preset ID and name
- Preset description
- All CarrierConfig key overrides
- Boolean, integer, and string values
- Metadata

**Format**: JSON  
**File Extension**: `.json`  
**Use Case**: Share custom CarrierConfig presets, backup modified presets

**How to Export**:
1. Open **CarrierConfig** screen
2. Select a preset from the list
3. Tap **Download icon** (top bar)
4. Choose **Export Selected Preset**
5. Choose save location and filename
6. File saved as `[preset-id].json`

**How to Import**:
1. Open **CarrierConfig** screen
2. Tap **Upload icon** (top bar)
3. Select preset JSON file
4. Preset automatically added to list
5. Can now deploy imported preset

**Example Preset JSON**:
```json
{
  "id": "custom_wifi_calling",
  "name": "Custom WiFi Calling",
  "description": "My custom WFC configuration",
  "overrides": {
    "carrier_wfc_ims_available_bool": true,
    "carrier_volte_available_bool": true,
    "editable_wfc_mode_bool": true,
    "carrier_default_wfc_ims_mode_int": 2,
    "carrier_default_wfc_ims_enabled_bool": true
  }
}
```

---

### 3. **Hook Profiles (Frida/LSPosed)** ✅ NEW!
**Location**: Entitlement screen → Top bar actions

**What's Included**:
- Profile ID, name, description
- Device compatibility info (manufacturer, model, OS version)
- Carrier targeting (MCC/MNC)
- Hook configuration (methods to hook)
- CarrierConfig keys to force
- Settings keys to modify
- Metadata and version info

**Format**: JSON  
**File Extension**: `.json`  
**Use Case**: Share custom hook profiles, community contributions, device-specific configs

**How to Export**:
1. Open **Entitlement** screen
2. Select a profile from the list
3. Tap **Download icon** (top bar)
4. Choose **Export Selected Profile**
5. Choose save location and filename
6. File saved as `[profile-id].json`

**How to Import**:
1. Open **Entitlement** screen
2. Tap **Upload icon** (top bar)
3. Select profile JSON file
4. Profile automatically saved and added to list
5. Can now use imported profile for instrumentation

**Example Profile JSON**:
```json
{
  "id": "custom_oneui6_s24",
  "name": "Galaxy S24 Custom Profile",
  "description": "Optimized for S24 on One UI 6.1",
  "version": "1.0",
  "compatibility": {
    "manufacturer": "samsung",
    "models": ["SM-S921U", "SM-S926U", "SM-S928U"],
    "minSdkVersion": 34,
    "maxSdkVersion": 34,
    "oneUiVersions": ["6.1"]
  },
  "targeting": {
    "mccMnc": ["310260", "310490"],
    "carriers": ["T-Mobile US"]
  },
  "hooks": {
    "imsHooks": [
      "isWfcEnabledByPlatform",
      "isVolteEnabledByPlatform",
      "isWfcEntitled"
    ],
    "carrierConfigKeys": {
      "carrier_wfc_ims_available_bool": true,
      "carrier_volte_available_bool": true,
      "editable_wfc_mode_bool": true
    },
    "settingsKeys": {
      "wfc_ims_enabled": "1",
      "volte_vt_enabled": "1"
    }
  }
}
```

---

### 4. **Diagnostics Reports** ✅
**Location**: Dashboard → Export Report

**What's Included**:
- Device information (model, OS, kernel)
- SIM status (carrier, MCC/MNC, network type)
- IMS status (VoLTE/VoWiFi availability)
- WFC UI detection results
- Blocker analysis
- Timestamp and metadata

**Formats**: JSON + TXT  
**File Extensions**: `.json`, `.txt`  
**Use Case**: Troubleshooting, bug reports, community support

**How to Export**:
1. Open **Dashboard**
2. Tap **FAB** (floating action button)
3. Select **Export Report**
4. Files saved to `/sdcard/CCO/exports/`
   - `diagnostics_[timestamp].json` (machine-readable)
   - `diagnostics_[timestamp].txt` (human-readable)

---

## 🔄 Common Workflows

### Backup Everything
1. **Export app settings** (Settings → Backup & Data)
2. **Export all custom presets** (CarrierConfig screen)
3. **Export custom profiles** (Entitlement screen)
4. Store files in cloud storage or backup location

### Share Custom Configuration
1. Create and test your custom preset/profile
2. Export to JSON file
3. Share file via:
   - Email attachment
   - Cloud link (Google Drive, Dropbox)
   - GitHub repository
   - XDA forum post
   - Direct file transfer

### Migrate to New Device
1. Export all configs from old device
2. Install CCO Manager on new device
3. Import app settings first
4. Import custom presets
5. Import custom profiles
6. Verify functionality

### Community Contribution
1. Create optimized profile for your device
2. Test thoroughly
3. Export profile to JSON
4. Document compatibility (device model, OS version, carrier)
5. Submit to CCO GitHub repository or forum

---

## 📁 File Locations

### Export Directory
**Default**: `/sdcard/CCO/exports/`  
**Customizable**: Yes (Settings → Advanced → Export Directory)

### File Naming Convention
- **App configs**: `config_[YYYYMMDD_HHmmss].json`
- **Diagnostics**: `diagnostics_[YYYYMMDD_HHmmss].json`
- **Presets**: `[preset-id].json` (user-defined)
- **Profiles**: `[profile-id].json` (user-defined)

### Storage Requirements
- App settings: ~5-10 KB
- CarrierConfig preset: ~2-5 KB
- Hook profile: ~10-20 KB
- Diagnostics report: ~20-50 KB (JSON + TXT)

---

## 🔒 Security & Privacy

### What's NOT Exported
- **Sensitive data**: IMSI, IMEI, phone numbers, SIM serial
- **Credentials**: Root tokens, API keys
- **System state**: Running processes, memory dumps
- **Personal data**: Call logs, contacts, messages

### Data Sanitization
All exports automatically sanitize sensitive information:
- Device identifiers replaced with generic values
- Personal information redacted
- Only configuration data included

### Safe Sharing
- ✅ **Safe to share**: App settings, presets, profiles
- ⚠️ **Review first**: Diagnostics reports (may contain carrier info)
- ❌ **Never share**: Full system dumps, logs with personal data

---

## 🛠️ Technical Details

### JSON Schema

#### App Configuration
```typescript
{
  version: string,           // App version
  exportDate: number,        // Unix timestamp
  settings: {
    autoRefresh: boolean,
    enableNotifications: boolean,
    debugMode: boolean,
    theme: string,
    glassStrength: string,
    exportDirectory: string
  },
  customKeys: Array<CustomKey>
}
```

#### CarrierConfig Preset
```typescript
{
  id: string,                // Unique preset ID
  name: string,              // Display name
  description: string,       // User description
  overrides: {
    [key: string]: boolean | number | string
  }
}
```

#### Hook Profile
```typescript
{
  id: string,
  name: string,
  description: string,
  version: string,
  compatibility: {
    manufacturer: string,
    models: string[],
    minSdkVersion: number,
    maxSdkVersion: number,
    oneUiVersions: string[]
  },
  targeting: {
    mccMnc: string[],
    carriers: string[]
  },
  hooks: {
    imsHooks: string[],
    carrierConfigKeys: object,
    settingsKeys: object
  }
}
```

### Import Validation
All imports are validated for:
1. **JSON syntax**: Valid JSON structure
2. **Schema compliance**: Required fields present
3. **Type checking**: Correct data types
4. **Version compatibility**: Supported version range
5. **Security**: No malicious content

### Error Handling
- Invalid JSON → Error message with parse details
- Missing fields → Fallback to defaults
- Incompatible version → Warning with migration option
- IO errors → Detailed error message

---

## 🐛 Troubleshooting

### Import Fails
**Problem**: "Failed to import configuration"

**Solutions**:
1. Verify file is valid JSON (use JSON validator)
2. Check file permissions (must be readable)
3. Ensure file extension is `.json`
4. Try exporting and re-importing to test
5. Check logs for detailed error message

### Export Directory Not Found
**Problem**: "Export failed: Directory not found"

**Solutions**:
1. Grant storage permissions to CCO Manager
2. Create directory manually: `/sdcard/CCO/exports/`
3. Change export directory in Settings → Advanced
4. Check available storage space

### Profile Not Loading
**Problem**: Imported profile doesn't appear in list

**Solutions**:
1. Verify profile JSON structure
2. Check for duplicate profile ID
3. Ensure compatibility section is valid
4. Restart app to refresh profile list
5. Check ProfileManager logs

---

## 📚 Examples & Use Cases

### Use Case 1: Backup Before System Update
```bash
# Before update
1. Export app settings
2. Export all custom presets
3. Export working hook profiles
4. Store files safely

# After update
1. Reinstall CCO Manager
2. Import app settings
3. Import presets
4. Import profiles
5. Test functionality
```

### Use Case 2: Share with Community
```markdown
## My Custom S24 Profile

**Device**: Samsung Galaxy S24 Ultra (SM-S928U)  
**Carrier**: T-Mobile US  
**One UI**: 6.1  
**Android**: 14

**What it does**:
- Enables WiFi Calling UI
- Forces VoLTE provisioning
- Unlocks WFC mode selection

**Download**: [s24_ultra_tmobile.json](link)

**Installation**:
1. Open CCO Manager
2. Navigate to Entitlement screen
3. Tap Upload icon
4. Select downloaded JSON file
5. Profile ready to use!
```

### Use Case 3: Development Testing
```bash
# Export baseline
./export_baseline.sh

# Make changes
# Test modifications

# Compare
diff baseline.json modified.json

# Keep or revert
./import_config.sh baseline.json  # Revert
```

---

## 🎓 Best Practices

### Export Strategy
1. **Regular backups**: Weekly or before major changes
2. **Version naming**: Include date or version in filename
3. **Cloud storage**: Keep exports in Google Drive/Dropbox
4. **Test restores**: Verify imports work before needed

### Import Strategy
1. **Review first**: Check JSON content before importing
2. **Backup current**: Export current config before import
3. **Trusted sources**: Only import from verified sources
4. **Test separately**: Import one item at a time to isolate issues

### Sharing Strategy
1. **Document well**: Include device info, OS version, carrier
2. **Test thoroughly**: Ensure config works on your device first
3. **Include instructions**: How to install and use
4. **Version control**: Use GitHub for community profiles
5. **Update regularly**: Maintain profiles for new OS versions

---

## 🔗 Related Documentation

- [INSTALL.md](INSTALL.md) - Installation and setup
- [INSTRUMENTATION_GUIDE.md](INSTRUMENTATION_GUIDE.md) - Hook profiles guide
- [CONTRIBUTING.md](CONTRIBUTING.md) - How to contribute profiles
- [README.md](../readme.md) - Main documentation

---

## 📞 Support

### Getting Help
- **GitHub Issues**: Report bugs or request features
- **Discussions**: Ask questions, share configs
- **XDA Forum**: Community support and custom profiles

### Contributing
We welcome contributions of:
- Device-specific profiles
- Carrier-specific configurations
- Bug fixes and improvements
- Documentation updates

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

---

*End of Export & Import Guide*
