package com.supermarsx.carrierconfig.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.documentfile.provider.DocumentFile

/**
 * Activity result contract for picking a directory
 */
class PickDirectoryContract : ActivityResultContract<Uri?, Uri?>() {
    
    override fun createIntent(context: Context, input: Uri?): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            if (input != null) {
                putExtra("android.provider.extra.INITIAL_URI", input)
            }
        }
    }
    
    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent?.data
    }
}

/**
 * Activity result contract for picking a JSON configuration file
 */
class PickConfigFileContract : ActivityResultContract<Unit, Uri?>() {
    
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/json",
                "text/plain"
            ))
        }
    }
    
    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent?.data
    }
}

/**
 * Activity result contract for creating a new file
 */
class CreateFileContract : ActivityResultContract<String, Uri?>() {
    
    override fun createIntent(context: Context, input: String): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = when {
                input.endsWith(".json") -> "application/json"
                input.endsWith(".txt") -> "text/plain"
                input.endsWith(".xml") -> "text/xml"
                else -> "*/*"
            }
            putExtra(Intent.EXTRA_TITLE, input)
        }
    }
    
    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent?.data
    }
}

/**
 * Helper functions for working with URIs
 */
object UriHelper {
    
    /**
     * Get display path from URI
     */
    fun getDisplayPath(context: Context, uri: Uri?): String {
        if (uri == null) return ""
        
        return try {
            val documentFile = DocumentFile.fromTreeUri(context, uri)
                ?: DocumentFile.fromSingleUri(context, uri)
            
            documentFile?.name ?: uri.lastPathSegment ?: uri.toString()
        } catch (e: Exception) {
            uri.lastPathSegment ?: uri.toString()
        }
    }
    
    /**
     * Take persistable URI permission
     */
    fun takePersistablePermission(context: Context, uri: Uri) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or 
                       Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: Exception) {
            // Permission already taken or not available
        }
    }
    
    /**
     * Read text content from URI
     */
    fun readTextFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Write text content to URI
     */
    fun writeTextToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.bufferedWriter().use { it.write(content) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
