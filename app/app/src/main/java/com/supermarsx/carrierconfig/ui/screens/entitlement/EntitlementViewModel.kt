package com.supermarsx.carrierconfig.ui.screens.entitlement

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarsx.carrierconfig.instrumentation.FridaManager
import com.supermarsx.carrierconfig.instrumentation.ProfileManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Entitlement (Method 2) — Runtime Entitlement Simulation.
 *
 * Manages Frida/LSPosed backend selection, hook profile loading,
 * entitlement package scanning, and session lifecycle per spec Section 4.3 / 6.x.
 */
@HiltViewModel
class EntitlementViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fridaManager: FridaManager,
    private val profileManager: ProfileManager
) : ViewModel() {

    private val _state = MutableStateFlow(EntitlementState())
    val state: StateFlow<EntitlementState> = _state.asStateFlow()

    private val _sessionEvents = MutableStateFlow<List<SessionEvent>>(emptyList())
    val sessionEvents: StateFlow<List<SessionEvent>> = _sessionEvents.asStateFlow()

    private var sessionJob: Job? = null

    init {
        loadProfiles()
        scanEntitlementPackages()
        checkFridaStatus()
    }

    // ── Profiles tab ────────────────────────────────────────

    fun loadProfiles() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingProfiles = true) }
            try {
                val database = profileManager.loadProfiles()
                _state.update {
                    it.copy(
                        profiles = database.profiles,
                        profilesMetadata = database.metadata,
                        isLoadingProfiles = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingProfiles = false,
                        error = "Failed to load profiles: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectProfile(profileId: String) {
        _state.update { it.copy(selectedProfileId = profileId) }
    }

    fun setBackend(backend: HookBackend) {
        _state.update { it.copy(selectedBackend = backend) }
    }

    // ── Hooks tab ───────────────────────────────────────────

    /**
     * Scan for installed entitlement-related packages (spec Section 6.2).
     */
    fun scanEntitlementPackages() {
        viewModelScope.launch {
            _state.update { it.copy(isScanningPackages = true) }
            try {
                val pm = context.packageManager
                val knownPackages = listOf(
                    "com.sec.imsservice",
                    "com.sec.ims",
                    "com.google.android.ims",
                    "com.samsung.android.ims",
                    "com.samsung.android.imsservice",
                    "com.samsung.android.vowifichecker",
                    "com.samsung.ims.smk",
                    "com.sec.epdg"
                )

                val installed = knownPackages.mapNotNull { pkg ->
                    try {
                        val info = pm.getPackageInfo(pkg, 0)
                        EntitlementPackage(
                            packageName = pkg,
                            versionName = info.versionName ?: "unknown",
                            isInstalled = true,
                            isHookTarget = true
                        )
                    } catch (_: PackageManager.NameNotFoundException) {
                        null
                    }
                }

                _state.update {
                    it.copy(
                        entitlementPackages = installed,
                        isScanningPackages = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isScanningPackages = false,
                        error = "Package scan failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun togglePackageHook(packageName: String) {
        _state.update { current ->
            val updated = current.entitlementPackages.map { pkg ->
                if (pkg.packageName == packageName) {
                    pkg.copy(isHookTarget = !pkg.isHookTarget)
                } else pkg
            }
            current.copy(entitlementPackages = updated)
        }
    }

    // ── Session tab ─────────────────────────────────────────

    fun checkFridaStatus() {
        viewModelScope.launch {
            try {
                val status = fridaManager.getStatus()
                _state.update {
                    it.copy(
                        fridaInstalled = status.isInstalled,
                        fridaRunning = status.isRunning,
                        fridaVersion = status.version,
                        fridaPid = status.pid
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Frida status check failed: ${e.message}") }
            }
        }
    }

    fun installFrida() {
        viewModelScope.launch {
            _state.update { it.copy(isInstallingFrida = true) }
            fridaManager.installFridaServer().fold(
                onSuccess = {
                    _state.update { it.copy(isInstallingFrida = false, fridaInstalled = true) }
                    checkFridaStatus()
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(isInstallingFrida = false, error = "Install failed: ${e.message}")
                    }
                }
            )
        }
    }

    fun startSession() {
        val profile = _state.value.selectedProfileId ?: return
        val targets = _state.value.entitlementPackages
            .filter { it.isHookTarget }
            .map { it.packageName }
        val target = targets.firstOrNull() ?: "com.sec.imsservice"

        _state.update { it.copy(sessionActive = true) }
        _sessionEvents.value = emptyList()

        sessionJob = viewModelScope.launch {
            try {
                fridaManager.startSession(target = target, profile = profile).collect { event ->
                    _sessionEvents.update { current ->
                        (current + SessionEvent(
                            timestamp = System.currentTimeMillis(),
                            type = SessionEventType.INFO,
                            message = event
                        )).takeLast(500)
                    }
                }
            } catch (e: Exception) {
                _sessionEvents.update { current ->
                    current + SessionEvent(
                        timestamp = System.currentTimeMillis(),
                        type = SessionEventType.ERROR,
                        message = "Session error: ${e.message}"
                    )
                }
                _state.update { it.copy(sessionActive = false) }
            }
        }
    }

    fun stopSession() {
        sessionJob?.cancel()
        sessionJob = null
        viewModelScope.launch {
            fridaManager.stopServer()
            _state.update { it.copy(sessionActive = false) }
            _sessionEvents.update { current ->
                current + SessionEvent(
                    timestamp = System.currentTimeMillis(),
                    type = SessionEventType.INFO,
                    message = "Session stopped"
                )
            }
        }
    }

    fun exportSessionTrace(): String {
        val events = _sessionEvents.value
        return buildString {
            appendLine("{")
            appendLine("  \"device\": { \"model\": \"${android.os.Build.MODEL}\", \"build\": \"${android.os.Build.FINGERPRINT}\" },")
            appendLine("  \"events\": [")
            events.forEachIndexed { i, ev ->
                val comma = if (i < events.lastIndex) "," else ""
                appendLine("    { \"t\": ${ev.timestamp}, \"type\": \"${ev.type.name}\", \"message\": \"${ev.message.replace("\"", "\\\"")}\" }$comma")
            }
            appendLine("  ]")
            appendLine("}")
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

// ── State models ────────────────────────────────────────────

/**
 * Active tab for the EntitlementScreen
 */
enum class EntitlementTab(val displayName: String) {
    PROFILES("Profiles"),
    HOOKS("Hooks"),
    SESSION("Session")
}

/**
 * Backend selector per spec Section 6.1
 */
enum class HookBackend(val displayName: String) {
    FRIDA("Frida"),
    LSPOSED("LSPosed")
}

/**
 * Combined state for EntitlementScreen
 */
data class EntitlementState(
    // Profiles
    val profiles: List<ProfileManager.HookProfile> = emptyList(),
    val profilesMetadata: ProfileManager.Metadata? = null,
    val selectedProfileId: String? = null,
    val isLoadingProfiles: Boolean = false,
    val selectedBackend: HookBackend = HookBackend.FRIDA,

    // Hooks / packages
    val entitlementPackages: List<EntitlementPackage> = emptyList(),
    val isScanningPackages: Boolean = false,

    // Session
    val sessionActive: Boolean = false,
    val fridaInstalled: Boolean = false,
    val fridaRunning: Boolean = false,
    val fridaVersion: String? = null,
    val fridaPid: Int? = null,
    val isInstallingFrida: Boolean = false,

    // General
    val selectedTab: EntitlementTab = EntitlementTab.PROFILES,
    val error: String? = null
)

/**
 * Discovered entitlement-related package
 */
data class EntitlementPackage(
    val packageName: String,
    val versionName: String,
    val isInstalled: Boolean,
    val isHookTarget: Boolean
)

/**
 * Live session event
 */
data class SessionEvent(
    val timestamp: Long,
    val type: SessionEventType,
    val message: String
)

enum class SessionEventType {
    INFO, HOOK, ERROR
}
