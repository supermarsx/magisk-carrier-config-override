/**
 * CCO Frida Agent - Main Entry Point
 * Runtime instrumentation for Samsung IMS entitlement simulation
 */

console.log("[CCO] Frida agent loading...");

// Configuration (loaded from CCO app)
let config = {
    profile: "generic",
    forceEntitled: true,
    logEvents: true,
    targets: []
};

// Event logging
function logEvent(type, method, args, result) {
    if (config.logEvents) {
        send({
            type: "event",
            timestamp: Date.now(),
            eventType: type,
            method: method,
            args: args,
            result: result
        });
    }
}

// Initialize hooks when ready
Java.perform(() => {
    console.log("[CCO] Java runtime available, installing hooks...");
    
    try {
        // Load hook modules
        loadImsHooks();
        loadCarrierConfigHooks();
        loadSettingsHooks();
        
        console.log("[CCO] All hooks installed successfully");
        send({ type: "ready", status: "success" });
        
    } catch (e) {
        console.error("[CCO] Error installing hooks:", e);
        send({ type: "error", message: e.toString() });
    }
});

/**
 * IMS Service Hooks
 * Targets: com.sec.imsservice
 */
function loadImsHooks() {
    console.log("[CCO] Loading IMS hooks...");
    
    try {
        // Hook: isWfcEntitled
        const ImsManager = Java.use("com.sec.ims.ImsManager");
        if (ImsManager.isWfcEntitled) {
            ImsManager.isWfcEntitled.overload().implementation = function() {
                const original = this.isWfcEntitled();
                const forced = config.forceEntitled ? true : original;
                
                logEvent("entitlement_query", "ImsManager.isWfcEntitled", [], {
                    original: original,
                    forced: forced
                });
                
                console.log(`[CCO] isWfcEntitled: ${original} → ${forced}`);
                return forced;
            };
            console.log("[CCO] ✓ Hooked ImsManager.isWfcEntitled");
        }
    } catch (e) {
        console.log("[CCO] ImsManager hook not available:", e.message);
    }
    
    try {
        // Hook: VoWiFi availability check
        const ImsFeature = Java.use("com.sec.internal.ims.servicemodules.im.ImsFeature");
        if (ImsFeature.isVowifiEnabled) {
            ImsFeature.isVowifiEnabled.implementation = function() {
                const original = this.isVowifiEnabled();
                const forced = config.forceEntitled ? true : original;
                
                logEvent("feature_check", "ImsFeature.isVowifiEnabled", [], {
                    original: original,
                    forced: forced
                });
                
                console.log(`[CCO] isVowifiEnabled: ${original} → ${forced}`);
                return forced;
            };
            console.log("[CCO] ✓ Hooked ImsFeature.isVowifiEnabled");
        }
    } catch (e) {
        console.log("[CCO] ImsFeature hook not available:", e.message);
    }
}

/**
 * CarrierConfig Hooks
 * Runtime override of CarrierConfig values
 */
function loadCarrierConfigHooks() {
    console.log("[CCO] Loading CarrierConfig hooks...");
    
    try {
        const PersistableBundle = Java.use("android.os.PersistableBundle");
        const CarrierConfigManager = Java.use("android.telephony.CarrierConfigManager");
        
        if (CarrierConfigManager.getConfigForSubId) {
            CarrierConfigManager.getConfigForSubId.implementation = function(subId) {
                const bundle = this.getConfigForSubId(subId);
                
                if (bundle && config.forceEntitled) {
                    // Force WFC availability
                    bundle.putBoolean("carrier_wfc_ims_available_bool", true);
                    bundle.putBoolean("carrier_default_wfc_ims_enabled_bool", true);
                    bundle.putBoolean("editable_wfc_mode_bool", true);
                    
                    console.log(`[CCO] Modified CarrierConfig for subId ${subId}`);
                    logEvent("carrier_config", "CarrierConfigManager.getConfigForSubId", [subId], {
                        modified: true
                    });
                }
                
                return bundle;
            };
            console.log("[CCO] ✓ Hooked CarrierConfigManager.getConfigForSubId");
        }
    } catch (e) {
        console.log("[CCO] CarrierConfigManager hook not available:", e.message);
    }
}

/**
 * Settings UI Hooks
 * Ensure Settings pages populate correctly
 */
function loadSettingsHooks() {
    console.log("[CCO] Loading Settings hooks...");
    
    try {
        // Hook Settings preference availability checks
        const WifiCallingSettings = Java.use("com.samsung.android.settings.wifi.WifiCallingSettings");
        
        if (WifiCallingSettings.isWifiCallingSupported) {
            WifiCallingSettings.isWifiCallingSupported.implementation = function(context) {
                const original = this.isWifiCallingSupported(context);
                const forced = config.forceEntitled ? true : original;
                
                logEvent("settings_check", "WifiCallingSettings.isWifiCallingSupported", [], {
                    original: original,
                    forced: forced
                });
                
                console.log(`[CCO] isWifiCallingSupported: ${original} → ${forced}`);
                return forced;
            };
            console.log("[CCO] ✓ Hooked WifiCallingSettings.isWifiCallingSupported");
        }
    } catch (e) {
        console.log("[CCO] WifiCallingSettings hook not available:", e.message);
    }
}

/**
 * RPC Handler - Receive commands from CCO app
 */
rpc.exports = {
    updateConfig: function(newConfig) {
        console.log("[CCO] Updating configuration:", JSON.stringify(newConfig));
        Object.assign(config, newConfig);
        return { status: "ok", config: config };
    },
    
    getStats: function() {
        return {
            status: "active",
            profile: config.profile,
            hooksActive: true
        };
    },
    
    stopSession: function() {
        console.log("[CCO] Session stop requested");
        // Hooks remain but can be toggled via config
        config.forceEntitled = false;
        return { status: "stopped" };
    }
};

console.log("[CCO] Frida agent ready");
