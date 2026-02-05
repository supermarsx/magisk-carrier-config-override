/**
 * IPC (Inter-Process Communication) Module
 * 
 * Provides communication between Frida agent and CCO app
 * Supports: commands, event streaming, status queries
 */

const IPC = {
    messageQueue: [],
    listeners: {},
    commandHandlers: {},
    maxQueueSize: 1000,
    
    // Initialize IPC system
    init() {
        console.log("[IPC] Initializing inter-process communication...");
        
        // Register default command handlers
        this.registerHandler('ping', this.handlePing.bind(this));
        this.registerHandler('getStatus', this.handleGetStatus.bind(this));
        this.registerHandler('clearEvents', this.handleClearEvents.bind(this));
        this.registerHandler('getEvents', this.handleGetEvents.bind(this));
        
        console.log("[IPC] Ready");
    },
    
    // Send message to CCO app
    send(message) {
        try {
            // Add metadata
            const fullMessage = {
                ...message,
                timestamp: Date.now(),
                pid: Process.id,
                tid: Process.getCurrentThreadId()
            };
            
            // Send via Frida's send()
            send(fullMessage);
            
            // Also queue locally for getEvents
            this.queueMessage(fullMessage);
            
        } catch (e) {
            console.error("[IPC] Failed to send message:", e);
        }
    },
    
    // Queue message in local buffer
    queueMessage(message) {
        this.messageQueue.push(message);
        
        // Limit queue size
        if (this.messageQueue.length > this.maxQueueSize) {
            this.messageQueue.shift();  // Remove oldest
        }
        
        // Notify listeners
        this.notifyListeners('message', message);
    },
    
    // Register command handler
    registerHandler(command, handler) {
        this.commandHandlers[command] = handler;
        console.log(`[IPC] Registered handler: ${command}`);
    },
    
    // Handle incoming command from CCO app
    handleCommand(command, args) {
        console.log(`[IPC] Received command: ${command}`);
        
        const handler = this.commandHandlers[command];
        if (!handler) {
            return {
                status: 'error',
                error: `Unknown command: ${command}`
            };
        }
        
        try {
            return handler(args);
        } catch (e) {
            console.error(`[IPC] Command ${command} failed:`, e);
            return {
                status: 'error',
                error: e.message,
                stack: e.stack
            };
        }
    },
    
    // Register event listener
    on(event, callback) {
        if (!this.listeners[event]) {
            this.listeners[event] = [];
        }
        this.listeners[event].push(callback);
    },
    
    // Notify event listeners
    notifyListeners(event, data) {
        const callbacks = this.listeners[event] || [];
        callbacks.forEach(cb => {
            try {
                cb(data);
            } catch (e) {
                console.error(`[IPC] Listener error for ${event}:`, e);
            }
        });
    },
    
    // Default handlers
    handlePing(args) {
        return {
            status: 'ok',
            message: 'pong',
            timestamp: Date.now()
        };
    },
    
    handleGetStatus(args) {
        return {
            status: 'ok',
            data: {
                pid: Process.id,
                platform: Process.platform,
                arch: Process.arch,
                queueSize: this.messageQueue.length,
                handlers: Object.keys(this.commandHandlers)
            }
        };
    },
    
    handleClearEvents(args) {
        const count = this.messageQueue.length;
        this.messageQueue = [];
        return {
            status: 'ok',
            cleared: count
        };
    },
    
    handleGetEvents(args) {
        const limit = args?.limit || 100;
        const offset = args?.offset || 0;
        
        const events = this.messageQueue.slice(offset, offset + limit);
        
        return {
            status: 'ok',
            data: {
                events: events,
                total: this.messageQueue.length,
                offset: offset,
                limit: limit
            }
        };
    }
};

// Event Logger - Structured logging with categories
const EventLogger = {
    categories: {
        entitlement: { color: '\x1b[32m', enabled: true },  // Green
        config: { color: '\x1b[34m', enabled: true },       // Blue
        settings: { color: '\x1b[36m', enabled: true },     // Cyan
        telephony: { color: '\x1b[33m', enabled: true },    // Yellow
        debug: { color: '\x1b[90m', enabled: true },        // Gray
        error: { color: '\x1b[31m', enabled: true }         // Red
    },
    
    // Log event with category
    log(category, level, message, data) {
        const cat = this.categories[category] || this.categories.debug;
        
        if (!cat.enabled) return;
        
        const timestamp = new Date().toISOString();
        const colorCode = cat.color;
        const resetCode = '\x1b[0m';
        
        // Console output with color
        console.log(`${colorCode}[${timestamp}] [${category.toUpperCase()}] ${message}${resetCode}`);
        if (data) {
            console.log(`${colorCode}${JSON.stringify(data, null, 2)}${resetCode}`);
        }
        
        // Send structured event via IPC
        IPC.send({
            type: 'log',
            category: category,
            level: level,
            message: message,
            data: data
        });
    },
    
    // Convenience methods
    entitlement(message, data) {
        this.log('entitlement', 'info', message, data);
    },
    
    config(message, data) {
        this.log('config', 'info', message, data);
    },
    
    settings(message, data) {
        this.log('settings', 'info', message, data);
    },
    
    telephony(message, data) {
        this.log('telephony', 'info', message, data);
    },
    
    debug(message, data) {
        this.log('debug', 'debug', message, data);
    },
    
    error(message, data) {
        this.log('error', 'error', message, data);
    },
    
    // Enable/disable category
    setCategory(category, enabled) {
        if (this.categories[category]) {
            this.categories[category].enabled = enabled;
        }
    }
};

// Export for use in other modules
module.exports = {
    IPC,
    EventLogger
};

console.log("[IPC] IPC module loaded");
