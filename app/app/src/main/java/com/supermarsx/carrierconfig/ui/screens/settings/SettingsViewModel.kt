package com.supermarsx.carrierconfig.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.supermarsx.carrierconfig.data.datastore.PreferencesManager
import com.supermarsx.carrierconfig.data.repository.ExportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val preferencesManager: PreferencesManager,
    private val exportRepository: ExportRepository
) : AndroidViewModel(application) {
    
    // =========================================================================
    // State
    // =========================================================================
    
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    init {
        loadPreferences()
        refreshCacheSize()
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
                
                // Calculate size before clearing
                val sizeBefore = calculateCacheSize()
                
                // Clear internal cache
                cacheDir.listFiles()?.forEach { file ->
                    file.deleteRecursively()
                }
                
                // Clear external cache
                externalCacheDir?.listFiles()?.forEach { file ->
                    file.deleteRecursively()
                }
                
                val sizeFreed = formatSize(sizeBefore)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        cacheSize = 0L,
                        message = "Cache cleared successfully ($sizeFreed freed)"
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
    
    fun refreshCacheSize() {
        viewModelScope.launch {
            val size = calculateCacheSize()
            _state.update { it.copy(cacheSize = size) }
        }
    }
    
    private fun calculateCacheSize(): Long {
        val context = getApplication<Application>().applicationContext
        val cacheDir = context.cacheDir
        val externalCacheDir = context.externalCacheDir
        
        fun getDirectorySize(dir: java.io.File?): Long {
            if (dir == null || !dir.exists()) return 0L
            
            var size = 0L
            dir.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getDirectorySize(file)
                } else {
                    file.length()
                }
            }
            return size
        }
        
        return getDirectorySize(cacheDir) + getDirectorySize(externalCacheDir)
    }
    
    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
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
                is com.supermarsx.carrierconfig.data.repository.ExportResult.Success -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            message = "Configuration exported to:\n${result.filePath}"
                        )
                    }
                }
                is com.supermarsx.carrierconfig.data.repository.ExportResult.Error -> {
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
                is com.supermarsx.carrierconfig.data.repository.ImportResult.Success -> {
                    loadPreferences() // Reload after import
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            message = "Configuration imported successfully (version ${result.version})"
                        )
                    }
                }
                is com.supermarsx.carrierconfig.data.repository.ImportResult.Error -> {
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
    
    fun importConfigurationFromUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val content = com.supermarsx.carrierconfig.util.UriHelper.readTextFromUri(context, uri)
                
                if (content == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to read configuration file"
                        )
                    }
                    return@launch
                }
                
                val result = exportRepository.importConfigurationFromString(content)
                
                when (result) {
                    is com.supermarsx.carrierconfig.data.repository.ImportResult.Success -> {
                        loadPreferences()
                        _state.update {
                            it.copy(
                                isLoading = false,
                                message = "Configuration imported successfully (version ${result.version})"
                            )
                        }
                    }
                    is com.supermarsx.carrierconfig.data.repository.ImportResult.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Import failed: ${e.message}"
                    )
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
                @Suppress("UNCHECKED_CAST")
                SettingsState(
                    autoRefresh = (values[0] as? Boolean) ?: true,
                    enableNotifications = (values[1] as? Boolean) ?: false,
                    theme = (values[2] as? String) ?: "dark",
                    glassEffectEnabled = (values[3] as? Boolean) ?: true,
                    glassStrength = (values[4] as? String) ?: "medium",
                    debugMode = (values[5] as? Boolean) ?: false,
                    exportDirectory = (values[6] as? String) ?: "",
                    autoBackup = (values[7] as? Boolean) ?: false,
                    backupFrequency = (values[8] as? String) ?: "weekly"
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
    
    // =========================================================================
    // Utility Functions
    // =========================================================================
    
    fun getFormattedCacheSize(): String {
        return formatSize(_state.value.cacheSize)
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
    val cacheSize: Long = 0L,  // Cache size in bytes
    
    // Backup & Data
    val autoBackup: Boolean = false,
    val backupFrequency: String = "weekly",
    
    // UI State
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
