# CCO CarrierConfig Override Profiles

This directory contains pre-made CarrierConfig override profiles for common use cases.

## Available Profiles

### 1. `generic_wfc_enable.xml`
**Purpose:** Standard Wi-Fi Calling enablement with recommended settings
**Mode:** Wi-Fi Preferred
**Use when:** You want to enable Wi-Fi Calling with balanced settings

**Key features:**
- Enables Wi-Fi Calling availability
- Sets Wi-Fi Preferred mode by default
- Allows user to change modes
- Enables VoLTE alongside Wi-Fi Calling
- Disables provisioning requirements

### 2. `wifi_only_mode.xml`
**Purpose:** Force all voice calls through Wi-Fi exclusively
**Mode:** Wi-Fi Only
**Use when:** You want to prevent cellular voice calls entirely

**⚠️ WARNING:** This profile will prevent cellular voice calls even when Wi-Fi is unavailable. Only use if you understand the implications.

**Key features:**
- Forces Wi-Fi Only calling mode
- Prevents fallback to cellular network
- Useful for devices without cellular service
- Good for data-only SIMs with Wi-Fi Calling

### 3. `aggressive_enable.xml`
**Purpose:** Maximum enablement with carrier restriction bypass
**Mode:** Wi-Fi Preferred
**Use when:** Generic profile doesn't work, carrier blocks Wi-Fi Calling

**Key features:**
- Disables all provisioning checks
- Bypasses entitlement requirements
- Removes UI restrictions
- Forces IMS registration
- Samsung-specific tweaks
- Most comprehensive override set

## How to Use Profiles

### Method 1: Via CCO Manager App (Recommended)
1. Open CCO Manager app
2. Go to CarrierConfig tab
3. Select a profile from the list
4. Tap "Deploy Profile"
5. Reboot device

### Method 2: Manual Installation
```bash
# Copy profile to active directory
adb push generic_wfc_enable.xml /sdcard/
adb shell su -c 'cp /sdcard/generic_wfc_enable.xml /data/adb/cco/active/override.xml'
adb shell su -c 'chmod 644 /data/adb/cco/active/override.xml'

# Reboot device
adb reboot
```

### Method 3: Using CLI Tool
```bash
./ccoctl deploy-profile generic_wfc_enable
./ccoctl reboot
```

## Profile Selection Guide

| Scenario | Recommended Profile |
|----------|-------------------|
| First-time setup on most carriers | `generic_wfc_enable.xml` |
| Carrier blocks Wi-Fi Calling | `aggressive_enable.xml` |
| Data-only SIM, no cellular voice | `wifi_only_mode.xml` |
| International roaming | `generic_wfc_enable.xml` |
| Testing/debugging | `aggressive_enable.xml` |

## Creating Custom Profiles

You can create custom profiles by:

1. Copy an existing profile as a template
2. Modify the XML values according to your needs
3. Save with a descriptive name
4. Deploy using one of the methods above

### Common Configuration Keys

**Boolean Values:**
- `carrier_wfc_ims_available_bool` - Enable/disable Wi-Fi Calling feature
- `carrier_default_wfc_ims_enabled_bool` - Default on/off state
- `editable_wfc_mode_bool` - Allow user to change mode
- `carrier_volte_available_bool` - Enable VoLTE
- `require_entitlement_checks_bool` - Carrier entitlement verification

**Integer Values:**
- `carrier_default_wfc_ims_mode_int` - Default calling mode:
  - `0` = Cellular Preferred
  - `1` = Wi-Fi Preferred
  - `2` = Wi-Fi Only

## Troubleshooting

**Profile not taking effect:**
1. Verify file is at `/data/adb/cco/active/override.xml`
2. Check file permissions: `chmod 644 override.xml`
3. Verify module is enabled in Magisk
4. Check module logs: `/data/adb/cco/logs/module.log`
5. Ensure you've rebooted after deployment

**Wi-Fi Calling still not showing:**
1. Try `aggressive_enable.xml` profile
2. Check Settings → Connections → Wi-Fi Calling
3. Run diagnostics in CCO Manager app
4. Check if SIM supports Wi-Fi Calling at all
5. Verify carrier hasn't completely removed the feature from firmware

**Device unstable after profile:**
1. Boot into safe mode
2. Disable CCO module in Magisk
3. Reboot normally
4. Try a less aggressive profile

## Safety Notes

- **Always backup:** Module automatically backs up original files
- **Test first:** Try generic profile before aggressive
- **Revertible:** Simply disable module or delete override file
- **Logs available:** Check `/data/adb/cco/logs/` for detailed logs
- **No guarantees:** Results vary by device, carrier, and firmware

## Profile Versioning

Current profile version: 1.0.0
Compatible with CCO Module: 1.0.0+
Last updated: 2026-02-04

## Contributing

Found a profile that works better for your carrier? Share it!
Submit profiles via GitHub pull request or issue.
