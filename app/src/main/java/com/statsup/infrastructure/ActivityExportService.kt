package com.statsup.infrastructure

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.google.android.gms.maps.model.LatLng
import com.statsup.domain.Training
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object ActivityExportService {

    private val ISO_UTC = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    suspend fun exportGpx(context: Context, training: Training) = withContext(Dispatchers.IO) {
        val file = buildGpxFile(context, training)
        shareFile(context, file, "application/gpx+xml")
    }

    suspend fun exportTcx(context: Context, training: Training) = withContext(Dispatchers.IO) {
        val file = buildTcxFile(context, training)
        shareFile(context, file, "application/vnd.garmin.tcx+xml")
    }

    // ── GPX ─────────────────────────────────────────────────────────────────

    private fun buildGpxFile(context: Context, training: Training): File {
        val points = training.trip?.steps() ?: emptyList()
        val timestamps = distributeTimestamps(training.date, training.movingTime, points)

        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine(
            """<gpx xmlns="http://www.topografix.com/GPX/1/1" """ +
            """xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" """ +
            """version="1.1" creator="StatsUp">"""
        )
        sb.appendLine("  <metadata>")
        sb.appendLine("    <name>${escapeXml(training.name)}</name>")
        sb.appendLine("    <time>${training.date.format(ISO_UTC)}</time>")
        sb.appendLine("  </metadata>")
        sb.appendLine("  <trk>")
        sb.appendLine("    <name>${escapeXml(training.name)}</name>")
        sb.appendLine("    <type>${gpxType(training.sportType)}</type>")
        sb.appendLine("    <trkseg>")
        points.forEachIndexed { i, pt ->
            sb.appendLine("""      <trkpt lat="${pt.latitude}" lon="${pt.longitude}">""")
            sb.appendLine("        <time>${timestamps[i]}</time>")
            sb.appendLine("      </trkpt>")
        }
        sb.appendLine("    </trkseg>")
        sb.appendLine("  </trk>")
        sb.appendLine("</gpx>")

        return writeToCache(context, "activity_${training.id}.gpx", sb.toString())
    }

    // ── TCX ─────────────────────────────────────────────────────────────────

    private fun buildTcxFile(context: Context, training: Training): File {
        val points = training.trip?.steps() ?: emptyList()
        val timestamps = distributeTimestamps(training.date, training.movingTime, points)
        val startIso = training.date.format(ISO_UTC)
        val sport = tcxSport(training.sportType)

        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine(
            """<TrainingCenterDatabase """ +
            """xmlns="http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2" """ +
            """xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">"""
        )
        sb.appendLine("  <Activities>")
        sb.appendLine("""    <Activity Sport="$sport">""")
        sb.appendLine("      <Id>$startIso</Id>")
        sb.appendLine("""      <Lap StartTime="$startIso">""")
        sb.appendLine("        <TotalTimeSeconds>${training.movingTime}</TotalTimeSeconds>")
        sb.appendLine("        <DistanceMeters>${training.distance}</DistanceMeters>")
        if (training.maxSpeed > 0) {
            sb.appendLine("        <MaximumSpeed>${training.maxSpeed}</MaximumSpeed>")
        }
        sb.appendLine("        <Calories>0</Calories>")
        if (training.averageHeartrate != null && training.averageHeartrate!! > 0) {
            sb.appendLine("        <AverageHeartRateBpm><Value>${training.averageHeartrate!!.toInt()}</Value></AverageHeartRateBpm>")
        }
        if (training.maxHeartrate > 0) {
            sb.appendLine("        <MaximumHeartRateBpm><Value>${training.maxHeartrate.toInt()}</Value></MaximumHeartRateBpm>")
        }
        sb.appendLine("        <Intensity>Active</Intensity>")
        sb.appendLine("        <TriggerMethod>Manual</TriggerMethod>")
        if (points.isNotEmpty()) {
            sb.appendLine("        <Track>")
            points.forEachIndexed { i, pt ->
                sb.appendLine("          <Trackpoint>")
                sb.appendLine("            <Time>${timestamps[i]}</Time>")
                sb.appendLine("            <Position>")
                sb.appendLine("              <LatitudeDegrees>${pt.latitude}</LatitudeDegrees>")
                sb.appendLine("              <LongitudeDegrees>${pt.longitude}</LongitudeDegrees>")
                sb.appendLine("            </Position>")
                sb.appendLine("          </Trackpoint>")
            }
            sb.appendLine("        </Track>")
        }
        sb.appendLine("      </Lap>")
        sb.appendLine("    </Activity>")
        sb.appendLine("  </Activities>")
        sb.appendLine("</TrainingCenterDatabase>")

        return writeToCache(context, "activity_${training.id}.tcx", sb.toString())
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Distributes [movingTime] seconds across [points] proportionally to cumulative distance,
     * so each trackpoint gets an estimated timestamp.
     */
    private fun distributeTimestamps(
        start: ZonedDateTime,
        movingTime: Int,
        points: List<LatLng>
    ): List<String> {
        if (points.isEmpty()) return emptyList()
        if (points.size == 1) return listOf(start.format(ISO_UTC))

        val distances = DoubleArray(points.size) { 0.0 }
        for (i in 1 until points.size) {
            distances[i] = distances[i - 1] + haversine(
                points[i - 1].latitude, points[i - 1].longitude,
                points[i].latitude, points[i].longitude
            )
        }
        val totalDist = distances.last().takeIf { it > 0 } ?: 1.0

        return distances.map { d ->
            val offsetSeconds = (d / totalDist * movingTime).toLong()
            start.plusSeconds(offsetSeconds).format(ISO_UTC)
        }
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(a))
    }

    private fun gpxType(sportType: String?) = when (sportType?.lowercase()) {
        "run", "running" -> "running"
        "ride", "cycling", "virtualride" -> "cycling"
        "hike", "hiking" -> "hiking"
        "walk", "walking" -> "walking"
        "swim", "swimming" -> "swimming"
        else -> "other"
    }

    private fun tcxSport(sportType: String?) = when (sportType?.lowercase()) {
        "run", "running" -> "Running"
        "ride", "cycling", "virtualride" -> "Biking"
        "swim", "swimming" -> "Other"
        else -> "Other"
    }

    private fun escapeXml(text: String) = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private fun writeToCache(context: Context, fileName: String, content: String): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        return File(dir, fileName).also { it.writeText(content, Charsets.UTF_8) }
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "com.statsup.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }
}
