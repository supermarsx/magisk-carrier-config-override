#!/system/bin/sh
###############################################################################
# CCO CarrierConfig Override - Post-FS-Data Script
# Runs after /data is mounted, before system services start
###############################################################################

MODDIR=${0%/*}
CCO_DATA="/data/adb/cco"
LOG_FILE="$CCO_DATA/logs/module.log"

# Create CCO directory structure
mkdir -p "$CCO_DATA/overrides"
mkdir -p "$CCO_DATA/active"
mkdir -p "$CCO_DATA/logs"
mkdir -p "$CCO_DATA/backup"

# Set permissions
chmod 755 "$CCO_DATA"
chmod 755 "$CCO_DATA/overrides"
chmod 755 "$CCO_DATA/active"
chmod 755 "$CCO_DATA/logs"
chmod 755 "$CCO_DATA/backup"

# Log initialization
echo "[$(date '+%Y-%m-%d %H:%M:%S')] CCO post-fs-data: Initializing directory structure" >> "$LOG_FILE"

# Try to restore SELinux contexts if possible
if [ -x "$(command -v restorecon)" ]; then
    restorecon -R "$CCO_DATA" 2> /dev/null || true
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] CCO post-fs-data: Applied SELinux contexts" >> "$LOG_FILE"
fi

echo "[$(date '+%Y-%m-%d %H:%M:%S')] CCO post-fs-data: Complete" >> "$LOG_FILE"
