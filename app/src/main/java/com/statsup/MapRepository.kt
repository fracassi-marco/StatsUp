package com.statsup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

object MapRepository {

    suspend fun ofActivity(context: Context, activity: Activity): Bitmap? {
        if (activity.map.isNullOrBlank()) {
            return null
        }

        return withContext(Dispatchers.IO) {
            val imgFile = File(context.getExternalFilesDir(null), "activity_${activity.id}.png")
            if (!imgFile.exists()) {
                download(Confs(context), activity, imgFile)
            }

            BitmapFactory.decodeFile(imgFile.getAbsolutePath())
        }
    }

    private fun download(confs: Confs, activity: Activity, imgFile: File) {
        URL("https://maps.googleapis.com/maps/api/staticmap?size=600x300&maptype=roadmap&key=${confs.mapsKey}&path=enc:${activity.map}")
            .openConnection().getInputStream().use { input ->
                imgFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

    }
}
