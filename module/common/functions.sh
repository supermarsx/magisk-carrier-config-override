#!/system/bin/sh
###############################################################################
# CCO CarrierConfig Override - Utility Functions
# Common functions used by module scripts
###############################################################################

# Colors for logging
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function with timestamp
log() {
    local level="$1"
    local msg="$2"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "[${timestamp}] [${level}] $msg" >> "${LOG_FILE:-/data/adb/cco/logs/module.log}"
}

log_info() {
    log "INFO" "$1"
}

log_warn() {
    log "WARN" "$1"
}

log_error() {
    log "ERROR" "$1"
}

# Detect CarrierConfig override path for this device
detect_override_path() {
    local candidate_paths="
/data/vendor/carrierconfig/override.xml
/data/vendor/carrierconfig/override_carrier.xml
/data/misc/carrierconfig/override.xml
/data/user_de/0/com.android.phone/files/carrierconfig_override.xml
"

    # First, check if any path already has a file (indicates system uses it)
    for path in $candidate_paths; do
        if [ -f "$path" ]; then
            echo "$path"
            return 0
        fi
    done

    # Next, check if directory exists
    for path in $candidate_paths; do
        local dir=$(dirname "$path")
        if [ -d "$dir" ]; then
            echo "$path"
            return 0
        fi
    done

    # Default fallback
    echo "/data/vendor/carrierconfig/override.xml"
    return 0
}

# Wait for path to be ready with timeout
wait_for_path() {
    local path="$1"
    local timeout="${2:-30}"
    local elapsed=0

    while [ $elapsed -lt $timeout ]; do
        if [ -e "$path" ]; then
            return 0
        fi
        sleep 1
        elapsed=$((elapsed + 1))
    done

    return 1
}

# Create directory with proper SELinux context
create_dir_with_context() {
    local dir="$1"
    mkdir -p "$dir"
    chmod 755 "$dir"

    # Try to set SELinux context
    if [ -x "$(command -v chcon)" ]; then
        chcon -R u:object_r:vendor_data_file:s0 "$dir" 2> /dev/null \
            || chcon -R u:object_r:radio_data_file:s0 "$dir" 2> /dev/null \
            || restorecon -R "$dir" 2> /dev/null || true
    fi
}

# Safely bind mount with verification
safe_bind_mount() {
    local source="$1"
    local target="$2"

    # Validate source exists
    if [ ! -f "$source" ]; then
        log_error "Source file does not exist: $source"
        return 1
    fi

    # Create target directory if needed
    local target_dir=$(dirname "$target")
    if [ ! -d "$target_dir" ]; then
        create_dir_with_context "$target_dir"
    fi

    # Create empty target file if it doesn't exist
    if [ ! -f "$target" ]; then
        touch "$target"
        chmod 644 "$target"
    fi

    # Set SELinux context on target before mounting
    if [ -x "$(command -v chcon)" ]; then
        chcon u:object_r:radio_data_file:s0 "$target" 2> /dev/null \
            || chcon u:object_r:vendor_data_file:s0 "$target" 2> /dev/null || true
    fi

    # Perform bind mount
    mount --bind "$source" "$target"
    local result=$?

    if [ $result -eq 0 ]; then
        log_info "Bind mount successful: $source -> $target"
        chmod 644 "$target"
        return 0
    else
        log_error "Bind mount failed with code $result"
        return 1
    fi
}

# Check if module is disabled by user
is_module_disabled() {
    [ -f "/data/adb/cco/disable" ]
}

# Get device information
get_device_info() {
    local manufacturer=$(getprop ro.product.manufacturer)
    local model=$(getprop ro.product.model)
    local android=$(getprop ro.build.version.release)
    local sdk=$(getprop ro.build.version.sdk)
    local build=$(getprop ro.build.fingerprint)

    echo "Manufacturer: $manufacturer"
    echo "Model: $model"
    echo "Android: $android (SDK $sdk)"
    echo "Build: $build"
}
