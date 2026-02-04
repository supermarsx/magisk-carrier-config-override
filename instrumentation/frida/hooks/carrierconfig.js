/**
 * CarrierConfig Hooks
 * 
 * Intercepts and modifies CarrierConfig at runtime
 * Targets: android.telephony.CarrierConfigManager
 */

const hooks = {
    installed: [],
    configCache: {},
    modificationsApplied: 0
};

// CarrierConfig keys we want to force
const FORCE_KEYS = {
    'carrier_wfc_ims_available_bool': true,
    'editable_wfc_mode_bool': true,
    'carrier_default_wfc_ims_enabled_bool': true,
    'carrier_default_wfc_ims_roaming_enabled_bool': true,
    'carrier_promote_wfc_on_call_fail_bool': true,
    'use_wfc_home_network_mode_in_roaming_network_bool': false,
    'wfc_carrier_name_override_by_pnn_bool': false
};

function install(config, logEvent, log) {
    log("info", "Installing CarrierConfig hooks...");
    
    // Hook 1: CarrierConfigManager.getConfigForSubId
    hookCarrierConfigGetter(config, logEvent, log);
    
    // Hook 2: CarrierConfigManager.getConfig
    hookCarrierConfigGetterNoSub(config, logEvent, log);
    
    // Hook 3: PersistableBundle modifications
    hookPersistableBundle(config, logEvent, log);
    
    // Hook 4: Config change notifications
    hookConfigChangeNotifications(config, logEvent, log);
    
    log("info", `CarrierConfig hooks installed: ${hooks.installed.length}`);
    return hooks;
}

function hookCarrierConfigGetter(config, logEvent, log) {
    try {
        const CarrierConfigManager = Java.use("android.telephony.CarrierConfigManager");
        
        if (CarrierConfigManager.getConfigForSubId) {
            CarrierConfigManager.getConfigForSubId.implementation = function(subId) {
                const original = this.getConfigForSubId(subId);
                
                if (original != null && config.features.autoBypass) {
                    // Modify the config bundle
                    modifyConfigBundle(original, subId, logEvent, log);
                    hooks.modificationsApplied++;
                }
                
                logEvent("config_query", "CarrierConfigManager.getConfigForSubId", [subId], {
                    subId,
                    modified: original != null,
                    keysModified: Object.keys(FORCE_KEYS).length
                });
                
                return original;
            };
            hooks.installed.push("CarrierConfigManager.getConfigForSubId");
        }
        
    } catch (e) {
        log("error", "Failed to hook CarrierConfigManager", { error: e.message });
    }
}

function hookCarrierConfigGetterNoSub(config, logEvent, log) {
    try {
        const CarrierConfigManager = Java.use("android.telephony.CarrierConfigManager");
        
        if (CarrierConfigManager.getConfig) {
            CarrierConfigManager.getConfig.implementation = function() {
                const original = this.getConfig();
                
                if (original != null && config.features.autoBypass) {
                    modifyConfigBundle(original, -1, logEvent, log);
                    hooks.modificationsApplied++;
                }
                
                logEvent("config_query", "CarrierConfigManager.getConfig", [], {
                    modified: original != null
                });
                
                return original;
            };
            hooks.installed.push("CarrierConfigManager.getConfig");
        }
        
    } catch (e) {
        log("debug", "CarrierConfigManager.getConfig not available");
    }
}

function hookPersistableBundle(config, logEvent, log) {
    try {
        const PersistableBundle = Java.use("android.os.PersistableBundle");
        
        // Hook getBoolean to intercept reads
        const originalGetBoolean = PersistableBundle.getBoolean.overload('java.lang.String', 'boolean');
        PersistableBundle.getBoolean.overload('java.lang.String', 'boolean').implementation = function(key, defaultValue) {
            let value = originalGetBoolean.call(this, key, defaultValue);
            
            // Force specific carrier config keys
            if (FORCE_KEYS.hasOwnProperty(key)) {
                const forcedValue = FORCE_KEYS[key];
                if (value !== forcedValue) {
                    logEvent("bundle_intercept", "PersistableBundle.getBoolean", [key], {
                        key,
                        original: value,
                        forced: forcedValue
                    });
                    value = forcedValue;
                }
            }
            
            return value;
        };
        hooks.installed.push("PersistableBundle.getBoolean");
        
    } catch (e) {
        log("warn", "PersistableBundle hook failed", { error: e.message });
    }
}

function hookConfigChangeNotifications(config, logEvent, log) {
    try {
        const CarrierConfigManager = Java.use("android.telephony.CarrierConfigManager");
        
        if (CarrierConfigManager.notifyConfigChangedForSubId) {
            CarrierConfigManager.notifyConfigChangedForSubId.implementation = function(subId) {
                logEvent("config_change", "CarrierConfigManager.notifyConfigChangedForSubId", [subId], {
                    subId
                });
                
                this.notifyConfigChangedForSubId(subId);
            };
            hooks.installed.push("CarrierConfigManager.notifyConfigChangedForSubId");
        }
        
    } catch (e) {
        log("debug", "Config change notification hook not available");
    }
}

/**
 * Modify a PersistableBundle with forced values
 */
function modifyConfigBundle(bundle, subId, logEvent, log) {
    if (bundle == null) return;
    
    try {
        const modifications = [];
        
        for (const [key, value] of Object.entries(FORCE_KEYS)) {
            try {
                if (typeof value === 'boolean') {
                    const original = bundle.getBoolean(key, !value);
                    if (original !== value) {
                        bundle.putBoolean(key, value);
                        modifications.push({key, original, forced: value});
                    }
                } else if (typeof value === 'number') {
                    bundle.putInt(key, value);
                    modifications.push({key, forced: value});
                } else if (typeof value === 'string') {
                    bundle.putString(key, value);
                    modifications.push({key, forced: value});
                }
            } catch (e) {
                // Key might not exist, ignore
            }
        }
        
        if (modifications.length > 0) {
            logEvent("bundle_modified", "modifyConfigBundle", [subId], {
                subId,
                modifications
            });
        }
        
    } catch (e) {
        log("error", "Failed to modify bundle", { error: e.message });
    }
}

/**
 * Dump current carrier config
 */
function dumpConfig() {
    const result = {
        success: false,
        config: {},
        error: null
    };
    
    try {
        Java.perform(() => {
            const CarrierConfigManager = Java.use("android.telephony.CarrierConfigManager");
            const context = Java.use("android.app.ActivityThread")
                .currentApplication()
                .getApplicationContext();
            
            const ccm = context.getSystemService("carrier_config");
            const bundle = ccm.getConfig();
            
            if (bundle != null) {
                // Extract all keys
                const keys = Object.keys(FORCE_KEYS);
                for (const key of keys) {
                    try {
                        const value = bundle.get(key);
                        result.config[key] = value ? value.toString() : null;
                    } catch (e) {
                        result.config[key] = `error: ${e.message}`;
                    }
                }
                result.success = true;
            }
        });
    } catch (e) {
        result.error = e.message;
    }
    
    return result;
}

/**
 * Inject a custom config
 */
function injectConfig(customConfig) {
    try {
        Java.perform(() => {
            const CarrierConfigManager = Java.use("android.telephony.CarrierConfigManager");
            const context = Java.use("android.app.ActivityThread")
                .currentApplication()
                .getApplicationContext();
            
            const ccm = context.getSystemService("carrier_config");
            const bundle = ccm.getConfig();
            
            if (bundle != null) {
                for (const [key, value] of Object.entries(customConfig)) {
                    if (typeof value === 'boolean') {
                        bundle.putBoolean(key, value);
                    } else if (typeof value === 'number') {
                        bundle.putInt(key, value);
                    } else if (typeof value === 'string') {
                        bundle.putString(key, value);
                    }
                }
                
                // Trigger refresh
                ccm.notifyConfigChangedForSubId(-1);
            }
        });
        return { success: true };
    } catch (e) {
        return { success: false, error: e.message };
    }
}

module.exports = {
    install,
    dumpConfig,
    injectConfig,
    hooks,
    FORCE_KEYS
};
