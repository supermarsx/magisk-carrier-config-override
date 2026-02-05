package dev.mars.carrierconfig.ui.screens.carrierconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarx.carrierconfig.data.model.CarrierConfigPreset
import com.supermarx.carrierconfig.data.repository.CarrierConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for CarrierConfig Override screen
 */
@HiltViewModel
class CarrierConfigViewModel @Inject constructor(
    private val carrierConfigRepository: CarrierConfigRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(CarrierConfigState())
    val state: StateFlow<CarrierConfigState> = _state.asStateFlow()
    
    init {
        loadPresets()
        checkOverrideStatus()
    }
    
    private fun loadPresets() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val presets = carrierConfigRepository.getAvailablePresets()
                _state.update { 
                    it.copy(
                        presets = presets,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Failed to load presets: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    private fun checkOverrideStatus() {
        viewModelScope.launch {
            try {
                val status = carrierConfigRepository.getOverrideStatus()
                _state.update { 
                    it.copy(
                        isOverrideActive = status.isActive,
                        currentPath = status.activePath,
                        deployedPresetId = status.activePresetId
                    )
                }
            } catch (e: Exception) {
                // Ignore errors in status check
            }
        }
    }
    
    fun selectPreset(preset: CarrierConfigPreset) {
        _state.update { it.copy(selectedPreset = preset) }
    }
    
    fun deployPreset(preset: CarrierConfigPreset) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                carrierConfigRepository.deployPreset(preset)
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isOverrideActive = true,
                        deployedPresetId = preset.id,
                        message = "Preset deployed successfully. Restart device to apply changes."
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to deploy preset: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun revertOverride() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                carrierConfigRepository.revertOverride()
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isOverrideActive = false,
                        deployedPresetId = null,
                        message = "Override reverted. Restart device to apply changes."
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to revert override: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun refreshStatus() {
        checkOverrideStatus()
    }
    
    fun clearMessage() {
        _state.update { it.copy(message = null, error = null) }
    }
}
