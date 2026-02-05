/**
 * Recording & Replay Engine
 * 
 * Records method calls and return values for later replay
 * Enables "playback mode" where recorded responses are used instead of live calls
 */

const EventRecorder = {
    isRecording: false,
    isReplaying: false,
    recordings: [],
    replayIndex: 0,
    sessionId: null,
    startTime: null,
    
    // Start recording all hook events
    startRecording() {
        this.isRecording = true;
        this.isReplaying = false;
        this.recordings = [];
        this.sessionId = generateSessionId();
        this.startTime = Date.now();
        
        console.log(`[Recording] Started session ${this.sessionId}`);
        return { status: 'recording', sessionId: this.sessionId };
    },
    
    // Stop recording and return data
    stopRecording() {
        this.isRecording = false;
        const duration = Date.now() - this.startTime;
        
        const session = {
            sessionId: this.sessionId,
            startTime: this.startTime,
            duration: duration,
            eventCount: this.recordings.length,
            events: this.recordings
        };
        
        console.log(`[Recording] Stopped. Captured ${this.recordings.length} events in ${duration}ms`);
        
        // Reset for next session
        this.recordings = [];
        this.sessionId = null;
        
        return session;
    },
    
    // Record a single event
    recordEvent(event) {
        if (!this.isRecording) return;
        
        this.recordings.push({
            timestamp: Date.now() - this.startTime,
            index: this.recordings.length,
            ...event
        });
    },
    
    // Load a recording session for replay
    loadRecording(session) {
        if (!session || !session.events) {
            throw new Error("Invalid recording session");
        }
        
        this.recordings = session.events;
        this.replayIndex = 0;
        console.log(`[Replay] Loaded ${this.recordings.length} events from session ${session.sessionId}`);
        
        return { status: 'loaded', eventCount: this.recordings.length };
    },
    
    // Start replay mode
    startReplay() {
        if (this.recordings.length === 0) {
            throw new Error("No recording loaded");
        }
        
        this.isReplaying = true;
        this.isRecording = false;
        this.replayIndex = 0;
        
        console.log(`[Replay] Starting playback of ${this.recordings.length} events`);
        return { status: 'replaying', eventCount: this.recordings.length };
    },
    
    // Stop replay mode
    stopReplay() {
        this.isReplaying = false;
        console.log(`[Replay] Stopped at event ${this.replayIndex}/${this.recordings.length}`);
        
        return { status: 'stopped', eventsPlayed: this.replayIndex };
    },
    
    // Get the next replay event that matches criteria
    getReplayEvent(className, methodName, args) {
        if (!this.isReplaying) return null;
        
        // Search from current index for matching event
        for (let i = this.replayIndex; i < this.recordings.length; i++) {
            const event = this.recordings[i];
            
            if (event.className === className && 
                event.methodName === methodName &&
                this.argsMatch(event.args, args)) {
                
                this.replayIndex = i + 1;
                console.log(`[Replay] Event ${i}: ${className}.${methodName} -> ${JSON.stringify(event.returnValue)}`);
                return event.returnValue;
            }
        }
        
        // No match found, continue from beginning (loop)
        console.log(`[Replay] No matching event found for ${className}.${methodName}, using default`);
        return null;
    },
    
    // Check if arguments match (simplified comparison)
    argsMatch(recordedArgs, currentArgs) {
        if (!recordedArgs && !currentArgs) return true;
        if (!recordedArgs || !currentArgs) return false;
        if (recordedArgs.length !== currentArgs.length) return false;
        
        // Simple equality check (can be enhanced for complex objects)
        for (let i = 0; i < recordedArgs.length; i++) {
            if (recordedArgs[i] !== currentArgs[i]) return false;
        }
        
        return true;
    },
    
    // Export recording to JSON
    exportRecording() {
        return JSON.stringify({
            sessionId: this.sessionId,
            startTime: this.startTime,
            eventCount: this.recordings.length,
            events: this.recordings
        }, null, 2);
    },
    
    // Import recording from JSON
    importRecording(jsonString) {
        try {
            const session = JSON.parse(jsonString);
            return this.loadRecording(session);
        } catch (e) {
            throw new Error(`Failed to import recording: ${e.message}`);
        }
    },
    
    // Get recording statistics
    getStats() {
        const eventsByType = {};
        const eventsByClass = {};
        
        this.recordings.forEach(event => {
            eventsByType[event.type] = (eventsByType[event.type] || 0) + 1;
            eventsByClass[event.className] = (eventsByClass[event.className] || 0) + 1;
        });
        
        return {
            isRecording: this.isRecording,
            isReplaying: this.isReplaying,
            eventCount: this.recordings.length,
            replayIndex: this.replayIndex,
            eventsByType,
            eventsByClass
        };
    }
};

// Helper: Generate unique session ID
function generateSessionId() {
    const timestamp = Date.now();
    const random = Math.random().toString(36).substring(2, 9);
    return `session_${timestamp}_${random}`;
}

// Wrap hook with recording capability
function createRecordableHook(className, methodName, originalImpl, config) {
    return function(...args) {
        // Check if replaying
        if (EventRecorder.isReplaying) {
            const replayValue = EventRecorder.getReplayEvent(className, methodName, args);
            if (replayValue !== null) {
                return replayValue;
            }
        }
        
        // Execute original implementation
        const returnValue = originalImpl.apply(this, args);
        
        // Record if recording enabled
        if (EventRecorder.isRecording) {
            EventRecorder.recordEvent({
                type: 'method_call',
                className: className,
                methodName: methodName,
                args: args.map(a => String(a)),  // Convert to strings for serialization
                returnValue: returnValue,
                thread: Process.getCurrentThreadId()
            });
        }
        
        return returnValue;
    };
}

// Export for use in other hook modules
module.exports = {
    EventRecorder,
    createRecordableHook
};

console.log("[Recording] Recording & Replay engine loaded");
