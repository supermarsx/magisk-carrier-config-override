/**
 * Diagnostics Hooks
 * 
 * Collects diagnostic information and system state
 */

const hooks = {
    installed: [],
    diagnostics: {
        device: {},
        telephony: {},
        ims: {},
        settings: {},
        system: {}
    },
    lastCollection: 0
};

function install(config, logEvent, log) {
    log("info", "Installing Diagnostics hooks...");
    
    // Collect initial diagnostics
    collectDiagnostics(logEvent, log);
    
    // Set up periodic collection
    setInterval(() => {
        if (config.features.diagnostics) {
            collectDiagnostics(logEvent, log);
        }
    }, 30000); // Every 30 seconds
    
    log("info", "Diagnostics hooks installed");
    return hooks;
}

/**
 * Collect comprehensive diagnostics
 */
function collectDiagnostics(logEvent, log) {
    try {
        Java.perform(() => {
            // Device info
            hooks.diagnostics.device = getDeviceInfo();
            
            // Telephony state
            hooks.diagnostics.telephony = getTelephonyState();
            
            // IMS state
            hooks.diagnostics.ims = getImsState();
            
            // Settings
            hooks.diagnostics.settings = getWfcSettings();
            
            // System state
            hooks.diagnostics.system = getSystemState();
            
            hooks.lastCollection = Date.now();
            
            logEvent("diagnostics_collected", "collectDiagnostics", [], {
                timestamp: hooks.lastCollection,
                sections: Object.keys(hooks.diagnostics)
            });
        });
    } catch (e) {
        log("error", "Failed to collect diagnostics", { error: e.message });
    }
}

function getDeviceInfo() {
    const info = {};
    
    try {
        const Build = Java.use("android.os.Build");
        info.manufacturer = Build.MANUFACTURER.value;
        info.brand = Build.BRAND.value;
        info.model = Build.MODEL.value;
        info.device = Build.DEVICE.value;
        info.product = Build.PRODUCT.value;
        info.androidVersion = Build.VERSION.RELEASE.value;
        info.sdkInt = Build.VERSION.SDK_INT.value;
        info.buildId = Build.DISPLAY.value;
        
        // Try Samsung-specific
        try {
            info.oneUIVersion = Build.VERSION.SEM_PLATFORM_INT.value;
        } catch (e) {
            info.oneUIVersion = "N/A";
        }
    } catch (e) {
        info.error = e.message;
    }
    
    return info;
}

function getTelephonyState() {
    const state = {};
    
    try {
        const context = Java.use("android.app.ActivityThread")
            .currentApplication()
            .getApplicationContext();
        const tm = context.getSystemService("phone");
        
        if (tm) {
            // SIM state
            try {
                state.simState = tm.getSimState();
            } catch (e) {}
            
            // Operator info
            try {
                state.simOperator = tm.getSimOperator();
                state.simOperatorName = tm.getSimOperatorName();
                state.networkOperator = tm.getNetworkOperator();
                state.networkOperatorName = tm.getNetworkOperatorName();
            } catch (e) {}
            
            // Network type
            try {
                state.networkType = tm.getNetworkType();
                state.dataNetworkType = tm.getDataNetworkType();
            } catch (e) {}
            
            // Phone type
            try {
                state.phoneType = tm.getPhoneType();
            } catch (e) {}
            
            // Data state
            try {
                state.dataState = tm.getDataState();
                state.dataActivity = tm.getDataActivity();
            } catch (e) {}
            
            // Call state
            try {
                state.callState = tm.getCallState();
            } catch (e) {}
        }
    } catch (e) {
        state.error = e.message;
    }
    
    return state;
}

function getImsState() {
    const state = {};
    
    try {
        const context = Java.use("android.app.ActivityThread")
            .currentApplication()
            .getApplicationContext();
        
        // Try to get ImsManager
        try {
            const ImsManager = Java.use("android.telephony.ims.ImsManager");
            const imsManager = context.getSystemService("ims");
            
            if (imsManager) {
                // VoLTE
                try {
                    state.volteProvisioned = imsManager.isVolteProvisioned();
                } catch (e) {}
                
                // VoWiFi
                try {
                    state.wfcEnabled = imsManager.isWfcEnabled();
                    state.wfcEntitled = imsManager.isWfcEntitled();
                } catch (e) {}
                
                // VT
                try {
                    state.vtEnabled = imsManager.isVtEnabled();
                } catch (e) {}
            }
        } catch (e) {
            state.imsManagerError = e.message;
        }
        
        // Try Samsung ImsRegistry
        try {
            const ImsRegistry = Java.use("com.sec.ims.ImsRegistry");
            const services = ImsRegistry.getServices();
            state.imsServicesCount = services ? services.length : 0;
        } catch (e) {
            state.imsRegistryError = e.message;
        }
    } catch (e) {
        state.error = e.message;
    }
    
    return state;
}

function getWfcSettings() {
    const settings = {};
    
    try {
        const context = Java.use("android.app.ActivityThread")
            .currentApplication()
            .getApplicationContext();
        const resolver = context.getContentResolver();
        const SettingsGlobal = Java.use("android.provider.Settings$Global");
        
        // WFC settings
        try {
            settings.wfcEnabled = SettingsGlobal.getInt(resolver, "wfc_ims_enabled", 0);
            settings.wfcMode = SettingsGlobal.getInt(resolver, "wfc_ims_mode", 0);
            settings.wfcRoamingEnabled = SettingsGlobal.getInt(resolver, "wfc_ims_roaming_enabled", 0);
            settings.wfcRoamingMode = SettingsGlobal.getInt(resolver, "wfc_ims_roaming_mode", 0);
        } catch (e) {}
        
        // VoLTE settings
        try {
            settings.volteEnabled = SettingsGlobal.getInt(resolver, "volte_vt_enabled", 0);
        } catch (e) {}
    } catch (e) {
        settings.error = e.message;
    }
    
    return settings;
}

function getSystemState() {
    const state = {};
    
    try {
        // Memory
        const Runtime = Java.use("java.lang.Runtime");
        const runtime = Runtime.getRuntime();
        state.memory = {
            free: runtime.freeMemory(),
            total: runtime.totalMemory(),
            max: runtime.maxMemory()
        };
        
        // Uptime
        const SystemClock = Java.use("android.os.SystemClock");
        state.uptime = SystemClock.uptimeMillis();
        
        // Current process
        const Process = Java.use("android.os.Process");
        state.pid = Process.myPid();
        state.uid = Process.myUid();
        
    } catch (e) {
        state.error = e.message;
    }
    
    return state;
}

/**
 * Get full diagnostic report
 */
function getReport() {
    // Refresh diagnostics
    collectDiagnostics(() => {}, () => {});
    
    return {
        timestamp: hooks.lastCollection,
        diagnostics: hooks.diagnostics,
        summary: generateSummary()
    };
}

function generateSummary() {
    const diag = hooks.diagnostics;
    const summary = {
        device: `${diag.device.manufacturer} ${diag.device.model}`,
        android: diag.device.androidVersion,
        carrier: diag.telephony.simOperatorName || 'Unknown',
        simState: diag.telephony.simState,
        networkType: diag.telephony.networkType,
        wfcEnabled: diag.settings.wfcEnabled === 1,
        volteEnabled: diag.settings.volteEnabled === 1,
        imsRegistered: diag.ims.imsServicesCount > 0
    };
    
    return summary;
}

/**
 * Export diagnostics to file
 */
function exportDiagnostics() {
    const report = getReport();
    
    try {
        const File = Java.use("java.io.File");
        const FileWriter = Java.use("java.io.FileWriter");
        
        const path = "/sdcard/cco-diagnostics.json";
        const file = File.$new(path);
        const writer = FileWriter.$new(file);
        
        writer.write(JSON.stringify(report, null, 2));
        writer.close();
        
        return { success: true, path };
    } catch (e) {
        return { success: false, error: e.message };
    }
}

module.exports = {
    install,
    collectDiagnostics,
    getReport,
    exportDiagnostics,
    hooks
};
