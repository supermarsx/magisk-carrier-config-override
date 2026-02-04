#!/system/bin/sh
###############################################################################
# CCO CarrierConfig Override - Install Helper Script
# Called during module installation to validate environment
###############################################################################

MODPATH="${0%/*}"

ui_print() {
    echo "$1"
}

ui_print "=========================================="
ui_print "  CCO CarrierConfig Override v1.0.0"
ui_print "=========================================="
ui_print ""
ui_print "Installing CCO module..."
ui_print ""

# Check Magisk version
MAGISK_VER_CODE=$(magisk -V 2> /dev/null || echo 0)
if [ "$MAGISK_VER_CODE" -lt 24000 ]; then
    ui_print "! Warning: Magisk 24.0+ recommended"
    ui_print "! Current version may not fully support bind mounts"
fi

# Check device info
DEVICE=$(getprop ro.product.model)
ANDROID=$(getprop ro.build.version.release)
SDK=$(getprop ro.build.version.sdk)

ui_print "Device: $DEVICE"
ui_print "Android: $ANDROID (SDK $SDK)"
ui_print ""

# Validate Android version
if [ "$SDK" -lt 33 ]; then
    ui_print "! Warning: Android 13+ (SDK 33+) recommended"
    ui_print "! This device is running SDK $SDK"
    ui_print "! Module may not work properly"
    ui_print ""
fi

# Check for Samsung
MANUFACTURER=$(getprop ro.product.manufacturer | tr '[:upper:]' '[:lower:]')
if echo "$MANUFACTURER" | grep -q "samsung"; then
    ui_print "✓ Samsung device detected"
else
    ui_print "⚠ Non-Samsung device detected"
    ui_print "  Module is optimized for Samsung devices"
    ui_print "  Functionality may be limited"
fi
ui_print ""

# Check for existing CarrierConfig directories
ui_print "Checking CarrierConfig paths..."
CCO_PATHS_FOUND=0
if [ -d "/data/vendor/carrierconfig" ]; then
    ui_print "  ✓ /data/vendor/carrierconfig"
    CCO_PATHS_FOUND=$((CCO_PATHS_FOUND + 1))
fi
if [ -d "/data/misc/carrierconfig" ]; then
    ui_print "  ✓ /data/misc/carrierconfig"
    CCO_PATHS_FOUND=$((CCO_PATHS_FOUND + 1))
fi

if [ "$CCO_PATHS_FOUND" -eq 0 ]; then
    ui_print "  ! No standard paths found"
    ui_print "  Module will create default path"
fi
ui_print ""

# Create CCO data directory structure
ui_print "Creating CCO data directory..."
mkdir -p /data/adb/cco/overrides
mkdir -p /data/adb/cco/active
mkdir -p /data/adb/cco/logs
mkdir -p /data/adb/cco/backup

if [ -d "/data/adb/cco" ]; then
    ui_print "  ✓ Created /data/adb/cco"
    chmod 755 /data/adb/cco
    chmod 755 /data/adb/cco/overrides
    chmod 755 /data/adb/cco/active
    chmod 755 /data/adb/cco/logs
    chmod 755 /data/adb/cco/backup
else
    ui_print "  ! Failed to create CCO directory"
fi
ui_print ""

# Create initial log entry
LOG_FILE="/data/adb/cco/logs/module.log"
echo "[$(date '+%Y-%m-%d %H:%M:%S')] CCO Module installed" >> "$LOG_FILE"
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Device: $DEVICE" >> "$LOG_FILE"
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Android: $ANDROID (SDK $SDK)" >> "$LOG_FILE"

ui_print "=========================================="
ui_print "  Installation Complete!"
ui_print "=========================================="
ui_print ""
ui_print "Next steps:"
ui_print "1. Install the CCO Manager app"
ui_print "2. Configure your CarrierConfig profile"
ui_print "3. Deploy override and reboot"
ui_print ""
ui_print "Data directory: /data/adb/cco"
ui_print "Logs: /data/adb/cco/logs/module.log"
ui_print ""

# Set module as installed
set_perm_recursive "$MODPATH" 0 0 0755 0644
chmod 755 "$MODPATH/service.sh"
chmod 755 "$MODPATH/post-fs-data.sh"
chmod 755 "$MODPATH/uninstall.sh"

ui_print "Ready to use after reboot"
