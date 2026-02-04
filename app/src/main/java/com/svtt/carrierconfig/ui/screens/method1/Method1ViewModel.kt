package com.svtt.carrierconfig.ui.screens.method1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svtt.carrierconfig.data.model.*
import com.svtt.carrierconfig.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class Method1ViewModel @Inject constructor(
    private val presetRepository: PresetRepository,
    private val deviceInfoRepository: DeviceInfoRepository,
    private val magiskRepository: MagiskRepository,
    private val overrideXmlBuilder: OverrideXmlBuilder
) : ViewModel() {
    
    private val _state = MutableStateFlow(Method1State())
    val state: StateFlow<Method1State> = _state.asStateFlow()
    
    private val _selectedKeys = MutableStateFlow<List<CarrierConfigKey>>(emptyList())
    val selectedKeys: StateFlow<List<CarrierConfigKey>> = _selectedKeys.asStateFlow()
    
    init {
        loadMethod1Data()
    }
    
    private fun loadMethod1Data() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                
                // Load presets
                val presets = presetRepository.getPresets()
                _state.value = _state.value.copy(presets = presets)
                
                // Check root
                val deviceInfo = deviceInfoRepository.getDeviceInfo()
                val isRooted = deviceInfo.isRooted
                _state.value = _state.value.copy(isRootAvailable = isRooted)
                
                if (isRooted) {
                    // Check Magisk
                    val isMagiskInstalled = magiskRepository.isMagiskInstalled()
                    _state.value = _state.value.copy(isMagiskInstalled = isMagiskInstalled)
                    
                    // Detect paths
                    val detectedPaths = magiskRepository.getAllDetectedPaths()
                    _state.value = _state.value.copy(detectedPaths = detectedPaths)
                    
                    // Check if override is active
                    val isActive = magiskRepository.isOverrideActive()
                    if (isActive) {
                        _state.value = _state.value.copy(activeOverride = "Active")
                    }
                }
                
                _state.value = _state.value.copy(isLoading = false)
                Timber.d("Method 1 data loaded")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load Method 1 data")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load: ${e.message}"
                )
            }
        }
    }
    
    fun selectPreset(preset: CarrierConfigPreset) {
        _state.value = _state.value.copy(selectedPreset = preset)
        _selectedKeys.value = preset.keys
        Timber.d("Selected preset: ${preset.name}")
    }
    
    fun addCustomKey(key: CarrierConfigKey) {
        val current = _state.value.customKeys.toMutableList()
        current.add(key)
        _state.value = _state.value.copy(customKeys = current)
        
        // Update selected keys
        val selected = _selectedKeys.value.toMutableList()
        selected.add(key)
        _selectedKeys.value = selected
        
        Timber.d("Added custom key: ${key.key}")
    }
    
    fun removeCustomKey(key: CarrierConfigKey) {
        val current = _state.value.customKeys.toMutableList()
        current.remove(key)
        _state.value = _state.value.copy(customKeys = current)
        
        // Update selected keys
        val selected = _selectedKeys.value.toMutableList()
        selected.remove(key)
        _selectedKeys.value = selected
        
        Timber.d("Removed custom key: ${key.key}")
    }
    
    fun updateKeyValue(key: CarrierConfigKey, newValue: Any) {
        val updated = key.copy(value = newValue)
        
        // Update in custom keys
        val customKeys = _state.value.customKeys.toMutableList()
        val index = customKeys.indexOfFirst { it.key == key.key }
        if (index != -1) {
            customKeys[index] = updated
            _state.value = _state.value.copy(customKeys = customKeys)
        }
        
        // Update in selected keys
        val selectedKeys = _selectedKeys.value.toMutableList()
        val selectedIndex = selectedKeys.indexOfFirst { it.key == key.key }
        if (selectedIndex != -1) {
            selectedKeys[selectedIndex] = updated
            _selectedKeys.value = selectedKeys
        }
        
        Timber.d("Updated key ${key.key} value to $newValue")
    }
    
    fun clearSelection() {
        _state.value = _state.value.copy(selectedPreset = null)
        _selectedKeys.value = emptyList()
        Timber.d("Cleared selection")
    }
    
    suspend fun deployOverride(config: DeploymentConfig): DeploymentResult {
        return try {
            Timber.d("Deploying override...")
            
            if (!_state.value.isRootAvailable) {
                return DeploymentResult(
                    success = false,
                    message = "Root access is required"
                )
            }
            
            if (_selectedKeys.value.isEmpty()) {
                return DeploymentResult(
                    success = false,
                    message = "No keys selected for deployment"
                )
            }
            
            // Build XML
            val xml = overrideXmlBuilder.buildXml(_selectedKeys.value)
            
            // Validate XML
            if (!overrideXmlBuilder.validateXml(xml)) {
                return DeploymentResult(
                    success = false,
                    message = "Generated XML is invalid"
                )
            }
            
            // Create data directories
            if (!magiskRepository.createDataDirectories()) {
                return DeploymentResult(
                    success = false,
                    message = "Failed to create data directories"
                )
            }
            
            // Save to app storage
            val savedFile = magiskRepository.saveOverrideToAppStorage(xml)
            if (savedFile == null) {
                return DeploymentResult(
                    success = false,
                    message = "Failed to save override file"
                )
            }
            
            // Copy to SVTT data directory
            if (!magiskRepository.copyOverrideToSvttData(savedFile)) {
                return DeploymentResult(
                    success = false,
                    message = "Failed to copy override to data directory"
                )
            }
            
            _state.value = _state.value.copy(activeOverride = "Active")
            
            DeploymentResult(
                success = true,
                message = "Override deployed successfully! Reboot required for changes to take effect.",
                deployedPath = "/data/adb/svtt/active/override.xml"
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to deploy override")
            DeploymentResult(
                success = false,
                message = "Deployment failed: ${e.message}"
            )
        }
    }
    
    suspend fun revertOverride(): Boolean {
        return try {
            Timber.d("Reverting override...")
            
            if (!_state.value.isRootAvailable) {
                return false
            }
            
            val success = magiskRepository.removeOverride()
            
            if (success) {
                _state.value = _state.value.copy(activeOverride = null)
                Timber.d("Override reverted successfully")
            }
            
            success
        } catch (e: Exception) {
            Timber.e(e, "Failed to revert override")
            false
        }
    }
    
    fun refresh() {
        loadMethod1Data()
    }
}
