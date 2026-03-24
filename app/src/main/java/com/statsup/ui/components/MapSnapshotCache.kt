package com.statsup.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File

object MapSnapshotCache {

    private const val TAG = "MapSnapshotCache"
    private const val DIR = "map_snapshots"

    private fun cacheFile(context: Context, trainingId: Long): File {
        val dir = File(context.cacheDir, DIR)
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "$trainingId.png")
    }

    fun load(context: Context, trainingId: Long): Bitmap? {
        return try {
            val file = cacheFile(context, trainingId)
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        } catch (t: Throwable) {
            Log.e(TAG, "Error loading snapshot for $trainingId", t)
            null
        }
    }

    fun save(context: Context, trainingId: Long, bitmap: Bitmap) {
        try {
            val file = cacheFile(context, trainingId)
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            Log.d(TAG, "Snapshot saved for training $trainingId")
        } catch (t: Throwable) {
            Log.e(TAG, "Error saving snapshot for $trainingId", t)
        }
    }
}
