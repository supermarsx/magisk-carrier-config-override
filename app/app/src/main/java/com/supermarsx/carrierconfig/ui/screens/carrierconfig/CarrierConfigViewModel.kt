package com.supermarsx.carrierconfig.ui.screens.carrierconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarsx.carrierconfig.data.model.*
import com.supermarsx.carrierconfig.data.repository.CarrierConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for CarrierConfig override screen
 */
@HiltViewModel
class CarrierConfigViewModel @Inject constructor(
    private val repository: CarrierConfigRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(CarrierConfigState())
    val state: StateFlow<CarrierConfigState> = _state.asStateFlow()
    
    init {
        loadPresets()
        checkPrerequisites()
    }
    
    /**
     * Load available presets
     */
    private fun loadPresets() {
        val presets = repository.getPresets()
        _state.value = _state.value.copy(presets = presets)
    }
    
    /**
     * Check system prerequisites
     */
    fun checkPrerequisites() {
        viewModelScope.launch {
            try {
                val prerequisites = repository.checkPrerequisites()
                _state.value = _state.value.copy(prerequisites = prerequisites)
                
                // Also check deployment status if path is available
                prerequisites.carrierConfigPath?.let { path ->
                    val deployment = repository.getDeploymentStatus(path)
                    _state.value = _state.value.copy(deployment = deployment)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to check prerequisites: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Select a preset
     */
    fun selectPreset(preset: CarrierConfigPreset) {
        _state.value = _state.value.copy(selectedPreset = preset)
    }
    
    /**
     * Add a custom key
     */
    fun addCustomKey(key: ConfigKey) {
        val currentKeys = _state.value.customKeys.toMutableList()
        
        // Remove existing key with same name if present
        currentKeys.removeAll { it.key == key.key }
        currentKeys.add(key)
        
        _state.value = _state.value.copy(customKeys = currentKeys)
    }
    
    /**
     * Remove a custom key
     */
    fun removeCustomKey(key: String) {
        val updatedKeys = _state.value.customKeys.filter { it.key != key }
        _state.value = _state.value.copy(customKeys = updatedKeys)
    }
    
    /**
     * Get all selected keys (preset + custom)
     */
    fun getSelectedKeys(): List<ConfigKey> {
        val presetKeys = _state.value.selectedPreset?.keys?.map { (key, value) ->
            ConfigKey(key, value, "")
        } ?: emptyList()
        
        return presetKeys + _state.value.customKeys
    }
    
    /**
     * Generate XML preview
     */
    fun generateXMLPreview(): String {
        val keys = getSelectedKeys()
        return repository.generateXML(keys)
    }
    
    /**
     * Deploy the override
     */
    fun deploy() {
        viewModelScope.launch {
            val prerequisites = _state.value.prerequisites
            if (prerequisites == null || !prerequisites.allMet) {
                _state.value = _state.value.copy(
                    error = "Prerequisites not met. Please check root access and Magisk installation."
                )
                return@launch
            }
            
            val selectedPreset = _state.value.selectedPreset
            if (selectedPreset == null) {
                _state.value = _state.value.copy(
                    error = "Please select a preset first"
                )
                return@launch
            }
            
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val result = repository.deployOverride(
                    preset = selectedPreset,
                    customKeys = _state.value.customKeys,
                    targetPath = prerequisites.carrierConfigPath!!
                )
                
                when (result) {
                    is DeploymentResult.Success -> {
                        // Refresh deployment status
                        val deployment = repository.getDeploymentStatus(prerequisites.carrierConfigPath)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            deployment = deployment.copy(
                                deployedPresetId = selectedPreset.id,
                                deployedKeys = getSelectedKeys(),
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    is DeploymentResult.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    DeploymentResult.PrerequisitesNotMet -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Prerequisites check failed"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Deployment failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Revert the override
     */
    fun revert() {
        viewModelScope.launch {
            val prerequisites = _state.value.prerequisites
            if (prerequisites?.carrierConfigPath == null) {
                _state.value = _state.value.copy(
                    error = "Cannot determine CarrierConfig path"
                )
                return@launch
            }
            
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val result = repository.revertOverride(prerequisites.carrierConfigPath)
                
                when (result) {
                    is DeploymentResult.Success -> {
                        val deployment = repository.getDeploymentStatus(prerequisites.carrierConfigPath)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            deployment = deployment
                        )
                    }
                    is DeploymentResult.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    DeploymentResult.PrerequisitesNotMet -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Cannot revert: prerequisites not met"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Revert failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Switch tab
     */
    fun switchTab(tabIndex: Int) {
        _state.value = _state.value.copy(currentTab = tabIndex)
    }
    
    /**
     * Clear error
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
