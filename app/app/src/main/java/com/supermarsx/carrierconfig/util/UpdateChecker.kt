package com.supermarsx.carrierconfig.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

/**
 * Update checker for GitHub releases
 */
object UpdateChecker {
    
    private const val GITHUB_API_URL =
        "https://api.github.com/repos/supermarsx/magisk-carrier-config-override/releases/latest"
    private const val GITHUB_RELEASES_URL =
        "https://github.com/supermarsx/magisk-carrier-config-override/releases"
    
    private val json = Json {
        ignoreUnknownKeys = true
    }
    
    /**
     * Check for updates
     */
    suspend fun checkForUpdates(context: Context): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val currentVersion = getCurrentVersion(context)
            val latestRelease = fetchLatestRelease()
            
            if (latestRelease == null) {
                return@withContext UpdateCheckResult.Error("Failed to fetch release information")
            }
            
            val latestVersion = parseVersion(latestRelease.tagName)
            val current = parseVersion(currentVersion)
            
            if (latestVersion > current) {
                UpdateCheckResult.UpdateAvailable(
                    currentVersion = currentVersion,
                    latestVersion = latestRelease.tagName,
                    downloadUrl = latestRelease.assets.firstOrNull { it.name.endsWith(".apk") }?.browserDownloadUrl
                        ?: GITHUB_RELEASES_URL,
                    releaseNotes = latestRelease.body,
                    publishedAt = latestRelease.publishedAt
                )
            } else {
                UpdateCheckResult.UpToDate(currentVersion)
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error("Update check failed: ${e.message}")
        }
    }
    
    /**
     * Fetch latest release from GitHub API
     */
    private suspend fun fetchLatestRelease(): GitHubRelease? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(GITHUB_API_URL).openConnection()
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            json.decodeFromString<GitHubRelease>(response)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get current app version
     */
    private fun getCurrentVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }
    
    /**
     * Parse version string to comparable format
     */
    private fun parseVersion(version: String): Version {
        val cleanVersion = version.removePrefix("v").split("-").first()
        val parts = cleanVersion.split(".")
        return Version(
            major = parts.getOrNull(0)?.toIntOrNull() ?: 0,
            minor = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
        )
    }
    
    /**
     * Open download URL in browser
     */
    fun openDownloadPage(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Failed to open browser
        }
    }
}

/**
 * Version comparison class
 */
private data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Comparable<Version> {
    override fun compareTo(other: Version): Int {
        return when {
            major != other.major -> major.compareTo(other.major)
            minor != other.minor -> minor.compareTo(other.minor)
            else -> patch.compareTo(other.patch)
        }
    }
}

/**
 * Update check result
 */
sealed class UpdateCheckResult {
    data class UpdateAvailable(
        val currentVersion: String,
        val latestVersion: String,
        val downloadUrl: String,
        val releaseNotes: String?,
        val publishedAt: String?
    ) : UpdateCheckResult()
    
    data class UpToDate(
        val currentVersion: String
    ) : UpdateCheckResult()
    
    data class Error(
        val message: String
    ) : UpdateCheckResult()
}

/**
 * GitHub release response model
 */
@Serializable
private data class GitHubRelease(
    @SerialName("tag_name")
    val tagName: String,
    val name: String,
    val body: String? = null,
    @SerialName("published_at")
    val publishedAt: String? = null,
    val assets: List<GitHubAsset> = emptyList()
) {
    @Serializable
    data class GitHubAsset(
        val name: String,
        @SerialName("browser_download_url")
        val browserDownloadUrl: String,
        val size: Long
    )
}
