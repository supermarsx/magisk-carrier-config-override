package dev.mars.carrierconfig.ui.screens.carrierconfig

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.supermarx.carrierconfig.data.model.CarrierConfigPreset
import com.supermarx.carrierconfig.data.repository.CarrierConfigRepository
import com.supermarx.carrierconfig.util.UriHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for CarrierConfig Override screen
 */
@HiltViewModel
class CarrierConfigViewModel @Inject constructor(
    private val carrierConfigRepository: CarrierConfigRepository,
    private val gson: Gson
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
    
    /**
     * Export selected preset to file
     */
    fun exportSelectedPreset(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val preset = _state.value.selectedPreset
            if (preset == null) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "No preset selected"
                    )
                }
                return@launch
            }
            
            try {
                withContext(Dispatchers.IO) {
                    val json = gson.toJson(preset)
                    UriHelper.writeTextToUri(context, uri, json)
                }
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        message = "Preset '${preset.name}' exported successfully"
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to export preset: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Import preset from file
     */
    fun importPreset(context: Context, uri: Uri) {
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
                            error = "Failed to read preset file"
                        )
                    }
                    return@launch
                }
                
                val preset = gson.fromJson(json, CarrierConfigPreset::class.java)
                
                // Add to presets list if not already present
                val updatedPresets = _state.value.presets.toMutableList()
                val existingIndex = updatedPresets.indexOfFirst { it.id == preset.id }
                
                if (existingIndex >= 0) {
                    updatedPresets[existingIndex] = preset
                } else {
                    updatedPresets.add(preset)
                }
                
                _state.update { 
                    it.copy(
                        presets = updatedPresets,
                        selectedPreset = preset,
                        isLoading = false,
                        message = "Preset '${preset.name}' imported successfully"
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to import preset: ${e.message}"
                    )
                }
            }
        }
    }
}
