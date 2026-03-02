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
CANDIDATE_PATHS="
/data/vendor/carrierconfig/override.xml
/data/vendor/carrierconfig/override_carrier.xml
/data/misc/carrierconfig/override.xml
/data/user_de/0/com.android.phone/files/carrierconfig_override.xml
"

is_mounted() {
    local path="$1"
    if command -v mountpoint > /dev/null 2>&1; then
        mountpoint -q "$path" 2> /dev/null
        return $?
    fi

    mount | grep -F " on $path " > /dev/null 2>&1
    return $?
}

for path in $CANDIDATE_PATHS; do
    if is_mounted "$path"; then
        if ! umount "$path" 2>/dev/null; then
            # Retry after a short delay (mount may be busy)
            sleep 1
            umount -l "$path" 2>/dev/null || log_msg "WARNING: could not unmount $path"
        fi
        log_msg "Unmounted: $path"
    fi
done

# Restore original files if backup exists
if [ -f "$CCO_DATA/backup/override_original.xml" ]; then
    ORIGINAL_PATH=""
    BACKUP_INFO="$CCO_DATA/backup/backup_info.txt"
    if [ -f "$BACKUP_INFO" ]; then
        ORIGINAL_PATH=$(grep -m1 '^Original Path:' "$BACKUP_INFO" | sed 's/^Original Path:[[:space:]]*//')
    fi

    if [ -n "$ORIGINAL_PATH" ]; then
        cp "$CCO_DATA/backup/override_original.xml" "$ORIGINAL_PATH"
        log_msg "Restored original override to $ORIGINAL_PATH"
    else
        for path in $CANDIDATE_PATHS; do
            if [ -f "$path" ]; then
                cp "$CCO_DATA/backup/override_original.xml" "$path"
                log_msg "Restored original override to $path (fallback path)"
                break
            fi
        done
    fi
fi

# Ask user if they want to keep data
# For now, we'll preserve logs but remove active configurations
if [ -d "$CCO_DATA/active" ]; then
    rm -rf "$CCO_DATA/active"
    log_msg "Removed active configurations"
fi

log_msg "Uninstall complete. Logs preserved at $CCO_DATA/logs"
log_msg "To fully remove all CCO data, manually delete: $CCO_DATA"
