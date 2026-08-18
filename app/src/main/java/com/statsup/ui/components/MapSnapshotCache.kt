package com.statsup.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File
import kotlinx.coroutines.sync.Semaphore

object MapSnapshotCache {

    private const val TAG = "MapSnapshotCache"
    private const val DIR = "map_snapshots"

    // Serializes MapView snapshot renders: only 1 runs at a time to avoid frame drops
    // when multiple uncached items are visible simultaneously.
    val renderSemaphore = Semaphore(1)

    fun cacheFile(context: Context, trainingId: String): File {
        val dir = File(context.cacheDir, DIR)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "$trainingId.jpg")
    }

    // Only a cheap existence check: the actual decode + memory caching of the
    // bitmap is delegated to Coil (see MapListItemPreview), which avoids
    // re-decoding the full-resolution JPEG on every recomposition/scroll.
    fun exists(context: Context, trainingId: String): Boolean {
        return try {
            cacheFile(context, trainingId).exists()
        } catch (t: Throwable) {
            Log.e(TAG, "Error checking snapshot for $trainingId", t)
            false
        }
    }

    fun clearAll(context: Context) {
        try {
            val dir = File(context.cacheDir, DIR)
            dir.listFiles()?.forEach { it.delete() }
            Log.d(TAG, "All snapshots cleared")
        } catch (t: Throwable) {
            Log.e(TAG, "Error clearing all snapshots", t)
        }
    }

    fun save(context: Context, trainingId: String, bitmap: Bitmap) {
        try {
            val file = cacheFile(context, trainingId)
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            Log.d(TAG, "Snapshot saved for training $trainingId")
        } catch (t: Throwable) {
            Log.e(TAG, "Error saving snapshot for $trainingId", t)
        }
    }
}
