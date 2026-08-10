package com.statsup.infrastructure.repository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.google.android.gms.maps.model.LatLng
import com.statsup.domain.repository.Peak
import com.statsup.domain.repository.PeakLookupException
import com.statsup.domain.repository.PeakLookupRepository
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import topinambur.Http

private val jsonMapper = jsonMapper { addModule(kotlinModule()) }

private const val SEARCH_RADIUS_METERS = 1500

fun parseOverpassPeaks(json: String): List<Peak> {
    val root: JsonNode = jsonMapper.readTree(json)
    val elements = root.get("elements") ?: return emptyList()
    if (!elements.isArray) return emptyList()
    return elements.mapNotNull { node ->
        val tags = node.get("tags") ?: return@mapNotNull null
        val name = tags.get("name")?.asText()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val lat = node.get("lat")?.asDouble() ?: return@mapNotNull null
        val lon = node.get("lon")?.asDouble() ?: return@mapNotNull null
        val elevation = tags.get("ele")?.asText()?.toDoubleOrNull()
        Peak(name = name, latLng = LatLng(lat, lon), elevation = elevation)
    }
}

private fun haversineMeters(a: LatLng, b: LatLng): Double {
    val earthRadiusMeters = 6371000.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLng = Math.toRadians(b.longitude - a.longitude)
    val sinDLat = sin(dLat / 2)
    val sinDLng = sin(dLng / 2)
    val h = sinDLat * sinDLat +
        cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sinDLng * sinDLng
    return 2 * earthRadiusMeters * atan2(sqrt(h), sqrt(1 - h))
}

class OverpassPeakRepository : PeakLookupRepository {

    override suspend fun findNearestPeak(latLng: LatLng, elevationHint: Double): Peak? {
        val query = "[out:json][timeout:15];" +
            "node[\"natural\"=\"peak\"][\"name\"]" +
            "(around:$SEARCH_RADIUS_METERS,${latLng.latitude},${latLng.longitude});" +
            "out body;"
        var lastError: Throwable? = null
        try {
            for (attempt in 1..MAX_ATTEMPTS) {
                try {
                    val response = Http().get(
                        url = "https://overpass-api.de/api/interpreter",
                        params = mapOf("data" to query),
                        headers = mapOf("Accept" to "application/json")
                    )
                    if (response.statusCode in 200..299) {
                        return parseOverpassPeaks(response.body).minByOrNull { haversineMeters(latLng, it.latLng) }
                    }
                    android.util.Log.w(
                        "OverpassPeak",
                        "findNearestPeak HTTP ${response.statusCode} (attempt $attempt/$MAX_ATTEMPTS)"
                    )
                    lastError = PeakLookupException("Overpass returned HTTP ${response.statusCode}")
                    if (response.statusCode !in RETRYABLE_STATUSES) break
                } catch (e: Exception) {
                    android.util.Log.e("OverpassPeak", "findNearestPeak error (attempt $attempt/$MAX_ATTEMPTS): ${e.message}")
                    lastError = e
                }
                if (attempt < MAX_ATTEMPTS) delay(RETRY_DELAYS_MS[attempt - 1])
            }
            throw PeakLookupException("Overpass lookup failed after $MAX_ATTEMPTS attempts", lastError)
        } finally {
            delay(THROTTLE_DELAY_MS)
        }
    }

    companion object {
        private const val MAX_ATTEMPTS = 3
        private val RETRY_DELAYS_MS = listOf(1000L, 3000L)
        private val RETRYABLE_STATUSES = setOf(429, 500, 502, 503, 504)
        private const val THROTTLE_DELAY_MS = 300L
    }
}
