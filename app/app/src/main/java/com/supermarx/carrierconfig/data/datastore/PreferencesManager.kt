package dev.mars.carrierconfig.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extension property to create DataStore instance
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cco_preferences")

/**
 * Manager for app preferences using DataStore
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val dataStore = context.dataStore
    
    companion object {
        // General Settings
        private val AUTO_REFRESH = booleanPreferencesKey("auto_refresh")
        private val ENABLE_NOTIFICATIONS = booleanPreferencesKey("enable_notifications")
        
        // Appearance
        private val THEME = stringPreferencesKey("theme")
        private val GLASS_EFFECT_ENABLED = booleanPreferencesKey("glass_effect_enabled")
        private val GLASS_STRENGTH = stringPreferencesKey("glass_strength")
        
        // Advanced
        private val DEBUG_MODE = booleanPreferencesKey("debug_mode")
        private val EXPORT_DIRECTORY = stringPreferencesKey("export_directory")
        
        // Backup & Data
        private val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
        private val BACKUP_FREQUENCY = stringPreferencesKey("backup_frequency")
        
        // First run
        private val FIRST_RUN = booleanPreferencesKey("first_run")
        
        // Last backup
        private val LAST_BACKUP_TIMESTAMP = longPreferencesKey("last_backup_timestamp")
    }
    
    // =========================================================================
    // General Settings
    // =========================================================================
    
    val autoRefresh: Flow<Boolean> = dataStore.data
        .catch { handleException(it) }
        .map { it[AUTO_REFRESH] ?: true }
    
    suspend fun setAutoRefresh(enabled: Boolean) {
        dataStore.edit { it[AUTO_REFRESH] = enabled }
    }
    
    val enableNotifications: Flow<Boolean> = dataStore.data
        .catch { handleException(it) }
        .map { it[ENABLE_NOTIFICATIONS] ?: false }
    
    suspend fun setEnableNotifications(enabled: Boolean) {
        dataStore.edit { it[ENABLE_NOTIFICATIONS] = enabled }
    }
    
    // =========================================================================
    // Appearance
    // =========================================================================
    
    val theme: Flow<String> = dataStore.data
        .catch { handleException(it) }
        .map { it[THEME] ?: "dark" }
    
    suspend fun setTheme(theme: String) {
        dataStore.edit { it[THEME] = theme }
    }
    
    val glassEffectEnabled: Flow<Boolean> = dataStore.data
        .catch { handleException(it) }
        .map { it[GLASS_EFFECT_ENABLED] ?: true }
    
    suspend fun setGlassEffectEnabled(enabled: Boolean) {
        dataStore.edit { it[GLASS_EFFECT_ENABLED] = enabled }
    }
    
    val glassStrength: Flow<String> = dataStore.data
        .catch { handleException(it) }
        .map { it[GLASS_STRENGTH] ?: "medium" }
    
    suspend fun setGlassStrength(strength: String) {
        dataStore.edit { it[GLASS_STRENGTH] = strength }
    }
    
    // =========================================================================
    // Advanced
    // =========================================================================
    
    val debugMode: Flow<Boolean> = dataStore.data
        .catch { handleException(it) }
        .map { it[DEBUG_MODE] ?: false }
    
    suspend fun setDebugMode(enabled: Boolean) {
        dataStore.edit { it[DEBUG_MODE] = enabled }
    }
    
    val exportDirectory: Flow<String> = dataStore.data
        .catch { handleException(it) }
        .map { it[EXPORT_DIRECTORY] ?: "" }
    
    suspend fun setExportDirectory(directory: String) {
        dataStore.edit { it[EXPORT_DIRECTORY] = directory }
    }
    
    // =========================================================================
    // Backup & Data
    // =========================================================================
    
    val autoBackup: Flow<Boolean> = dataStore.data
        .catch { handleException(it) }
        .map { it[AUTO_BACKUP] ?: false }
    
    suspend fun setAutoBackup(enabled: Boolean) {
        dataStore.edit { it[AUTO_BACKUP] = enabled }
    }
    
    val backupFrequency: Flow<String> = dataStore.data
        .catch { handleException(it) }
        .map { it[BACKUP_FREQUENCY] ?: "weekly" }
    
    suspend fun setBackupFrequency(frequency: String) {
        dataStore.edit { it[BACKUP_FREQUENCY] = frequency }
    }
    
    val lastBackupTimestamp: Flow<Long> = dataStore.data
        .catch { handleException(it) }
        .map { it[LAST_BACKUP_TIMESTAMP] ?: 0L }
    
    suspend fun setLastBackupTimestamp(timestamp: Long) {
        dataStore.edit { it[LAST_BACKUP_TIMESTAMP] = timestamp }
    }
    
    // =========================================================================
    // First Run
    // =========================================================================
    
    val isFirstRun: Flow<Boolean> = dataStore.data
        .catch { handleException(it) }
        .map { it[FIRST_RUN] ?: true }
    
    suspend fun setFirstRunComplete() {
        dataStore.edit { it[FIRST_RUN] = false }
    }
    
    // =========================================================================
    // Utility Methods
    // =========================================================================
    
    /**
     * Clear all preferences
     */
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
    
    /**
     * Reset to default values
     */
    suspend fun resetToDefaults() {
        dataStore.edit { prefs ->
            prefs.clear()
            // Set defaults
            prefs[AUTO_REFRESH] = true
            prefs[ENABLE_NOTIFICATIONS] = false
            prefs[THEME] = "dark"
            prefs[GLASS_EFFECT_ENABLED] = true
            prefs[GLASS_STRENGTH] = "medium"
            prefs[DEBUG_MODE] = false
            prefs[AUTO_BACKUP] = false
            prefs[BACKUP_FREQUENCY] = "weekly"
            prefs[FIRST_RUN] = false
        }
    }
    
    /**
     * Get all preferences as a map
     */
    fun getAllPreferences(): Flow<Map<String, Any>> = dataStore.data
        .catch { handleException(it) }
        .map { prefs ->
            prefs.asMap().mapKeys { it.key.name }
                .mapValues { it.value ?: "" }
        }
    
    /**
     * Handle DataStore exceptions
     */
    private suspend fun <T> handleException(exception: Throwable): T {
        if (exception is IOException) {
            // Log error
            throw exception
        } else {
            throw exception
        }
    }
}

/**
 * App preferences data class
 */
data class AppPreferences(
    val autoRefresh: Boolean = true,
    val enableNotifications: Boolean = false,
    val theme: String = "dark",
    val glassEffectEnabled: Boolean = true,
    val glassStrength: String = "medium",
    val debugMode: Boolean = false,
    val exportDirectory: String = "",
    val autoBackup: Boolean = false,
    val backupFrequency: String = "weekly",
    val isFirstRun: Boolean = true
)
