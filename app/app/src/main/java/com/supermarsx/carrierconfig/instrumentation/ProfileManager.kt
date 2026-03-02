package com.supermarsx.carrierconfig.instrumentation

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hook Profile Manager
 * 
 * Manages instrumentation profiles for different device/carrier combinations
 */
@Singleton
class ProfileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "ProfileManager"
        private val PROFILE_PATH_CANDIDATES = listOf(
            "shared/profiles.json",
            "instrumentation/shared/profiles.json"
        )
    }
    
    data class ProfileDatabase(
        val profiles: List<HookProfile>,
        val metadata: Metadata
    )
    
    data class HookProfile(
        val id: String,
        val name: String,
        val description: String,
        @SerializedName("oneui_versions")
        val oneuiVersions: List<String>,
        @SerializedName("android_versions")
        val androidVersions: List<String>,
        val carriers: List<String>? = null,
        val targets: List<HookTarget>,
        @SerializedName("carrier_config_overrides")
        val carrierConfigOverrides: Map<String, Any>? = null,
        @SerializedName("settings_overrides")
        val settingsOverrides: Map<String, Int>? = null,
        val note: String? = null
    )
    
    data class HookTarget(
        val `package`: String,
        val `class`: String,
        val method: String,
        val signature: String,
        @SerializedName("return_value")
        val returnValue: Any,
        val description: String
    )
    
    data class Metadata(
        val version: String,
        @SerializedName("last_updated")
        val lastUpdated: String,
        @SerializedName("schema_version")
        val schemaVersion: String
    )
    
    @Volatile
    private var cachedDatabase: ProfileDatabase? = null
    
    /**
     * Load all profiles
     */
    suspend fun loadProfiles(): ProfileDatabase = withContext(Dispatchers.IO) {
        if (cachedDatabase != null) {
            return@withContext cachedDatabase!!
        }
        
        try {
            val json = openProfilesAsset().bufferedReader().use {
                it.readText()
            }
            
            val type = object : TypeToken<ProfileDatabase>() {}.type
            val database = gson.fromJson<ProfileDatabase>(json, type)
            
            cachedDatabase = database
            Timber.tag(TAG).d("Loaded ${database.profiles.size} profiles")
            
            database
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to load profiles")
            // Return default profile
            ProfileDatabase(
                profiles = listOf(getDefaultProfile()),
                metadata = Metadata("1.0.0", "2026-02-05", "1.0")
            )
        }
    }
    
    /**
     * Get profile by ID
     */
    suspend fun getProfile(profileId: String): HookProfile? {
        val database = loadProfiles()
        return database.profiles.find { it.id == profileId }
    }
    
    /**
     * Find suitable profile for device
     */
    suspend fun findProfileForDevice(
        oneuiVersion: String?,
        androidVersion: String?,
        carrier: String? = null
    ): HookProfile? {
        val database = loadProfiles()
        
        // Try exact match first
        var profile = database.profiles.find { p ->
            (oneuiVersion in p.oneuiVersions || "*" in p.oneuiVersions) &&
            (androidVersion in p.androidVersions || "*" in p.androidVersions) &&
            (carrier == null || p.carriers == null || carrier in p.carriers)
        }
        
        // Fallback to generic profile
        if (profile == null) {
            profile = database.profiles.find { it.id == "oneui6_generic" }
        }
        
        Timber.tag(TAG).d("Selected profile: ${profile?.id}")
        return profile
    }
    
    /**
     * Save custom profile (from recording)
     */
    suspend fun saveCustomProfile(profile: HookProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // In a real implementation, this would save to persistent storage
            // For now, just cache it
            val database = loadProfiles()
            val updated = database.copy(
                profiles = database.profiles.filter { it.id != profile.id } + profile
            )
            cachedDatabase = updated
            
            Timber.tag(TAG).i("Saved custom profile: ${profile.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to save profile")
            Result.failure(e)
        }
    }
    
    /**
     * Export profile to JSON
     */
    fun exportProfile(profile: HookProfile): String {
        return gson.toJson(profile)
    }
    
    /**
     * Import profile from JSON
     */
    fun importProfile(json: String): HookProfile {
        return gson.fromJson(json, HookProfile::class.java)
    }
    
    /**
     * Get default fallback profile
     */
    private fun getDefaultProfile(): HookProfile {
        return HookProfile(
            id = "generic_fallback",
            name = "Generic Fallback",
            description = "Basic hooks for unknown devices",
            oneuiVersions = listOf("*"),
            androidVersions = listOf("*"),
            targets = listOf(
                HookTarget(
                    `package` = "com.sec.imsservice",
                    `class` = "com.sec.ims.ImsManager",
                    method = "isWfcEntitled",
                    signature = "()Z",
                    returnValue = true,
                    description = "Primary WFC entitlement"
                )
            )
        )
    }

    private fun openProfilesAsset(): InputStream {
        for (path in PROFILE_PATH_CANDIDATES) {
            try {
                return context.assets.open(path)
            } catch (_: Exception) {
                // Try next path
            }
        }
        throw IllegalStateException("profiles.json asset not found")
    }
}
