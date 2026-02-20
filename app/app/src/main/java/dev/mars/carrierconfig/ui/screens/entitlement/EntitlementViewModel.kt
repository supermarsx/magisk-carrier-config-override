package dev.mars.carrierconfig.ui.screens.entitlement

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.mars.carrierconfig.data.repository.FridaRepository
import dev.mars.carrierconfig.data.repository.LSPosedRepository
import dev.mars.carrierconfig.util.UriHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mars.carrierconfig.instrumentation.ProfileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for Runtime Entitlement screen
 */
@HiltViewModel
class EntitlementViewModel @Inject constructor(
    private val fridaRepository: FridaRepository,
    private val lsposedRepository: LSPosedRepository,
    private val profileManager: ProfileManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(EntitlementState())
    val state: StateFlow<EntitlementState> = _state.asStateFlow()
    
    init {
        loadProfiles()
        checkStatus()
    }
    
    private fun loadProfiles() {
        viewModelScope.launch {
            try {
                val profiles = fridaRepository.getAvailableProfiles().map { profile ->
                    HookProfile(
                        id = profile.id,
                        name = profile.name,
                        description = profile.description
                    )
                }
                
                _state.update { 
                    it.copy(
                        profiles = profiles,
                        selectedProfile = profiles.firstOrNull()
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = "Failed to load profiles: ${e.message}")
                }
            }
        }
    }
    
    private fun checkStatus() {
        viewModelScope.launch {
            try {
                val fridaStatus = fridaRepository.getStatus()
                val lsposedActive = lsposedRepository.isModuleActive()
                
                _state.update { 
                    it.copy(
                        fridaStatus = FridaStatus(
                            isInstalled = fridaStatus.isInstalled,
                            isRunning = fridaStatus.isRunning,
                            version = fridaStatus.version
                        ),
                        lsposedModuleActive = lsposedActive
                    )
                }
            } catch (e: Exception) {
                // Ignore status check errors
            }
        }
    }
    
    fun selectBackend(backend: InstrumentationBackend) {
        _state.update { it.copy(selectedBackend = backend) }
    }
    
    fun selectProfile(profile: HookProfile) {
        _state.update { it.copy(selectedProfile = profile) }
    }
    
    fun installFridaServer() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                fridaRepository.installServer()
                checkStatus()
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        message = "Frida server installed successfully"
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to install Frida server: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun startFridaServer() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                fridaRepository.startServer()
                checkStatus()
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        message = "Frida server started"
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to start Frida server: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun stopFridaServer() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                fridaRepository.stopServer()
                checkStatus()
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isSessionActive = false,
                        message = "Frida server stopped"
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to stop Frida server: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun startSession() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val profile = _state.value.selectedProfile
            if (profile == null) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Please select a profile first"
                    )
                }
                return@launch
            }
            
            try {
                fridaRepository.startSession(profile.id)
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isSessionActive = true,
                        message = "Instrumentation session started with ${profile.name}"
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to start session: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun stopSession() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                fridaRepository.stopSession()
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isSessionActive = false,
                        message = "Instrumentation session stopped"
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to stop session: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun refreshStatus() {
        checkStatus()
    }
    
    fun clearMessage() {
        _state.update { it.copy(message = null, error = null) }
    }
    
    /**
     * Export selected profile to file
     */
    fun exportSelectedProfile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val selectedProfile = _state.value.selectedProfile
            if (selectedProfile == null) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "No profile selected"
                    )
                }
                return@launch
            }
            
            try {
                // Get full profile from ProfileManager
                val fullProfile = withContext(Dispatchers.IO) {
                    profileManager.getProfile(selectedProfile.id)
                }
                
                if (fullProfile == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Profile not found"
                        )
                    }
                    return@launch
                }
                
                val json = profileManager.exportProfile(fullProfile)
                withContext(Dispatchers.IO) {
                    UriHelper.writeTextToUri(context, uri, json)
                }
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        message = "Profile '${fullProfile.name}' exported successfully"
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to export profile: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Import profile from file
     */
    fun importProfile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val json = withContext(Dispatchers.IO) {
                    UriHelper.readTextFromUri(context, uri)
                }
                
                if (json == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to read profile file"
                        )
                    }
                    return@launch
                }
                
                val importedProfile = profileManager.importProfile(json)
                
                // Save as custom profile
                val result = profileManager.saveCustomProfile(importedProfile)
                
                if (result.isSuccess) {
                    // Reload profiles to include the new one
                    loadProfiles()
                    
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            message = "Profile '${importedProfile.name}' imported successfully"
                        )
                    }
                } else {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to save imported profile: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to import profile: ${e.message}"
                    )
                }
            }
        }
    }
}
