/**
 * IMS Service Hooks
 * 
 * Hooks for Samsung IMS (IP Multimedia Subsystem) services
 * Targets: com.sec.ims.*, com.sec.internal.ims.*
 */

const hooks = {
    installed: [],
    intercepted: 0
};

function install(config, logEvent, log) {
    log("info", "Installing IMS hooks...");
    
    // Hook 1: ImsManager - Main entitlement check
    hookImsManagerEntitlement(config, logEvent, log);
    
    // Hook 2: ImsFeature - Feature availability
    hookImsFeatureAvailability(config, logEvent, log);
    
    // Hook 3: ImsRegistration - Registration status
    hookImsRegistration(config, logEvent, log);
    
    // Hook 4: VoWiFi Service Manager
    hookVoWiFiManager(config, logEvent, log);
    
    // Hook 5: IMS Settings
    hookImsSettings(config, logEvent, log);
    
    // Hook 6: Entitlement Check Service
    hookEntitlementCheck(config, logEvent, log);
    
    log("info", `IMS hooks installed: ${hooks.installed.length}`);
    return hooks;
}

function hookImsManagerEntitlement(config, logEvent, log) {
    try {
        const ImsManager = Java.use("com.sec.ims.ImsManager");
        
        // Primary entitlement method
        if (ImsManager.isWfcEntitled) {
            ImsManager.isWfcEntitled.overload().implementation = function() {
                const original = this.isWfcEntitled();
                const forced = config.forceEntitled ? true : original;
                
                logEvent("entitlement_check", "ImsManager.isWfcEntitled", [], {
                    original,
                    forced,
                    bypassed: original !== forced
                });
                
                hooks.intercepted++;
                return forced;
            };
            hooks.installed.push("ImsManager.isWfcEntitled");
        }
        
        // Alternative entitlement check with phone ID
        if (ImsManager.isWfcEntitled && ImsManager.isWfcEntitled.overload('int')) {
            ImsManager.isWfcEntitled.overload('int').implementation = function(phoneId) {
                const original = this.isWfcEntitled(phoneId);
                const forced = config.forceEntitled ? true : original;
                
                logEvent("entitlement_check", "ImsManager.isWfcEntitled(int)", [phoneId], {
                    original,
                    forced,
                    phoneId
                });
                
                hooks.intercepted++;
                return forced;
            };
            hooks.installed.push("ImsManager.isWfcEntitled(int)");
        }
        
        // Check if VoWiFi is provisioned
        if (ImsManager.isVolteProvisioned) {
            ImsManager.isVolteProvisioned.overload().implementation = function() {
                const original = this.isVolteProvisioned();
                const forced = config.forceEntitled ? true : original;
                
                logEvent("provisioning_check", "ImsManager.isVolteProvisioned", [], {
                    original,
                    forced
                });
                
                return forced;
            };
            hooks.installed.push("ImsManager.isVolteProvisioned");
        }
        
    } catch (e) {
        log("warn", "ImsManager hooks partially available", { error: e.message });
    }
}

function hookImsFeatureAvailability(config, logEvent, log) {
    try {
        const ImsFeature = Java.use("com.sec.internal.ims.servicemodules.im.ImsFeature");
        
        if (ImsFeature.isVowifiEnabled) {
            ImsFeature.isVowifiEnabled.implementation = function() {
                const original = this.isVowifiEnabled();
                const forced = config.forceEntitled ? true : original;
                
                logEvent("feature_check", "ImsFeature.isVowifiEnabled", [], {
                    original,
                    forced
                });
                
                hooks.intercepted++;
                return forced;
            };
            hooks.installed.push("ImsFeature.isVowifiEnabled");
        }
        
        if (ImsFeature.isVolteEnabled) {
            ImsFeature.isVolteEnabled.implementation = function() {
                const original = this.isVolteEnabled();
                const forced = config.forceEntitled ? true : original;
                
                logEvent("feature_check", "ImsFeature.isVolteEnabled", [], {
                    original,
                    forced
                });
                
                return forced;
            };
            hooks.installed.push("ImsFeature.isVolteEnabled");
        }
        
    } catch (e) {
        log("debug", "ImsFeature hooks not available", { error: e.message });
    }
}

function hookImsRegistration(config, logEvent, log) {
    try {
        const ImsRegistry = Java.use("com.sec.ims.ImsRegistry");
        
        // Hook registration status queries
        if (ImsRegistry.isRegistered) {
            const originalIsRegistered = ImsRegistry.isRegistered;
            ImsRegistry.isRegistered.overload('int').implementation = function(phoneId) {
                const original = originalIsRegistered.call(this, phoneId);
                
                logEvent("registration_status", "ImsRegistry.isRegistered", [phoneId], {
                    registered: original,
                    phoneId
                });
                
                return original;
            };
            hooks.installed.push("ImsRegistry.isRegistered");
        }
        
    } catch (e) {
        log("debug", "ImsRegistry hooks not available");
    }
}

function hookVoWiFiManager(config, logEvent, log) {
    try {
        // Samsung-specific VoWiFi manager
        const VoWifiManager = Java.use("com.sec.ims.volte2.IVolteServiceModule");
        
        if (VoWifiManager.getVoWiFiMode) {
            VoWifiManager.getVoWiFiMode.implementation = function() {
                const original = this.getVoWiFiMode();
                
                logEvent("vowifi_mode", "VoWifiManager.getVoWiFiMode", [], {
                    mode: original
                });
                
                return original;
            };
            hooks.installed.push("VoWifiManager.getVoWiFiMode");
        }
        
    } catch (e) {
        log("debug", "VoWifiManager hooks not available");
    }
}

function hookImsSettings(config, logEvent, log) {
    try {
        const ImsSettings = Java.use("com.sec.ims.settings.ImsSettings");
        
        // Hook settings queries
        if (ImsSettings.getBoolean) {
            const originalGetBoolean = ImsSettings.getBoolean;
            ImsSettings.getBoolean.overload('android.content.Context', 'java.lang.String', 'int', 'boolean')
                .implementation = function(context, key, phoneId, defaultValue) {
                
                const original = originalGetBoolean.call(this, context, key, phoneId, defaultValue);
                
                // Force enable for WFC-related settings
                let forced = original;
                if (config.forceEntitled && key.toLowerCase().includes('wfc')) {
                    forced = true;
                }
                
                logEvent("settings_query", "ImsSettings.getBoolean", [key, phoneId], {
                    key,
                    original,
                    forced,
                    modified: original !== forced
                });
                
                return forced;
            };
            hooks.installed.push("ImsSettings.getBoolean");
        }
        
    } catch (e) {
        log("debug", "ImsSettings hooks not available");
    }
}

function hookEntitlementCheck(config, logEvent, log) {
    try {
        // Samsung entitlement check service
        const EntitlementManager = Java.use("com.sec.ims.extensions.EntitlementManager");
        
        if (EntitlementManager.hasEntitlement) {
            EntitlementManager.hasEntitlement.implementation = function(type) {
                const original = this.hasEntitlement(type);
                const forced = config.forceEntitled ? true : original;
                
                logEvent("entitlement_service", "EntitlementManager.hasEntitlement", [type], {
                    type: type.toString(),
                    original,
                    forced
                });
                
                hooks.intercepted++;
                return forced;
            };
            hooks.installed.push("EntitlementManager.hasEntitlement");
        }
        
    } catch (e) {
        log("debug", "EntitlementManager hooks not available");
    }
}

// Additional utility functions
function getImsStatus() {
    try {
        const ImsManager = Java.use("com.sec.ims.ImsManager");
        const context = Java.use("android.app.ActivityThread")
            .currentApplication()
            .getApplicationContext();
        
        const imsManager = context.getSystemService("ims");
        
        return {
            registered: imsManager.isRegistered(),
            entitlement: imsManager.isWfcEntitled(),
            available: true
        };
    } catch (e) {
        return {
            available: false,
            error: e.message
        };
    }
}

module.exports = {
    install,
    getImsStatus,
    hooks
};
