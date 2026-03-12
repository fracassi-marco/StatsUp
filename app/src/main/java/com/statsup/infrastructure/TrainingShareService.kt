package com.statsup.infrastructure

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.google.android.gms.maps.model.LatLng
import com.statsup.domain.SportTypeFormatter
import com.statsup.domain.Training
import com.statsup.domain.formatLocal
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object TrainingShareService {

    private const val WIDTH = 1080
    private const val HEIGHT = 600
    private const val PADDING = 60f
    private const val STATS_RIGHT = 420f
    private const val MAP_LEFT = 480f
    private const val MAP_RIGHT = 1020f
    private const val HEADER_BOTTOM = 180f

    private val BG_COLOR = android.graphics.Color.parseColor("#1A1A1A")
    private val PRIMARY_COLOR = android.graphics.Color.parseColor("#FF5722")
    private val TEXT_PRIMARY = android.graphics.Color.parseColor("#FFFFFF")
    private val TEXT_SECONDARY = android.graphics.Color.parseColor("#AAAAAA")
    private val DIVIDER_COLOR = android.graphics.Color.parseColor("#333333")

    fun share(context: Context, training: Training) {
        val bitmap = createBitmap(training)
        val file = saveBitmap(context, bitmap)
        val uri = FileProvider.getUriForFile(context, "com.statsup.fileprovider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    private fun createBitmap(training: Training): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = BG_COLOR
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), paint)

        // Top accent bar
        paint.color = PRIMARY_COLOR
        canvas.drawRect(0f, 0f, WIDTH.toFloat(), 6f, paint)

        // Sport emoji + training name
        val emoji = SportTypeFormatter.getEmojiForSportType(training.sportType)
        val title = if (emoji.isNotEmpty()) "$emoji  ${training.name}" else training.name
        paint.color = TEXT_PRIMARY
        paint.textSize = 52f
        paint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText(title, PADDING, PADDING + 52f, paint)

        // Date
        paint.color = TEXT_SECONDARY
        paint.textSize = 32f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(formatLocal(training.date), PADDING, PADDING + 52f + 46f, paint)

        // "StatsUp" label top-right
        paint.color = PRIMARY_COLOR
        paint.textSize = 32f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("StatsUp", WIDTH - PADDING, PADDING + 52f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Horizontal divider
        paint.color = DIVIDER_COLOR
        paint.strokeWidth = 1.5f
        canvas.drawLine(PADDING, HEADER_BOTTOM, WIDTH - PADDING, HEADER_BOTTOM, paint)

        // Vertical divider (only if route will be drawn)
        if (training.trip != null) {
            canvas.drawLine(STATS_RIGHT + 30f, HEADER_BOTTOM + 20f, STATS_RIGHT + 30f, HEIGHT - PADDING, paint)
        }

        drawStats(canvas, paint, training)

        training.trip?.let { trip ->
            val points = trip.simplifiedSteps()
            if (points.size >= 2) {
                drawRoute(canvas, paint, points)
            }
        }

        return bitmap
    }

    private fun drawStats(canvas: Canvas, paint: Paint, training: Training) {
        var y = HEADER_BOTTOM + 80f

        if (training.distanceInKilometers() > 0) {
            drawStat(
                canvas, paint,
                String.format(Locale.getDefault(), "%.2f Km", training.distanceInKilometers()),
                "DISTANZA",
                PADDING, y
            )
            y += 120f
        }

        if (training.averagePace() > 0) {
            drawStat(
                canvas, paint,
                "${formatPace(training.averagePace())} min/km",
                "PASSO MEDIO",
                PADDING, y
            )
            y += 120f
        }

        if (training.totalElevationGain > 0) {
            drawStat(
                canvas, paint,
                "+${String.format(Locale.getDefault(), "%.0f m", training.totalElevationGain)}",
                "DISLIVELLO",
                PADDING, y
            )
        }
    }

    private fun drawStat(canvas: Canvas, paint: Paint, value: String, label: String, x: Float, y: Float) {
        paint.color = TEXT_PRIMARY
        paint.textSize = 58f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(value, x, y, paint)

        paint.color = TEXT_SECONDARY
        paint.textSize = 24f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(label, x, y + 30f, paint)
    }

    private fun drawRoute(canvas: Canvas, paint: Paint, points: List<LatLng>) {
        val mapRect = RectF(MAP_LEFT, HEADER_BOTTOM + 30f, MAP_RIGHT, HEIGHT - PADDING)

        val minLat = points.minOf { it.latitude }
        val maxLat = points.maxOf { it.latitude }
        val minLng = points.minOf { it.longitude }
        val maxLng = points.maxOf { it.longitude }

        val latRange = maxLat - minLat
        val lngRange = maxLng - minLng
        if (latRange == 0.0 || lngRange == 0.0) return

        val rectW = mapRect.width()
        val rectH = mapRect.height()
        val scale = minOf(rectW / lngRange.toFloat(), rectH / latRange.toFloat()) * 0.82f

        val routeW = (lngRange * scale).toFloat()
        val routeH = (latRange * scale).toFloat()
        val offsetX = mapRect.left + (rectW - routeW) / 2f
        val offsetY = mapRect.top + (rectH - routeH) / 2f

        fun toX(lng: Double) = offsetX + ((lng - minLng) * scale).toFloat()
        fun toY(lat: Double) = offsetY + ((maxLat - lat) * scale).toFloat()

        // Glow
        paint.color = PRIMARY_COLOR
        paint.alpha = 55
        paint.strokeWidth = 14f
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        val glowPath = Path()
        glowPath.moveTo(toX(points[0].longitude), toY(points[0].latitude))
        for (i in 1 until points.size) {
            glowPath.lineTo(toX(points[i].longitude), toY(points[i].latitude))
        }
        canvas.drawPath(glowPath, paint)

        // Main line
        paint.alpha = 255
        paint.strokeWidth = 5f
        val routePath = Path()
        routePath.moveTo(toX(points[0].longitude), toY(points[0].latitude))
        for (i in 1 until points.size) {
            routePath.lineTo(toX(points[i].longitude), toY(points[i].latitude))
        }
        canvas.drawPath(routePath, paint)

        // Start dot (green) and end dot (red)
        paint.style = Paint.Style.FILL
        paint.color = android.graphics.Color.parseColor("#4CAF50")
        canvas.drawCircle(toX(points.first().longitude), toY(points.first().latitude), 12f, paint)
        paint.color = android.graphics.Color.parseColor("#F44336")
        canvas.drawCircle(toX(points.last().longitude), toY(points.last().latitude), 12f, paint)

        paint.style = Paint.Style.FILL
        paint.alpha = 255
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap): File {
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(imagesDir, "share_training.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }

    private fun formatPace(paceInMinutes: Double): String {
        val minutes = paceInMinutes.toInt()
        val seconds = ((paceInMinutes - minutes) * 60).toInt()
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}
