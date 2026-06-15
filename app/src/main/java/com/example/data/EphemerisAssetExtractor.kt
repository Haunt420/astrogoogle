package com.example.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException

object EphemerisAssetExtractor {

    private const val TAG = "EphemerisAssetExtractor"
    private const val EPHE_ASSET_DIR = "ephe"
    private const val VERSION_FILE = ".ephe_version"
    private const val CURRENT_VERSION = "1"

    /**
     * Ensures all .se1 files are extracted to the internal ephemeris directory.
     * Safe to call multiple times. Gracefully returns empty string if no asset files exist,
     * so that the Swiss Ephemeris engine falls back to built-in Moshier analytical mode.
     */
    fun ensureExtracted(context: Context): String {
        val epheDir = File(context.filesDir, EPHE_ASSET_DIR)
        val versionFile = File(epheDir, VERSION_FILE)

        // If already extracted, return path
        if (epheDir.exists() && versionFile.exists()) {
            try {
                if (versionFile.readText().trim() == CURRENT_VERSION) {
                    return epheDir.absolutePath
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading version, re-extracting: ${e.message}")
            }
        }

        epheDir.mkdirs()

        try {
            val assets = context.assets.list(EPHE_ASSET_DIR) ?: emptyArray()
            if (assets.isEmpty()) {
                Log.i(TAG, "No ephemeris files found in assets/ephe ($EPHE_ASSET_DIR)")
                return ""
            }

            for (fileName in assets) {
                if (fileName == VERSION_FILE) continue
                val outFile = File(epheDir, fileName)
                context.assets.open("$EPHE_ASSET_DIR/$fileName").use { input ->
                    outFile.outputStream().use { output -> input.copyTo(output) }
                }
                Log.d(TAG, "Extracted asset: $fileName")
            }

            versionFile.writeText(CURRENT_VERSION)
            return epheDir.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy ephemeris assets, using Moshier fallback: ${e.message}")
            return ""
        }
    }
}
