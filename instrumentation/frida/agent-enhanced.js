/**
 * CCO Frida Agent - Enhanced Main Entry Point
 * 
 * Comprehensive runtime instrumentation framework for:
 * - Samsung IMS service manipulation
 * - CarrierConfig override interception
 * - Settings provider hooks
 * - Telephony framework monitoring
 * - Real-time event streaming
 */

// Import hook modules
const imsHooks = require('./hooks/ims');
const carrierConfigHooks = require('./hooks/carrierconfig');
const telephonyHooks = require('./hooks/telephony');
const settingsHooks = require('./hooks/settings');
const diagnosticsHooks = require('./hooks/diagnostics');
const utils = require('./hooks/utils');

console.log("[CCO] ═══════════════════════════════════════");
console.log("[CCO] CarrierConfig Override - Frida Agent");
console.log("[CCO] Version: 1.0.0");
console.log("[CCO] ═══════════════════════════════════════");

// Global configuration
let config = {
    profile: "generic",
    forceEntitled: true,
    logEvents: true,
    logLevel: "info", // debug, info, warn, error
    targets: {
        ims: true,
        carrierConfig: true,
        telephony: true,
        settings: false,
        diagnostics: true
    },
    filters: {
        includeClasses: [],
        excludeClasses: [],
        methods: []
    },
    features: {
        autoBypass: true,
        cacheIntercept: true,
        persistLogs: false
    }
};

// Event queue for batching
let eventQueue = [];
let eventBatchInterval = 1000; // 1 second
let eventBatchTimer = null;

// Statistics tracking
let stats = {
    hooksCalled: 0,
    bytesModified: 0,
    methodsIntercepted: 0,
    errorsEncountered: 0,
    startTime: Date.now()
};

/**
 * RPC Exports - Can be called from Python/CLI
 */
rpc.exports = {
    /**
     * Update configuration at runtime
     */
    updateConfig: function(newConfig) {
        console.log("[CCO] Updating configuration...");
        Object.assign(config, newConfig);
        send({ type: "config_updated", config: config });
        return { success: true, config: config };
    },
    
    /**
     * Get current statistics
     */
    getStats: function() {
        const uptime = (Date.now() - stats.startTime) / 1000;
        return {
            ...stats,
            uptime: uptime,
            hooksPerSecond: (stats.hooksCalled / uptime).toFixed(2)
        };
    },
    
    /**
     * Get current configuration
     */
    getConfig: function() {
        return config;
    },
    
    /**
     * Force a carrier config refresh
     */
    forceRefresh: function() {
        console.log("[CCO] Forcing carrier config refresh...");
        try {
            Java.perform(() => {
                const CarrierConfigManager = Java.use("android.telephony.CarrierConfigManager");
                const context = Java.use("android.app.ActivityThread")
                    .currentApplication()
                    .getApplicationContext();
                
                const ccm = context.getSystemService("carrier_config");
                ccm.notifyConfigChangedForSubId(-1);
                
                send({ type: "refresh_triggered", success: true });
            });
            return { success: true };
        } catch (e) {
            return { success: false, error: e.toString() };
        }
    },
    
    /**
     * Get loaded classes matching pattern
     */
    findClasses: function(pattern) {
        const results = [];
        Java.perform(() => {
            Java.enumerateLoadedClasses({
                onMatch: function(className) {
                    if (className.indexOf(pattern) !== -1) {
                        results.push(className);
                    }
                },
                onComplete: function() {}
            });
        });
        return results;
    },
    
    /**
     * Dump current carrier config
     */
    dumpCarrierConfig: function() {
        console.log("[CCO] Dumping carrier config...");
        return carrierConfigHooks.dumpConfig();
    },
    
    /**
     * Enable/disable specific hook categories
     */
    toggleHooks: function(category, enabled) {
        config.targets[category] = enabled;
        console.log(`[CCO] ${enabled ? 'Enabled' : 'Disabled'} ${category} hooks`);
        return { success: true, category, enabled };
    },
    
    /**
     * Clear event queue
     */
    clearEvents: function() {
        const count = eventQueue.length;
        eventQueue = [];
        return { cleared: count };
    }
};

/**
 * Event logging with batching
 */
function logEvent(type, method, args, result) {
    if (!config.logEvents) return;
    
    stats.hooksCalled++;
    
    const event = {
        type: "event",
        timestamp: Date.now(),
        eventType: type,
        method: method,
        args: utils.sanitizeArgs(args),
        result: utils.sanitizeResult(result),
        thread: Process.getCurrentThreadId()
    };
    
    // Add to queue
    eventQueue.push(event);
    
    // Start batch timer if not running
    if (!eventBatchTimer) {
        eventBatchTimer = setTimeout(flushEvents, eventBatchInterval);
    }
    
    // Immediate send for errors
    if (type === "error") {
        flushEvents();
    }
}

/**
 * Flush event queue to host
 */
function flushEvents() {
    if (eventQueue.length > 0) {
        send({
            type: "event_batch",
            events: eventQueue,
            count: eventQueue.length
        });
        eventQueue = [];
    }
    eventBatchTimer = null;
}

/**
 * Enhanced logging with levels
 */
function log(level, message, data = null) {
    const levels = { debug: 0, info: 1, warn: 2, error: 3 };
    const configLevel = levels[config.logLevel] || 1;
    
    if (levels[level] >= configLevel) {
        const prefix = `[CCO:${level.toUpperCase()}]`;
        if (data) {
            console.log(prefix, message, JSON.stringify(data));
        } else {
            console.log(prefix, message);
        }
    }
}

/**
 * Error handler wrapper
 */
function safeHook(name, hookFunction) {
    try {
        hookFunction();
        log("info", `✓ Installed: ${name}`);
        stats.methodsIntercepted++;
    } catch (e) {
        log("error", `✗ Failed: ${name}`, { error: e.message });
        stats.errorsEncountered++;
    }
}

/**
 * Initialize all hooks
 */
Java.perform(() => {
    log("info", "═══════════════════════════════════════");
    log("info", "Java runtime available, installing hooks...");
    log("info", "═══════════════════════════════════════");
    
    // Install hook modules based on config
    if (config.targets.ims) {
        log("info", "Loading IMS hooks...");
        imsHooks.install(config, logEvent, log);
    }
    
    if (config.targets.carrierConfig) {
        log("info", "Loading CarrierConfig hooks...");
        carrierConfigHooks.install(config, logEvent, log);
    }
    
    if (config.targets.telephony) {
        log("info", "Loading Telephony hooks...");
        telephonyHooks.install(config, logEvent, log);
    }
    
    if (config.targets.settings) {
        log("info", "Loading Settings hooks...");
        settingsHooks.install(config, logEvent, log);
    }
    
    if (config.targets.diagnostics) {
        log("info", "Loading Diagnostics hooks...");
        diagnosticsHooks.install(config, logEvent, log);
    }
    
    log("info", "═══════════════════════════════════════");
    log("info", `✓ Agent initialized successfully`);
    log("info", `  Hooks intercepted: ${stats.methodsIntercepted}`);
    log("info", `  Errors: ${stats.errorsEncountered}`);
    log("info", "═══════════════════════════════════════");
    
    // Send ready signal
    send({
        type: "ready",
        status: "success",
        config: config,
        stats: stats
    });
    
    // Start periodic stats reporting
    setInterval(() => {
        send({
            type: "stats",
            data: rpc.exports.getStats()
        });
    }, 10000); // Every 10 seconds
});

// Handle script unload
Script.bindWeak(Java.vm, () => {
    log("info", "Agent unloading, flushing events...");
    flushEvents();
});

// Export for module usage
module.exports = {
    config,
    logEvent,
    log,
    safeHook
};
