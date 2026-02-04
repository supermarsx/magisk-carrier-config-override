/**
 * Utility Functions for Hooks
 * 
 * Shared utilities for sanitization, formatting, and common operations
 */

/**
 * Sanitize sensitive data in logs
 */
function sanitize(data, options = {}) {
    if (data === null || data === undefined) {
        return data;
    }
    
    const {
        maskIMSI = true,
        maskIMEI = true,
        maskPhoneNumber = true,
        maskSerialNo = true
    } = options;
    
    if (typeof data === 'string') {
        let sanitized = data;
        
        // Mask IMSI (15 digits)
        if (maskIMSI) {
            sanitized = sanitized.replace(/\b\d{15}\b/g, 'IMSI_REDACTED');
        }
        
        // Mask IMEI (14-15 digits)
        if (maskIMEI) {
            sanitized = sanitized.replace(/\b\d{14,15}\b/g, 'IMEI_REDACTED');
        }
        
        // Mask phone numbers (various formats)
        if (maskPhoneNumber) {
            sanitized = sanitized.replace(/\+?\d{10,15}/g, 'PHONE_REDACTED');
        }
        
        // Mask serial numbers
        if (maskSerialNo) {
            sanitized = sanitized.replace(/[A-Z0-9]{8,}/g, (match) => {
                return match.length > 8 ? 'SERIAL_REDACTED' : match;
            });
        }
        
        return sanitized;
    }
    
    if (typeof data === 'object') {
        const sanitized = Array.isArray(data) ? [] : {};
        
        for (const [key, value] of Object.entries(data)) {
            // Sanitize keys that might contain sensitive info
            if (key.toLowerCase().includes('imsi') || 
                key.toLowerCase().includes('imei') ||
                key.toLowerCase().includes('serial') ||
                key.toLowerCase().includes('phone')) {
                sanitized[key] = 'REDACTED';
            } else {
                sanitized[key] = sanitize(value, options);
            }
        }
        
        return sanitized;
    }
    
    return data;
}

/**
 * Format bytes to human-readable string
 */
function formatBytes(bytes) {
    if (bytes === 0) return '0 B';
    
    const units = ['B', 'KB', 'MB', 'GB', 'TB'];
    const k = 1024;
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + units[i];
}

/**
 * Format duration to human-readable string
 */
function formatDuration(ms) {
    const seconds = Math.floor(ms / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    
    if (days > 0) {
        return `${days}d ${hours % 24}h`;
    } else if (hours > 0) {
        return `${hours}h ${minutes % 60}m`;
    } else if (minutes > 0) {
        return `${minutes}m ${seconds % 60}s`;
    } else {
        return `${seconds}s`;
    }
}

/**
 * Deep clone an object
 */
function deepClone(obj) {
    if (obj === null || typeof obj !== 'object') {
        return obj;
    }
    
    if (Array.isArray(obj)) {
        return obj.map(item => deepClone(item));
    }
    
    const cloned = {};
    for (const [key, value] of Object.entries(obj)) {
        cloned[key] = deepClone(value);
    }
    
    return cloned;
}

/**
 * Throttle function calls
 */
function throttle(func, delay) {
    let lastCall = 0;
    let timeout = null;
    
    return function(...args) {
        const now = Date.now();
        
        if (now - lastCall >= delay) {
            lastCall = now;
            func.apply(this, args);
        } else {
            if (timeout) {
                clearTimeout(timeout);
            }
            
            timeout = setTimeout(() => {
                lastCall = Date.now();
                func.apply(this, args);
            }, delay - (now - lastCall));
        }
    };
}

/**
 * Debounce function calls
 */
function debounce(func, delay) {
    let timeout = null;
    
    return function(...args) {
        if (timeout) {
            clearTimeout(timeout);
        }
        
        timeout = setTimeout(() => {
            func.apply(this, args);
        }, delay);
    };
}

/**
 * Safe JSON stringify with error handling
 */
function safeStringify(obj, indent = 0) {
    try {
        return JSON.stringify(obj, (key, value) => {
            // Handle circular references
            if (typeof value === 'object' && value !== null) {
                if (cache.has(value)) {
                    return '[Circular]';
                }
                cache.add(value);
            }
            return value;
        }, indent);
    } catch (e) {
        return `[Stringify Error: ${e.message}]`;
    }
}
const cache = new WeakSet();

/**
 * Extract stack trace
 */
function getStackTrace() {
    try {
        const Exception = Java.use("java.lang.Exception");
        const exception = Exception.$new();
        const trace = exception.getStackTrace();
        
        const frames = [];
        for (let i = 0; i < Math.min(trace.length, 10); i++) {
            const frame = trace[i];
            frames.push({
                class: frame.getClassName(),
                method: frame.getMethodName(),
                file: frame.getFileName(),
                line: frame.getLineNumber()
            });
        }
        
        return frames;
    } catch (e) {
        return [];
    }
}

/**
 * Get caller information
 */
function getCaller(skipFrames = 2) {
    try {
        const Exception = Java.use("java.lang.Exception");
        const exception = Exception.$new();
        const trace = exception.getStackTrace();
        
        if (trace.length > skipFrames) {
            const frame = trace[skipFrames];
            return {
                class: frame.getClassName(),
                method: frame.getMethodName(),
                location: `${frame.getFileName()}:${frame.getLineNumber()}`
            };
        }
    } catch (e) {
        return null;
    }
}

/**
 * Java object to JS object conversion
 */
function javaToJS(obj) {
    if (obj === null || obj === undefined) {
        return null;
    }
    
    try {
        // Try toString first
        if (obj.$className) {
            const className = obj.$className;
            
            // Handle common types
            if (className === 'java.lang.String') {
                return obj.toString();
            }
            if (className === 'java.lang.Integer' || 
                className === 'java.lang.Long' ||
                className === 'java.lang.Float' ||
                className === 'java.lang.Double') {
                return obj.intValue ? obj.intValue() : obj.floatValue();
            }
            if (className === 'java.lang.Boolean') {
                return obj.booleanValue();
            }
            if (className === 'android.os.Bundle' || 
                className === 'android.os.PersistableBundle') {
                return bundleToJS(obj);
            }
        }
        
        return obj.toString();
    } catch (e) {
        return `[Conversion Error: ${e.message}]`;
    }
}

/**
 * Bundle to JS object
 */
function bundleToJS(bundle) {
    const obj = {};
    
    try {
        const keySet = bundle.keySet();
        const iterator = keySet.iterator();
        
        while (iterator.hasNext()) {
            const key = iterator.next();
            const value = bundle.get(key);
            obj[key] = javaToJS(value);
        }
    } catch (e) {
        obj._error = e.message;
    }
    
    return obj;
}

/**
 * Generate unique ID
 */
function generateId() {
    return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}

/**
 * Measure execution time
 */
function measure(name, func) {
    const start = Date.now();
    try {
        const result = func();
        const duration = Date.now() - start;
        return { success: true, result, duration };
    } catch (e) {
        const duration = Date.now() - start;
        return { success: false, error: e.message, duration };
    }
}

/**
 * Retry with exponential backoff
 */
async function retry(func, options = {}) {
    const {
        maxAttempts = 3,
        initialDelay = 100,
        maxDelay = 5000,
        factor = 2
    } = options;
    
    let delay = initialDelay;
    
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
        try {
            return await func();
        } catch (e) {
            if (attempt === maxAttempts) {
                throw e;
            }
            
            await new Promise(resolve => setTimeout(resolve, delay));
            delay = Math.min(delay * factor, maxDelay);
        }
    }
}

/**
 * LRU Cache implementation
 */
class LRUCache {
    constructor(maxSize = 100) {
        this.maxSize = maxSize;
        this.cache = new Map();
    }
    
    get(key) {
        if (!this.cache.has(key)) {
            return undefined;
        }
        
        const value = this.cache.get(key);
        this.cache.delete(key);
        this.cache.set(key, value);
        return value;
    }
    
    set(key, value) {
        if (this.cache.has(key)) {
            this.cache.delete(key);
        } else if (this.cache.size >= this.maxSize) {
            const firstKey = this.cache.keys().next().value;
            this.cache.delete(firstKey);
        }
        
        this.cache.set(key, value);
    }
    
    has(key) {
        return this.cache.has(key);
    }
    
    clear() {
        this.cache.clear();
    }
    
    size() {
        return this.cache.size;
    }
}

module.exports = {
    sanitize,
    formatBytes,
    formatDuration,
    deepClone,
    throttle,
    debounce,
    safeStringify,
    getStackTrace,
    getCaller,
    javaToJS,
    bundleToJS,
    generateId,
    measure,
    retry,
    LRUCache
};
