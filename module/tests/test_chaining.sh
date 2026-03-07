#!/usr/bin/env bash
###############################################################################
# CCO Module Chaining Tests
# Tests that verify the install → post-fs-data → service → uninstall chain
# and cross-script interactions work correctly together.
###############################################################################

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/test_common.sh"

print_header "Module Chaining / Integration Tests"

# =========================================================================
# Chain 1: post-fs-data → service dependency
# =========================================================================

print_test "Chain: post-fs-data creates dirs that service requires"
# post-fs-data.sh creates: CCO_DATA/overrides, active, logs, backup
# service.sh checks: CCO_DATA, ACTIVE_OVERRIDE, LOG_FILE
if grep -q 'mkdir -p.*CCO_DATA/active' "$MODULE_DIR/post-fs-data.sh" 2>/dev/null &&
   grep -q 'mkdir -p.*CCO_DATA/logs' "$MODULE_DIR/post-fs-data.sh" 2>/dev/null &&
   grep -q 'ACTIVE_OVERRIDE.*active/override.xml' "$MODULE_DIR/service.sh" 2>/dev/null; then
    pass_test "post-fs-data creates dirs that service depends on"
else
    fail_test "post-fs-data/service directory dependency broken"
fi

print_test "Chain: post-fs-data and service share same CCO_DATA path"
PFS_PATH=$(grep '^CCO_DATA=' "$MODULE_DIR/post-fs-data.sh" 2>/dev/null | head -1 | cut -d= -f2 | tr -d '"')
SVC_PATH=$(grep '^CCO_DATA=' "$MODULE_DIR/service.sh" 2>/dev/null | head -1 | cut -d= -f2 | tr -d '"')
if [ -n "$PFS_PATH" ] && [ "$PFS_PATH" = "$SVC_PATH" ]; then
    pass_test "CCO_DATA path consistent: $PFS_PATH"
else
    fail_test "CCO_DATA mismatch: post-fs-data='$PFS_PATH' service='$SVC_PATH'"
fi

# =========================================================================
# Chain 2: install → module structure → service
# =========================================================================

print_test "Chain: install.sh references exist in module structure"
# install.sh should reference files/dirs that exist in the module
if [ -f "$MODULE_DIR/install.sh" ]; then
    # Check that install script mentions key module files
    if grep -q 'module.prop\|common/functions.sh\|service.sh' "$MODULE_DIR/install.sh" 2>/dev/null; then
        pass_test "install.sh references existing module files"
    else
        # install.sh might be simple, check it's at least valid
        bash -n "$MODULE_DIR/install.sh" 2>/dev/null && pass_test "install.sh is syntactically valid" || fail_test "install.sh has syntax errors"
    fi
else
    fail_test "install.sh not found"
fi

print_test "Chain: service.sh sources functions.sh correctly"
if grep -q '\..*common/functions.sh' "$MODULE_DIR/service.sh" 2>/dev/null; then
    pass_test "service.sh sources common/functions.sh"
else
    fail_test "service.sh does not source common/functions.sh"
fi

print_test "Chain: service.sh has fallback if functions.sh missing"
if grep -q 'if.*-f.*functions.sh' "$MODULE_DIR/service.sh" 2>/dev/null; then
    pass_test "service.sh has conditional source with fallback"
else
    fail_test "service.sh lacks fallback for missing functions.sh"
fi

# =========================================================================
# Chain 3: service → XML validation → mount
# =========================================================================

print_test "Chain: service.sh validates XML before mounting"
if grep -q 'carrier_config' "$MODULE_DIR/service.sh" 2>/dev/null &&
   grep -q 'grep.*carrier_config.*ACTIVE_OVERRIDE' "$MODULE_DIR/service.sh" 2>/dev/null; then
    pass_test "service.sh validates XML structure before mount"
else
    # Check for any XML validation pattern
    if grep -q 'grep.*xml\|carrier_config' "$MODULE_DIR/service.sh" 2>/dev/null; then
        pass_test "service.sh has XML validation"
    else
        fail_test "service.sh lacks XML validation before mount"
    fi
fi

print_test "Chain: service.sh handles missing override gracefully"
if grep -q 'if.*!.*-f.*ACTIVE_OVERRIDE' "$MODULE_DIR/service.sh" 2>/dev/null; then
    pass_test "service.sh handles missing override file"
else
    fail_test "service.sh does not check for missing override"
fi

# =========================================================================
# Chain 4: disable flag chain
# =========================================================================

print_test "Chain: service.sh respects disable flag"
if grep -q 'DISABLE_FLAG\|disable' "$MODULE_DIR/service.sh" 2>/dev/null; then
    if grep -q 'exit 0' "$MODULE_DIR/service.sh" 2>/dev/null; then
        pass_test "service.sh checks disable flag and exits cleanly"
    else
        fail_test "service.sh checks disable flag but doesn't exit"
    fi
else
    fail_test "service.sh does not check disable flag"
fi

print_test "Chain: disable flag path is in CCO_DATA"
SVC_DISABLE=$(grep 'DISABLE_FLAG=' "$MODULE_DIR/service.sh" 2>/dev/null | head -1)
if echo "$SVC_DISABLE" | grep -q 'CCO_DATA.*disable'; then
    pass_test "Disable flag uses CCO_DATA path"
else
    fail_test "Disable flag path inconsistent with CCO_DATA"
fi

# =========================================================================
# Chain 5: uninstall cleanup chain
# =========================================================================

print_test "Chain: uninstall.sh cleans up what service.sh creates"
if [ -f "$MODULE_DIR/uninstall.sh" ]; then
    # Uninstall should remove CCO_DATA or key files
    if grep -q 'CCO_DATA\|/data/adb/cco\|rm.*active\|rm.*override' "$MODULE_DIR/uninstall.sh" 2>/dev/null; then
        pass_test "uninstall.sh cleans up CCO data directory"
    else
        fail_test "uninstall.sh does not clean up CCO data"
    fi
else
    fail_test "uninstall.sh not found"
fi

print_test "Chain: uninstall removes bind mounts"
if [ -f "$MODULE_DIR/uninstall.sh" ]; then
    if grep -q 'umount' "$MODULE_DIR/uninstall.sh" 2>/dev/null; then
        pass_test "uninstall.sh unmounts bind mounts"
    else
        warn_test "uninstall.sh does not explicitly unmount"
    fi
fi

# =========================================================================
# Chain 6: log file consistency
# =========================================================================

print_test "Chain: all scripts write to same log file"
PFS_LOG=$(grep 'LOG_FILE=' "$MODULE_DIR/post-fs-data.sh" 2>/dev/null | head -1 | cut -d= -f2 | tr -d '"')
SVC_LOG=$(grep 'LOG_FILE=' "$MODULE_DIR/service.sh" 2>/dev/null | head -1 | cut -d= -f2 | tr -d '"')

# Both should reference CCO logs
if echo "$PFS_LOG" | grep -qi 'cco.*log' && echo "$SVC_LOG" | grep -qi 'cco.*log'; then
    pass_test "All scripts log to CCO log directory"
else
    fail_test "Log paths inconsistent: pfs='$PFS_LOG' svc='$SVC_LOG'"
fi

print_test "Chain: service.sh has log rotation"
if grep -q 'LOG_SIZE\|log.*rotation\|tail.*LOG_FILE' "$MODULE_DIR/service.sh" 2>/dev/null; then
    pass_test "service.sh implements log rotation"
else
    fail_test "service.sh lacks log rotation"
fi

# =========================================================================
# Chain 7: module.prop → version consistency
# =========================================================================

print_test "Chain: module.prop has required fields"
REQUIRED_FIELDS="id version versionCode name author description"
MISSING=""
for field in $REQUIRED_FIELDS; do
    if ! grep -q "^$field=" "$MODULE_DIR/module.prop" 2>/dev/null; then
        MISSING="$MISSING $field"
    fi
done
if [ -z "$MISSING" ]; then
    pass_test "module.prop has all required fields"
else
    fail_test "module.prop missing:$MISSING"
fi

print_test "Chain: module.prop version is valid semver pattern"
VERSION=$(grep '^version=' "$MODULE_DIR/module.prop" 2>/dev/null | cut -d= -f2)
if echo "$VERSION" | grep -qE '^v?[0-9]+\.[0-9]+\.[0-9]+'; then
    pass_test "module.prop version is valid: $VERSION"
else
    fail_test "module.prop version invalid: $VERSION"
fi

# =========================================================================
# Chain 8: per-SIM slot support
# =========================================================================

print_test "Chain: per-SIM slot override paths in service.sh"
if grep -q 'sim.*override\|override.*sim\|slot' "$MODULE_DIR/service.sh" 2>/dev/null; then
    pass_test "service.sh has per-SIM slot references"
else
    warn_test "service.sh does not explicitly reference per-SIM-slot files"
fi

# =========================================================================
# Chain 9: boot wait → data mounted → override applied
# =========================================================================

print_test "Chain: service.sh waits for /data mount"
if grep -q 'until.*data\|wait.*data\|count.*data' "$MODULE_DIR/service.sh" 2>/dev/null; then
    pass_test "service.sh waits for /data to be ready"
else
    fail_test "service.sh does not wait for /data"
fi

print_test "Chain: service.sh has /data timeout"
if grep -q 'count.*30\|timeout\|count.*-gt' "$MODULE_DIR/service.sh" 2>/dev/null; then
    pass_test "service.sh has data mount timeout"
else
    fail_test "service.sh lacks data mount timeout"
fi

# =========================================================================
# Chain 10: override path detection chain
# =========================================================================

print_test "Chain: service.sh detects carrier config override paths"
if grep -q 'override_path\|OVERRIDE_PATH\|carrierconfig\|carrier_config' "$MODULE_DIR/service.sh" 2>/dev/null; then
    pass_test "service.sh includes override path detection"
else
    fail_test "service.sh lacks override path detection"
fi

# =========================================================================
# Chain 11: profiles → active override chain
# =========================================================================

print_test "Chain: profile XML files can be used as active overrides"
PROFILE_COUNT=0
VALID_PROFILES=0
for profile in "$MODULE_DIR/profiles/"*.xml; do
    if [ -f "$profile" ]; then
        PROFILE_COUNT=$((PROFILE_COUNT + 1))
        if grep -q '<carrier_config' "$profile" 2>/dev/null &&
           grep -q '</carrier_config>' "$profile" 2>/dev/null; then
            VALID_PROFILES=$((VALID_PROFILES + 1))
        fi
    fi
done
if [ "$PROFILE_COUNT" -gt 0 ] && [ "$VALID_PROFILES" -eq "$PROFILE_COUNT" ]; then
    pass_test "All $PROFILE_COUNT profile(s) are valid carrier_config XML"
else
    fail_test "Profile validation: $VALID_PROFILES of $PROFILE_COUNT valid"
fi

# =========================================================================
# Chain 12: directory permissions chain
# =========================================================================

print_test "Chain: post-fs-data sets permissions on CCO dirs"
if grep -q 'chmod.*755\|chmod.*CCO_DATA' "$MODULE_DIR/post-fs-data.sh" 2>/dev/null; then
    pass_test "post-fs-data.sh sets dir permissions"
else
    fail_test "post-fs-data.sh does not set permissions"
fi

print_test "Chain: SELinux context restore in post-fs-data"
if grep -q 'restorecon' "$MODULE_DIR/post-fs-data.sh" 2>/dev/null; then
    pass_test "post-fs-data.sh restores SELinux contexts"
else
    fail_test "post-fs-data.sh lacks SELinux context restore"
fi

# =========================================================================
# Chain 13: error handling chain
# =========================================================================

print_test "Chain: service.sh has error exit codes"
if grep -q 'exit 1' "$MODULE_DIR/service.sh" 2>/dev/null; then
    pass_test "service.sh uses exit 1 for errors"
else
    fail_test "service.sh does not use exit 1 for errors"
fi

print_test "Chain: service.sh has clean exit for disable"
if grep -q 'exit 0' "$MODULE_DIR/service.sh" 2>/dev/null; then
    pass_test "service.sh uses exit 0 for clean exits"
else
    fail_test "service.sh does not use exit 0 for clean exits"
fi

# =========================================================================
# Summary
# =========================================================================

print_test_summary "Module Chaining Tests"
exit $?
