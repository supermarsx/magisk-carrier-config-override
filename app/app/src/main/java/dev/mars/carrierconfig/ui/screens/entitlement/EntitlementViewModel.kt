package dev.mars.carrierconfig.ui.screens.entitlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarx.carrierconfig.data.repository.FridaRepository
import com.supermarx.carrierconfig.data.repository.LSPosedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Runtime Entitlement screen
 */
@HiltViewModel
class EntitlementViewModel @Inject constructor(
    private val fridaRepository: FridaRepository,
    private val lsposedRepository: LSPosedRepository
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
}
