/**
 * CCO Frida Agent - Complete Implementation
 * 
 * Comprehensive runtime instrumentation for Samsung IMS entitlement simulation
 * Features: Recording/Replay, IPC, Multi-profile support, Advanced hooks
 */

console.log("[CCO] Loading complete Frida agent...");

// Import hook modules (loaded from hooks/ directory)
const imsHooks = require('./hooks/ims.js');
const carrierConfigHooks = require('./hooks/carrierconfig.js');
const settingsHooks = require('./hooks/settings.js');
const telephonyHooks = require('./hooks/telephony.js');
const { EventRecorder, createRecordableHook } = require('./hooks/recording.js');
const { IPC, EventLogger } = require('./hooks/ipc.js');

// Global configuration
let config = {
    profile: 'generic',
    forceEntitled: true,
    features: {
        autoBypass: true,
        recordMode: false,
        replayMode: false,
        eventLogging: true,
        ipcEnabled: true
    },
    targets: {
        ims: true,
        carrierConfig: true,
        settings: true,
        telephony: true
    },
    hooks: {
        installed: [],
        active: 0,
        intercepted: 0
    }
};

// Statistics
const stats = {
    startTime: Date.now(),
    totalInterceptions: 0,
    interceptionsByHook: {},
    errors: 0
};

// Initialize IPC
IPC.init();

// Event logging wrapper
function logEvent(type, method, args, result) {
    if (!config.features.eventLogging) return;
    
    stats.totalInterceptions++;
    stats.interceptionsByHook[method] = (stats.interceptionsByHook[method] || 0) + 1;
    
    const event = {
        type: type,
        method: method,
        args: args,
        result: result,
        timestamp: Date.now()
    };
    
    IPC.send({
        type: 'event',
        ...event
    });
    
    // Also record if recording mode enabled
    if (EventRecorder.isRecording) {
        EventRecorder.recordEvent({
            type: type,
            className: method.split('.')[0],
            methodName: method.split('.').slice(1).join('.'),
            args: args,
            returnValue: result
        });
    }
}

// Logging helper
function log(level, message, data) {
    const timestamp = new Date().toISOString();
    console.log(`[${timestamp}] [${level.toUpperCase()}] ${message}`);
    if (data) {
        console.log(JSON.stringify(data, null, 2));
    }
    
    EventLogger.log('debug', level, message, data);
}

// Install all hooks
function installAllHooks() {
    log('info', 'Installing hook modules...');
    
    try {
        if (config.targets.ims) {
            const imsResult = imsHooks.install(config, logEvent, log);
            config.hooks.installed.push(...imsResult.installed);
            log('info', `IMS hooks: ${imsResult.installed.length} installed`);
        }
        
        if (config.targets.carrierConfig) {
            const configResult = carrierConfigHooks.install(config, logEvent, log);
            config.hooks.installed.push(...configResult.installed);
            log('info', `CarrierConfig hooks: ${configResult.installed.length} installed`);
        }
        
        if (config.targets.settings) {
            const settingsResult = settingsHooks.install(config, logEvent, log);
            config.hooks.installed.push(...settingsResult.installed);
            log('info', `Settings hooks: ${settingsResult.installed.length} installed`);
        }
        
        if (config.targets.telephony) {
            const telephonyResult = telephonyHooks.install(config, logEvent, log);
            config.hooks.installed.push(...telephonyResult.installed);
            log('info', `Telephony hooks: ${telephonyResult.installed.length} installed`);
        }
        
        config.hooks.active = config.hooks.installed.length;
        
        log('info', `All hooks installed successfully: ${config.hooks.active} total`);
        
        IPC.send({
            type: 'ready',
            status: 'success',
            hooks: config.hooks.active,
            profile: config.profile
        });
        
    } catch (e) {
        stats.errors++;
        log('error', 'Failed to install hooks', {
            error: e.message,
            stack: e.stack
        });
        
        IPC.send({
            type: 'error',
            status: 'failed',
            error: e.message
        });
    }
}

// Main initialization
Java.perform(() => {
    log('info', 'Java runtime available, initializing agent...');
    
    // Install hooks
    installAllHooks();
    
    // Setup periodic status updates
    setInterval(() => {
        IPC.send({
            type: 'heartbeat',
            uptime: Date.now() - stats.startTime,
            interceptions: stats.totalInterceptions,
            hooks: config.hooks.active
        });
    }, 30000);  // Every 30 seconds
    
    log('info', 'Agent initialization complete');
});

// RPC Exports - Commands from CCO app
rpc.exports = {
    /**
     * Update agent configuration
     */
    updateConfig: function(newConfig) {
        log('info', 'Updating configuration', newConfig);
        Object.assign(config, newConfig);
        
        return {
            status: 'ok',
            config: config
        };
    },
    
    /**
     * Get current agent status and statistics
     */
    getStatus: function() {
        return {
            status: 'active',
            uptime: Date.now() - stats.startTime,
            profile: config.profile,
            hooks: {
                active: config.hooks.active,
                installed: config.hooks.installed,
                intercepted: stats.totalInterceptions
            },
            recording: {
                isRecording: EventRecorder.isRecording,
                isReplaying: EventRecorder.isReplaying,
                eventCount: EventRecorder.recordings.length
            },
            stats: stats
        };
    },
    
    /**
     * Start/Stop recording
     */
    startRecording: function() {
        log('info', 'Starting recording session');
        return EventRecorder.startRecording();
    },
    
    stopRecording: function() {
        log('info', 'Stopping recording session');
        return EventRecorder.stopRecording();
    },
    
    exportRecording: function() {
        log('info', 'Exporting recording');
        return {
            status: 'ok',
            data: EventRecorder.exportRecording()
        };
    },
    
    /**
     * Load and replay recording
     */
    loadRecording: function(sessionJson) {
        log('info', 'Loading recording session');
        try {
            const result = EventRecorder.importRecording(sessionJson);
            return { status: 'ok', result: result };
        } catch (e) {
            return { status: 'error', error: e.message };
        }
    },
    
    startReplay: function() {
        log('info', 'Starting replay mode');
        try {
            return EventRecorder.startReplay();
        } catch (e) {
            return { status: 'error', error: e.message };
        }
    },
    
    stopReplay: function() {
        log('info', 'Stopping replay mode');
        return EventRecorder.stopReplay();
    },
    
    /**
     * Enable/disable specific hook targets
     */
    setHookTarget: function(target, enabled) {
        log('info', `Setting hook target ${target}: ${enabled}`);
        if (config.targets.hasOwnProperty(target)) {
            config.targets[target] = enabled;
            return { status: 'ok', target: target, enabled: enabled };
        }
        return { status: 'error', error: `Unknown target: ${target}` };
    },
    
    /**
     * Force bypass mode
     */
    setForceEntitled: function(enabled) {
        log('info', `Setting forceEntitled: ${enabled}`);
        config.forceEntitled = enabled;
        return { status: 'ok', forceEntitled: enabled };
    },
    
    /**
     * Get statistics
     */
    getStatistics: function() {
        return {
            status: 'ok',
            stats: {
                ...stats,
                uptime: Date.now() - stats.startTime,
                recordingStats: EventRecorder.getStats()
            }
        };
    },
    
    /**
     * Clear statistics
     */
    clearStatistics: function() {
        stats.totalInterceptions = 0;
        stats.interceptionsByHook = {};
        stats.errors = 0;
        log('info', 'Statistics cleared');
        return { status: 'ok' };
    },
    
    /**
     * Reload hooks (useful after config changes)
     */
    reloadHooks: function() {
        log('info', 'Reloading all hooks');
        config.hooks.installed = [];
        config.hooks.active = 0;
        
        try {
            installAllHooks();
            return { status: 'ok', hooks: config.hooks.active };
        } catch (e) {
            return { status: 'error', error: e.message };
        }
    }
};

console.log("[CCO] Complete Frida agent ready");
console.log(`[CCO] Features: Recording=${EventRecorder}, IPC=${IPC ? 'enabled' : 'disabled'}`);
