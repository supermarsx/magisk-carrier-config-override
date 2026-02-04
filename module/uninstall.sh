#!/system/bin/sh
###############################################################################
# CCO CarrierConfig Override - Uninstall Script
# Cleans up CCO data when module is removed
###############################################################################

CCO_DATA="/data/adb/cco"
LOG_FILE="$CCO_DATA/logs/uninstall.log"

mkdir -p "$CCO_DATA/logs"

log_msg() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] CCO uninstall: $1" >> "$LOG_FILE"
}

log_msg "Uninstall script starting..."

# Unmount any active bind mounts
CANDIDATE_PATHS=(
    "/data/vendor/carrierconfig/override.xml"
    "/data/vendor/carrierconfig/override_carrier.xml"
    "/data/misc/carrierconfig/override.xml"
    "/data/user_de/0/com.android.phone/files/carrierconfig_override.xml"
)

for path in "${CANDIDATE_PATHS[@]}"; do
    if mountpoint -q "$path" 2> /dev/null; then
        umount "$path"
        log_msg "Unmounted: $path"
    fi
done

# Restore original files if backup exists
if [ -f "$CCO_DATA/backup/override_original.xml" ]; then
    for path in "${CANDIDATE_PATHS[@]}"; do
        if [ -f "$path" ]; then
            cp "$CCO_DATA/backup/override_original.xml" "$path"
            log_msg "Restored original override to $path"
        fi
    done
fi

# Ask user if they want to keep data
# For now, we'll preserve logs but remove active configurations
if [ -d "$CCO_DATA/active" ]; then
    rm -rf "$CCO_DATA/active"
    log_msg "Removed active configurations"
fi

log_msg "Uninstall complete. Logs preserved at $CCO_DATA/logs"
log_msg "To fully remove all CCO data, manually delete: $CCO_DATA"
