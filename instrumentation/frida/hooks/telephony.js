/**
 * Telephony Hooks
 * 
 * Intercepts telephony stack for SIM, network, and radio state
 * Targets: android.telephony.TelephonyManager, android.telephony.SubscriptionManager
 */

const hooks = {
    installed: [],
    simStates: {},
    networkTypes: {},
    lastUpdate: Date.now()
};

function install(config, logEvent, log) {
    log("info", "Installing Telephony hooks...");
    
    // Hook 1: TelephonyManager - SIM state
    hookSimState(config, logEvent, log);
    
    // Hook 2: TelephonyManager - Network type
    hookNetworkType(config, logEvent, log);
    
    // Hook 3: TelephonyManager - Carrier info
    hookCarrierInfo(config, logEvent, log);
    
    // Hook 4: SubscriptionManager - Active subscriptions
    hookSubscriptionManager(config, logEvent, log);
    
    // Hook 5: ServiceState - Registration state
    hookServiceState(config, logEvent, log);
    
    // Hook 6: TelephonyManager - VoLTE/VoWiFi capability
    hookCapabilities(config, logEvent, log);
    
    log("info", `Telephony hooks installed: ${hooks.installed.length}`);
    return hooks;
}

function hookSimState(config, logEvent, log) {
    try {
        const TelephonyManager = Java.use("android.telephony.TelephonyManager");
        
        // getSimState()
        if (TelephonyManager.getSimState) {
            TelephonyManager.getSimState.overload().implementation = function() {
                const state = this.getSimState();
                
                hooks.simStates['default'] = state;
                
                logEvent("sim_state", "TelephonyManager.getSimState", [], {
                    state: simStateToString(state),
                    stateCode: state
                });
                
                return state;
            };
            hooks.installed.push("TelephonyManager.getSimState()");
        }
        
        // getSimState(int slotIndex)
        if (TelephonyManager.getSimState.overload('int')) {
            TelephonyManager.getSimState.overload('int').implementation = function(slotIndex) {
                const state = this.getSimState(slotIndex);
                
                hooks.simStates[slotIndex] = state;
                
                logEvent("sim_state", "TelephonyManager.getSimState", [slotIndex], {
                    slotIndex,
                    state: simStateToString(state),
                    stateCode: state
                });
                
                return state;
            };
            hooks.installed.push("TelephonyManager.getSimState(int)");
        }
        
    } catch (e) {
        log("error", "Failed to hook SIM state", { error: e.message });
    }
}

function hookNetworkType(config, logEvent, log) {
    try {
        const TelephonyManager = Java.use("android.telephony.TelephonyManager");
        
        if (TelephonyManager.getNetworkType) {
            TelephonyManager.getNetworkType.implementation = function() {
                const networkType = this.getNetworkType();
                
                hooks.networkTypes['default'] = networkType;
                
                logEvent("network_type", "TelephonyManager.getNetworkType", [], {
                    type: networkTypeToString(networkType),
                    typeCode: networkType
                });
                
                return networkType;
            };
            hooks.installed.push("TelephonyManager.getNetworkType");
        }
        
        if (TelephonyManager.getDataNetworkType) {
            TelephonyManager.getDataNetworkType.implementation = function() {
                const networkType = this.getDataNetworkType();
                
                logEvent("network_type", "TelephonyManager.getDataNetworkType", [], {
                    type: networkTypeToString(networkType),
                    typeCode: networkType
                });
                
                return networkType;
            };
            hooks.installed.push("TelephonyManager.getDataNetworkType");
        }
        
    } catch (e) {
        log("error", "Failed to hook network type", { error: e.message });
    }
}

function hookCarrierInfo(config, logEvent, log) {
    try {
        const TelephonyManager = Java.use("android.telephony.TelephonyManager");
        
        // getSimOperator
        if (TelephonyManager.getSimOperator) {
            TelephonyManager.getSimOperator.implementation = function() {
                const operator = this.getSimOperator();
                
                logEvent("carrier_info", "TelephonyManager.getSimOperator", [], {
                    operator
                });
                
                return operator;
            };
            hooks.installed.push("TelephonyManager.getSimOperator");
        }
        
        // getSimOperatorName
        if (TelephonyManager.getSimOperatorName) {
            TelephonyManager.getSimOperatorName.implementation = function() {
                const operatorName = this.getSimOperatorName();
                
                logEvent("carrier_info", "TelephonyManager.getSimOperatorName", [], {
                    operatorName
                });
                
                return operatorName;
            };
            hooks.installed.push("TelephonyManager.getSimOperatorName");
        }
        
        // getNetworkOperator
        if (TelephonyManager.getNetworkOperator) {
            TelephonyManager.getNetworkOperator.implementation = function() {
                const operator = this.getNetworkOperator();
                
                logEvent("carrier_info", "TelephonyManager.getNetworkOperator", [], {
                    operator
                });
                
                return operator;
            };
            hooks.installed.push("TelephonyManager.getNetworkOperator");
        }
        
    } catch (e) {
        log("error", "Failed to hook carrier info", { error: e.message });
    }
}

function hookSubscriptionManager(config, logEvent, log) {
    try {
        const SubscriptionManager = Java.use("android.telephony.SubscriptionManager");
        
        // getActiveSubscriptionInfoList
        if (SubscriptionManager.getActiveSubscriptionInfoList) {
            SubscriptionManager.getActiveSubscriptionInfoList.implementation = function() {
                const subList = this.getActiveSubscriptionInfoList();
                
                const subs = [];
                if (subList != null) {
                    for (let i = 0; i < subList.size(); i++) {
                        const sub = subList.get(i);
                        subs.push({
                            id: sub.getSubscriptionId(),
                            slot: sub.getSimSlotIndex(),
                            carrier: sub.getCarrierName().toString()
                        });
                    }
                }
                
                logEvent("subscription_query", "SubscriptionManager.getActiveSubscriptionInfoList", [], {
                    count: subs.length,
                    subscriptions: subs
                });
                
                return subList;
            };
            hooks.installed.push("SubscriptionManager.getActiveSubscriptionInfoList");
        }
        
        // getDefaultSubscriptionId
        if (SubscriptionManager.getDefaultSubscriptionId) {
            SubscriptionManager.getDefaultSubscriptionId.implementation = function() {
                const subId = this.getDefaultSubscriptionId();
                
                logEvent("subscription_query", "SubscriptionManager.getDefaultSubscriptionId", [], {
                    subId
                });
                
                return subId;
            };
            hooks.installed.push("SubscriptionManager.getDefaultSubscriptionId");
        }
        
    } catch (e) {
        log("warn", "Failed to hook SubscriptionManager", { error: e.message });
    }
}

function hookServiceState(config, logEvent, log) {
    try {
        const ServiceState = Java.use("android.telephony.ServiceState");
        
        // getState
        if (ServiceState.getState) {
            ServiceState.getState.implementation = function() {
                const state = this.getState();
                
                logEvent("service_state", "ServiceState.getState", [], {
                    state: serviceStateToString(state),
                    stateCode: state
                });
                
                return state;
            };
            hooks.installed.push("ServiceState.getState");
        }
        
        // getDataRegistrationState
        if (ServiceState.getDataRegistrationState) {
            ServiceState.getDataRegistrationState.implementation = function() {
                const state = this.getDataRegistrationState();
                
                logEvent("service_state", "ServiceState.getDataRegistrationState", [], {
                    state: serviceStateToString(state),
                    stateCode: state
                });
                
                return state;
            };
            hooks.installed.push("ServiceState.getDataRegistrationState");
        }
        
        // isUsingCarrierAggregation
        if (ServiceState.isUsingCarrierAggregation) {
            ServiceState.isUsingCarrierAggregation.implementation = function() {
                const isUsing = this.isUsingCarrierAggregation();
                
                logEvent("service_state", "ServiceState.isUsingCarrierAggregation", [], {
                    isUsing
                });
                
                return isUsing;
            };
            hooks.installed.push("ServiceState.isUsingCarrierAggregation");
        }
        
    } catch (e) {
        log("warn", "Failed to hook ServiceState", { error: e.message });
    }
}

function hookCapabilities(config, logEvent, log) {
    try {
        const TelephonyManager = Java.use("android.telephony.TelephonyManager");
        
        // isVolteAvailable (Samsung specific)
        try {
            if (TelephonyManager.isVolteAvailable) {
                TelephonyManager.isVolteAvailable.implementation = function() {
                    const available = this.isVolteAvailable();
                    
                    logEvent("capability_check", "TelephonyManager.isVolteAvailable", [], {
                        available
                    });
                    
                    // Force true if autoBypass enabled
                    if (config.features.autoBypass && !available) {
                        return true;
                    }
                    
                    return available;
                };
                hooks.installed.push("TelephonyManager.isVolteAvailable");
            }
        } catch (e) {
            log("debug", "isVolteAvailable not available");
        }
        
        // isVideoCallingEnabled
        try {
            if (TelephonyManager.isVideoCallingEnabled) {
                TelephonyManager.isVideoCallingEnabled.implementation = function() {
                    const enabled = this.isVideoCallingEnabled();
                    
                    logEvent("capability_check", "TelephonyManager.isVideoCallingEnabled", [], {
                        enabled
                    });
                    
                    return enabled;
                };
                hooks.installed.push("TelephonyManager.isVideoCallingEnabled");
            }
        } catch (e) {
            log("debug", "isVideoCallingEnabled not available");
        }
        
    } catch (e) {
        log("warn", "Failed to hook capabilities", { error: e.message });
    }
}

// Helper functions
function simStateToString(state) {
    const states = {
        0: "UNKNOWN",
        1: "ABSENT",
        2: "PIN_REQUIRED",
        3: "PUK_REQUIRED",
        4: "NETWORK_LOCKED",
        5: "READY",
        6: "NOT_READY",
        7: "PERM_DISABLED",
        8: "CARD_IO_ERROR",
        9: "CARD_RESTRICTED"
    };
    return states[state] || `UNKNOWN(${state})`;
}

function networkTypeToString(type) {
    const types = {
        0: "UNKNOWN",
        1: "GPRS",
        2: "EDGE",
        3: "UMTS",
        4: "CDMA",
        5: "EVDO_0",
        6: "EVDO_A",
        7: "1xRTT",
        8: "HSDPA",
        9: "HSUPA",
        10: "HSPA",
        11: "IDEN",
        12: "EVDO_B",
        13: "LTE",
        14: "EHRPD",
        15: "HSPAP",
        16: "GSM",
        17: "TD_SCDMA",
        18: "IWLAN",
        19: "LTE_CA",
        20: "NR"
    };
    return types[type] || `UNKNOWN(${type})`;
}

function serviceStateToString(state) {
    const states = {
        0: "IN_SERVICE",
        1: "OUT_OF_SERVICE",
        2: "EMERGENCY_ONLY",
        3: "POWER_OFF"
    };
    return states[state] || `UNKNOWN(${state})`;
}

/**
 * Get current telephony status
 */
function getStatus() {
    return {
        simStates: hooks.simStates,
        networkTypes: hooks.networkTypes,
        lastUpdate: hooks.lastUpdate,
        hooksInstalled: hooks.installed.length
    };
}

module.exports = {
    install,
    getStatus,
    hooks
};
