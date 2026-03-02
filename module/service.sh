#!/system/bin/sh
###############################################################################
# CCO CarrierConfig Override - Service Script
# Runs after boot is complete, applies CarrierConfig overrides
###############################################################################


MODDIR=${0%/*}
CCO_DATA="/data/adb/cco"
ACTIVE_OVERRIDE="$CCO_DATA/active/override.xml"
DISABLE_FLAG="$CCO_DATA/disable"
LOG_FILE="$CCO_DATA/logs/module.log"

# Source utility functions if available
if [ -f "$MODDIR/common/functions.sh" ]; then
    . "$MODDIR/common/functions.sh"
else
    # Fallback logging function
    log_info() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] INFO: $1" >> "$LOG_FILE"; }
    log_warn() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] WARN: $1" >> "$LOG_FILE"; }
    log_error() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1" >> "$LOG_FILE"; }
fi

# ── Log rotation (keep last 50 KB) ──────────────────────────────────────────
if [ -f "$LOG_FILE" ]; then
    LOG_SIZE=$(stat -c%s "$LOG_FILE" 2>/dev/null || stat -f%z "$LOG_FILE" 2>/dev/null || echo 0)
    if [ "$LOG_SIZE" -gt 51200 ]; then
        tail -c 25600 "$LOG_FILE" > "${LOG_FILE}.tmp" 2>/dev/null \
            && mv "${LOG_FILE}.tmp" "$LOG_FILE" \
            || rm -f "${LOG_FILE}.tmp"
    fi
fi

log_info "=========================================="
log_info "CCO Service Script Starting"
log_info "=========================================="

# Log device information
log_info "Device: $(getprop ro.product.model)"
log_info "Android: $(getprop ro.build.version.release) (SDK $(getprop ro.build.version.sdk))"
log_info "Build: $(getprop ro.build.fingerprint)"

# Check if module is disabled
if [ -f "$DISABLE_FLAG" ]; then
    log_warn "Module disabled by user (disable flag present)"
    log_info "Remove $DISABLE_FLAG to re-enable"
    log_info "=========================================="
    exit 0
fi

# Wait for data to be fully mounted
log_info "Waiting for /data to be ready..."
count=0
until [ -d "/data/adb" ] || [ $count -gt 30 ]; do
    sleep 1
    count=$((count + 1))
done

if [ ! -d "/data/adb" ]; then
    log_error "Timeout waiting for /data/adb"
    exit 1
fi
log_info "/data is ready"

# Ensure CCO directory structure exists
if [ ! -d "$CCO_DATA" ]; then
    log_warn "CCO data directory missing, creating..."
    mkdir -p "$CCO_DATA/overrides"
    mkdir -p "$CCO_DATA/active"
    mkdir -p "$CCO_DATA/logs"
    mkdir -p "$CCO_DATA/backup"
    chmod -R 755 "$CCO_DATA"
fi

# Check if override file exists
if [ ! -f "$ACTIVE_OVERRIDE" ]; then
    log_warn "No active override file found at $ACTIVE_OVERRIDE"
    log_info "Deploy a configuration using CCO Manager app"
    log_info "Or manually copy profile to $ACTIVE_OVERRIDE"
    log_info "Sample profiles available in: $MODDIR/profiles/"
    log_info "=========================================="
    exit 0
fi

log_info "Active override file found"
log_info "File size: $(stat -c%s "$ACTIVE_OVERRIDE" 2>/dev/null || stat -f%z "$ACTIVE_OVERRIDE" 2>/dev/null || echo "unknown") bytes"

# ── Basic XML sanity check ──────────────────────────────────────────────────
if ! grep -q '<carrier_config' "$ACTIVE_OVERRIDE" 2>/dev/null; then
    log_error "Override file does not contain <carrier_config> element"
    log_error "Refusing to mount an invalid override. Fix the file and reboot."
    exit 1
fi
if ! grep -q '</carrier_config>' "$ACTIVE_OVERRIDE" 2>/dev/null; then
    log_warn "Override file may be truncated (missing closing tag)"
fi

# CarrierConfig override path detection
log_info "Detecting CarrierConfig override path..."

# Use detection function if available, otherwise use candidates directly
if command -v detect_override_path > /dev/null 2>&1; then
    TARGET_PATH=$(detect_override_path)
    log_info "Path detected via function: $TARGET_PATH"
else
    # Fallback: manual detection
    CANDIDATE_PATHS="
/data/vendor/carrierconfig/override.xml
/data/vendor/carrierconfig/override_carrier.xml
/data/misc/carrierconfig/override.xml
/data/user_de/0/com.android.phone/files/carrierconfig_override.xml
"

    TARGET_PATH=""
    for path in $CANDIDATE_PATHS; do
        dir=$(dirname "$path")
        if [ -d "$dir" ]; then
            TARGET_PATH="$path"
            log_info "Found existing directory: $dir"
            break
        fi
    done

    # Fallback to most common path
    if [ -z "$TARGET_PATH" ]; then
        TARGET_PATH="/data/vendor/carrierconfig/override.xml"
        log_info "Using default path: $TARGET_PATH"
    fi
fi

log_info "Selected target path: $TARGET_PATH"

# Backup original if it exists and we haven't already
BACKUP_FILE="$CCO_DATA/backup/override_original.xml"
BACKUP_INFO="$CCO_DATA/backup/backup_info.txt"

if [ -f "$TARGET_PATH" ] && [ ! -f "$BACKUP_FILE" ]; then
    log_info "Backing up original override file..."
    cp "$TARGET_PATH" "$BACKUP_FILE"

    # Save backup metadata
    echo "Backup Date: $(date '+%Y-%m-%d %H:%M:%S')" > "$BACKUP_INFO"
    echo "Original Path: $TARGET_PATH" >> "$BACKUP_INFO"
    echo "Device: $(getprop ro.product.model)" >> "$BACKUP_INFO"
    echo "Build: $(getprop ro.build.fingerprint)" >> "$BACKUP_INFO"

    log_info "Original backed up to $BACKUP_FILE"
else
    if [ -f "$BACKUP_FILE" ]; then
        log_info "Original backup already exists, skipping"
    fi
fi

# Create target directory if needed
TARGET_DIR=$(dirname "$TARGET_PATH")
if [ ! -d "$TARGET_DIR" ]; then
    log_info "Creating target directory: $TARGET_DIR"
    mkdir -p "$TARGET_DIR"
    chmod 755 "$TARGET_DIR"
fi

# Create mount point if it doesn't exist
if [ ! -f "$TARGET_PATH" ]; then
    log_info "Creating mount point file: $TARGET_PATH"
    touch "$TARGET_PATH"
    chmod 644 "$TARGET_PATH"
fi

# Set SELinux context on target
log_info "Setting SELinux context..."
if [ -x "$(command -v chcon)" ]; then
    if chcon u:object_r:radio_data_file:s0 "$TARGET_PATH" 2> /dev/null; then
        log_info "Applied radio_data_file context"
    elif chcon u:object_r:vendor_data_file:s0 "$TARGET_PATH" 2> /dev/null; then
        log_info "Applied vendor_data_file context"
    else
        log_warn "Could not set SELinux context (may not be required)"
    fi
else
    log_info "chcon not available, skipping SELinux context"
fi

# Perform bind mount
log_info "=========================================="
log_info "Performing bind mount..."
log_info "Source: $ACTIVE_OVERRIDE"
log_info "Target: $TARGET_PATH"
log_info "=========================================="

# Use safe_bind_mount if available, otherwise manual mount
if command -v safe_bind_mount > /dev/null 2>&1; then
    safe_bind_mount "$ACTIVE_OVERRIDE" "$TARGET_PATH"
    MOUNT_RESULT=$?
else
    mount --bind "$ACTIVE_OVERRIDE" "$TARGET_PATH"
    MOUNT_RESULT=$?
fi

if [ $MOUNT_RESULT -eq 0 ]; then
    log_info "✓ Bind mount SUCCESSFUL"

    # Set proper permissions on mounted file
    chmod 644 "$TARGET_PATH"

    # Verify mount
    if mountpoint -q "$TARGET_PATH" 2> /dev/null; then
        log_info "✓ Mount point verified"
    else
        log_warn "Mount point verification inconclusive"
    fi

    # Optionally trigger carrier config refresh per SIM slot
    log_info "Triggering CarrierConfig refresh..."
    if [ -x "$(command -v am)" ]; then
        SLOT_COUNT=$(getprop persist.radio.multisim.config 2>/dev/null)
        case "$SLOT_COUNT" in
            dsds|dsda|tsts) MAX_SLOTS=2 ;;
            *)              MAX_SLOTS=1 ;;
        esac
        SLOT=0
        while [ $SLOT -lt $MAX_SLOTS ]; do
            am broadcast -a android.telephony.action.CARRIER_CONFIG_CHANGED \
                --ei android.telephony.extra.SLOT_INDEX $SLOT 2>/dev/null \
                && log_info "✓ Broadcast sent for slot $SLOT" \
                || log_warn "Broadcast may have failed for slot $SLOT"
            SLOT=$((SLOT + 1))
        done
    else
        log_info "Activity Manager not available, skipping broadcast"
    fi

    log_info "=========================================="
    log_info "CCO CarrierConfig Override is now ACTIVE"
    log_info "Override file: $(basename "$ACTIVE_OVERRIDE")"
    log_info "Mounted at: $TARGET_PATH"
    log_info "=========================================="

else
    log_error "✗ Bind mount FAILED (error code: $MOUNT_RESULT)"
    log_error "Override may not be active"
    log_error "Check permissions and SELinux status"
    log_error "Manual reboot may be required"
    log_info "=========================================="
    exit 1
fi

log_info "Service script complete"
exit 0
