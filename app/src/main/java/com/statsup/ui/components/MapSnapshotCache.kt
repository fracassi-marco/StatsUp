package com.statsup.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File

object MapSnapshotCache {

    private const val TAG = "MapSnapshotCache"
    private const val DIR = "map_snapshots"

    private fun cacheFile(context: Context, trainingId: String): File {
        val dir = File(context.cacheDir, DIR)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "$trainingId.jpg")
    }

    fun load(context: Context, trainingId: String): Bitmap? {
        return try {
            val file = cacheFile(context, trainingId)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        } catch (t: Throwable) {
            Log.e(TAG, "Error loading snapshot for $trainingId", t)
            null
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
