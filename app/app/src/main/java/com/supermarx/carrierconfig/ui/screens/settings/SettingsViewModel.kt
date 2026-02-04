package com.supermarx.carrierconfig.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supermarx.carrierconfig.data.datastore.PreferencesManager
import com.supermarx.carrierconfig.data.repository.ExportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val exportRepository: ExportRepository
) : ViewModel() {
    
    // =========================================================================
    // State
    // =========================================================================
    
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    init {
        loadPreferences()
    }
    
    // =========================================================================
    // General Settings
    // =========================================================================
    
    fun setAutoRefresh(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoRefresh(enabled)
            _state.update { it.copy(autoRefresh = enabled) }
        }
    }
    
    fun setEnableNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setEnableNotifications(enabled)
            _state.update { it.copy(enableNotifications = enabled) }
        }
    }
    
    fun showMessage(message: String) {
        _state.update { it.copy(message = message) }
    }
    
    // =========================================================================
    // Appearance
    // =========================================================================
    
    fun setTheme(theme: String) {
        viewModelScope.launch {
            preferencesManager.setTheme(theme)
            _state.update { it.copy(theme = theme) }
        }
    }
    
    fun setGlassEffectEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setGlassEffectEnabled(enabled)
            _state.update { it.copy(glassEffectEnabled = enabled) }
        }
    }
    
    fun setGlassStrength(strength: String) {
        viewModelScope.launch {
            preferencesManager.setGlassStrength(strength)
            _state.update { it.copy(glassStrength = strength) }
        }
    }
    
    // =========================================================================
    // Advanced
    // =========================================================================
    
    fun setDebugMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDebugMode(enabled)
            _state.update { it.copy(debugMode = enabled) }
        }
    }
    
    fun setExportDirectory(directory: String) {
        viewModelScope.launch {
            preferencesManager.setExportDirectory(directory)
            _state.update { it.copy(exportDirectory = directory) }
        }
    }
    
    fun clearCache() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val context = getApplication<Application>().applicationContext
                val cacheDir = context.cacheDir
                val externalCacheDir = context.externalCacheDir
                
                // Clear internal cache
                cacheDir.listFiles()?.forEach { file ->
                    file.deleteRecursively()
                }
                
                // Clear external cache
                externalCacheDir?.listFiles()?.forEach { file ->
                    file.deleteRecursively()
                }
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        message = "Cache cleared successfully (${cacheDir.totalSpace / 1024 / 1024} MB freed)"
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        message = "Failed to clear cache: ${e.message}"
                    )
                }
            }
        }
    }
    
    // =========================================================================
    // Backup & Data
    // =========================================================================
    
    fun setAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoBackup(enabled)
            _state.update { it.copy(autoBackup = enabled) }
        }
    }
    
    fun setBackupFrequency(frequency: String) {
        viewModelScope.launch {
            preferencesManager.setBackupFrequency(frequency)
            _state.update { it.copy(backupFrequency = frequency) }
        }
    }
    
    fun exportConfiguration() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = exportRepository.exportConfiguration(
                includePresets = true,
                includeSettings = true
            )
            
            when (result) {
                is com.supermarx.carrierconfig.data.repository.ExportResult.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            message = "Configuration exported to:\n${result.filePath}"
                        )
                    }
                }
                is com.supermarx.carrierconfig.data.repository.ExportResult.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    fun importConfiguration(filePath: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = exportRepository.importConfiguration(filePath)
            
            when (result) {
                is com.supermarx.carrierconfig.data.repository.ImportResult.Success -> {
                    loadPreferences() // Reload after import
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            message = "Configuration imported successfully (version ${result.version})"
                        )
                    }
                }
                is com.supermarx.carrierconfig.data.repository.ImportResult.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    // =========================================================================
    // Danger Zone
    // =========================================================================
    
    fun resetSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            preferencesManager.resetToDefaults()
            loadPreferences()
            _state.update { 
                it.copy(
                    isLoading = false,
                    message = "Settings reset to defaults"
                )
            }
        }
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val context = getApplication<Application>().applicationContext
                
                // Clear all app data
                context.cacheDir.deleteRecursively()
                context.externalCacheDir?.deleteRecursively()
                
                // Clear app files
                context.filesDir.listFiles()?.forEach { file ->
                    if (!file.name.contains("datastore")) {
                        file.deleteRecursively()
                    }
                }
                
                // Reset preferences
                preferencesManager.clearAll()
                
                // Reload preferences
                loadPreferences()
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        message = "All data cleared and reset to defaults"
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        message = "Failed to clear data: ${e.message}"
                    )
                }
            }
        }
    }
    
    // =========================================================================
    // Utility
    // =========================================================================
    
    fun clearMessage() {
        _state.update { it.copy(message = null, error = null) }
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            combine(
                preferencesManager.autoRefresh,
                preferencesManager.enableNotifications,
                preferencesManager.theme,
                preferencesManager.glassEffectEnabled,
                preferencesManager.glassStrength,
                preferencesManager.debugMode,
                preferencesManager.exportDirectory,
                preferencesManager.autoBackup,
                preferencesManager.backupFrequency
            ) { values ->
                SettingsState(
                    autoRefresh = values[0] as Boolean,
                    enableNotifications = values[1] as Boolean,
                    theme = values[2] as String,
                    glassEffectEnabled = values[3] as Boolean,
                    glassStrength = values[4] as String,
                    debugMode = values[5] as Boolean,
                    exportDirectory = values[6] as String,
                    autoBackup = values[7] as Boolean,
                    backupFrequency = values[8] as String
                )
            }.collect { newState ->
                _state.update { it.copy(
                    autoRefresh = newState.autoRefresh,
                    enableNotifications = newState.enableNotifications,
                    theme = newState.theme,
                    glassEffectEnabled = newState.glassEffectEnabled,
                    glassStrength = newState.glassStrength,
                    debugMode = newState.debugMode,
                    exportDirectory = newState.exportDirectory,
                    autoBackup = newState.autoBackup,
                    backupFrequency = newState.backupFrequency
                ) }
            }
        }
    }
}

/**
 * Settings screen state
 */
data class SettingsState(
    // General
    val autoRefresh: Boolean = true,
    val enableNotifications: Boolean = false,
    
    // Appearance
    val theme: String = "dark",
    val glassEffectEnabled: Boolean = true,
    val glassStrength: String = "medium",
    
    // Advanced
    val debugMode: Boolean = false,
    val exportDirectory: String = "",
    
    // Backup & Data
    val autoBackup: Boolean = false,
    val backupFrequency: String = "weekly",
    
    // UI State
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
