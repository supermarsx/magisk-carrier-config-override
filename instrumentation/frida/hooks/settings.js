/**
 * Settings Hooks
 * 
 * Intercepts Android Settings provider for WFC/VoLTE settings
 * Targets: android.provider.Settings, Samsung Settings providers
 */

const hooks = {
    installed: [],
    settingsCache: {},
    modifications: 0
};

// Settings keys we want to track/modify
const SETTINGS_KEYS = {
    // Global WFC settings
    'wfc_ims_enabled': 1,
    'wfc_ims_mode': 2,  // 0=WiFi only, 1=Cellular preferred, 2=WiFi preferred
    'wfc_ims_roaming_enabled': 1,
    'wfc_ims_roaming_mode': 2,
    
    // VoLTE settings
    'volte_vt_enabled': 1,
    'lte_service_forced': 1,
    
    // Samsung specific
    'vowifi_settings': 1,
    'vowifi_roaming_enabled': 1
};

function install(config, logEvent, log) {
    log("info", "Installing Settings hooks...");
    
    // Hook 1: Settings.Global
    hookSettingsGlobal(config, logEvent, log);
    
    // Hook 2: Settings.System
    hookSettingsSystem(config, logEvent, log);
    
    // Hook 3: Settings.Secure
    hookSettingsSecure(config, logEvent, log);
    
    // Hook 4: ContentResolver queries
    hookContentResolver(config, logEvent, log);
    
    log("info", `Settings hooks installed: ${hooks.installed.length}`);
    return hooks;
}

function hookSettingsGlobal(config, logEvent, log) {
    try {
        const SettingsGlobal = Java.use("android.provider.Settings$Global");
        
        // getInt with default value
        if (SettingsGlobal.getInt) {
            const originalGetInt = SettingsGlobal.getInt.overload('android.content.ContentResolver', 'java.lang.String', 'int');
            originalGetInt.implementation = function(resolver, name, def) {
                let value = originalGetInt.call(this, resolver, name, def);
                
                // Force specific settings
                if (SETTINGS_KEYS.hasOwnProperty(name)) {
                    const forced = SETTINGS_KEYS[name];
                    if (value !== forced && config.features.autoBypass) {
                        logEvent("settings_intercept", "Settings.Global.getInt", [name], {
                            key: name,
                            original: value,
                            forced: forced
                        });
                        value = forced;
                        hooks.modifications++;
                    }
                }
                
                hooks.settingsCache[`global:${name}`] = value;
                return value;
            };
            hooks.installed.push("Settings.Global.getInt");
        }
        
        // getString
        if (SettingsGlobal.getString) {
            const originalGetString = SettingsGlobal.getString.overload('android.content.ContentResolver', 'java.lang.String');
            originalGetString.implementation = function(resolver, name) {
                const value = originalGetString.call(this, resolver, name);
                
                logEvent("settings_query", "Settings.Global.getString", [name], {
                    key: name,
                    value: value ? value.substring(0, 100) : null
                });
                
                hooks.settingsCache[`global:${name}`] = value;
                return value;
            };
            hooks.installed.push("Settings.Global.getString");
        }
        
        // putInt
        if (SettingsGlobal.putInt) {
            const originalPutInt = SettingsGlobal.putInt.overload('android.content.ContentResolver', 'java.lang.String', 'int');
            originalPutInt.implementation = function(resolver, name, value) {
                logEvent("settings_write", "Settings.Global.putInt", [name, value], {
                    key: name,
                    value: value
                });
                
                return originalPutInt.call(this, resolver, name, value);
            };
            hooks.installed.push("Settings.Global.putInt");
        }
        
    } catch (e) {
        log("error", "Failed to hook Settings.Global", { error: e.message });
    }
}

function hookSettingsSystem(config, logEvent, log) {
    try {
        const SettingsSystem = Java.use("android.provider.Settings$System");
        
        // getInt
        if (SettingsSystem.getInt) {
            const originalGetInt = SettingsSystem.getInt.overload('android.content.ContentResolver', 'java.lang.String', 'int');
            originalGetInt.implementation = function(resolver, name, def) {
                let value = originalGetInt.call(this, resolver, name, def);
                
                if (SETTINGS_KEYS.hasOwnProperty(name)) {
                    const forced = SETTINGS_KEYS[name];
                    if (value !== forced && config.features.autoBypass) {
                        logEvent("settings_intercept", "Settings.System.getInt", [name], {
                            key: name,
                            original: value,
                            forced: forced
                        });
                        value = forced;
                        hooks.modifications++;
                    }
                }
                
                hooks.settingsCache[`system:${name}`] = value;
                return value;
            };
            hooks.installed.push("Settings.System.getInt");
        }
        
    } catch (e) {
        log("warn", "Failed to hook Settings.System", { error: e.message });
    }
}

function hookSettingsSecure(config, logEvent, log) {
    try {
        const SettingsSecure = Java.use("android.provider.Settings$Secure");
        
        // getInt
        if (SettingsSecure.getInt) {
            const originalGetInt = SettingsSecure.getInt.overload('android.content.ContentResolver', 'java.lang.String', 'int');
            originalGetInt.implementation = function(resolver, name, def) {
                let value = originalGetInt.call(this, resolver, name, def);
                
                if (SETTINGS_KEYS.hasOwnProperty(name)) {
                    const forced = SETTINGS_KEYS[name];
                    if (value !== forced && config.features.autoBypass) {
                        logEvent("settings_intercept", "Settings.Secure.getInt", [name], {
                            key: name,
                            original: value,
                            forced: forced
                        });
                        value = forced;
                        hooks.modifications++;
                    }
                }
                
                hooks.settingsCache[`secure:${name}`] = value;
                return value;
            };
            hooks.installed.push("Settings.Secure.getInt");
        }
        
        // getString
        if (SettingsSecure.getString) {
            const originalGetString = SettingsSecure.getString.overload('android.content.ContentResolver', 'java.lang.String');
            originalGetString.implementation = function(resolver, name) {
                const value = originalGetString.call(this, resolver, name);
                
                // Track WFC-related settings
                if (name && (name.indexOf('wfc') >= 0 || name.indexOf('vowifi') >= 0 || name.indexOf('volte') >= 0)) {
                    logEvent("settings_query", "Settings.Secure.getString", [name], {
                        key: name,
                        value: value ? value.substring(0, 100) : null
                    });
                }
                
                hooks.settingsCache[`secure:${name}`] = value;
                return value;
            };
            hooks.installed.push("Settings.Secure.getString");
        }
        
    } catch (e) {
        log("warn", "Failed to hook Settings.Secure", { error: e.message });
    }
}

function hookContentResolver(config, logEvent, log) {
    try {
        const ContentResolver = Java.use("android.content.ContentResolver");
        
        // Query method (used by Settings internally)
        if (ContentResolver.query) {
            const originalQuery = ContentResolver.query.overload(
                'android.net.Uri',
                '[Ljava.lang.String;',
                'java.lang.String',
                '[Ljava.lang.String;',
                'java.lang.String'
            );
            
            originalQuery.implementation = function(uri, projection, selection, selectionArgs, sortOrder) {
                const uriStr = uri.toString();
                
                // Track Settings provider queries
                if (uriStr.indexOf('settings') >= 0) {
                    logEvent("content_query", "ContentResolver.query", [uriStr], {
                        uri: uriStr,
                        selection: selection
                    });
                }
                
                return originalQuery.call(this, uri, projection, selection, selectionArgs, sortOrder);
            };
            hooks.installed.push("ContentResolver.query");
        }
        
    } catch (e) {
        log("debug", "ContentResolver hook not critical");
    }
}

/**
 * Get current settings cache
 */
function getSettings() {
    return {
        cache: hooks.settingsCache,
        modifications: hooks.modifications,
        tracked: Object.keys(SETTINGS_KEYS)
    };
}

/**
 * Force a specific setting
 */
function forceSetting(namespace, key, value) {
    try {
        Java.perform(() => {
            const context = Java.use("android.app.ActivityThread")
                .currentApplication()
                .getApplicationContext();
            const resolver = context.getContentResolver();
            
            let Settings;
            if (namespace === 'global') {
                Settings = Java.use("android.provider.Settings$Global");
            } else if (namespace === 'system') {
                Settings = Java.use("android.provider.Settings$System");
            } else if (namespace === 'secure') {
                Settings = Java.use("android.provider.Settings$Secure");
            } else {
                return { success: false, error: `Invalid namespace: ${namespace}` };
            }
            
            if (typeof value === 'number') {
                Settings.putInt(resolver, key, value);
            } else {
                Settings.putString(resolver, key, value.toString());
            }
            
            return { success: true };
        });
    } catch (e) {
        return { success: false, error: e.message };
    }
}

/**
 * Read a setting value
 */
function readSetting(namespace, key, defaultValue) {
    try {
        let result;
        Java.perform(() => {
            const context = Java.use("android.app.ActivityThread")
                .currentApplication()
                .getApplicationContext();
            const resolver = context.getContentResolver();
            
            let Settings;
            if (namespace === 'global') {
                Settings = Java.use("android.provider.Settings$Global");
            } else if (namespace === 'system') {
                Settings = Java.use("android.provider.Settings$System");
            } else if (namespace === 'secure') {
                Settings = Java.use("android.provider.Settings$Secure");
            }
            
            result = Settings.getInt(resolver, key, defaultValue || 0);
        });
        return { success: true, value: result };
    } catch (e) {
        return { success: false, error: e.message };
    }
}

module.exports = {
    install,
    getSettings,
    forceSetting,
    readSetting,
    hooks,
    SETTINGS_KEYS
};
