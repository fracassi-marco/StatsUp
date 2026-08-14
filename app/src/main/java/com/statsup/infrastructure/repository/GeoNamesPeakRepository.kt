package com.statsup.infrastructure.repository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.google.android.gms.maps.model.LatLng
import com.statsup.BuildConfig
import com.statsup.domain.repository.Peak
import com.statsup.domain.repository.PeakLookupException
import com.statsup.domain.repository.PeakLookupRepository
import kotlinx.coroutines.delay
import topinambur.Http

private val jsonMapper = jsonMapper { addModule(kotlinModule()) }

// GeoNames feature codes under featureClass=T ("mountain,hill,rock,...") that actually denote a
// named summit; excludes sibling codes like HLL (hill) or PASS (mountain pass) to keep the same
// "is this really a peak" bar Overpass applies via natural=peak.
private val PEAK_FEATURE_CODES = setOf("PK", "MT")

fun parseGeoNamesPeaks(json: String): List<Peak> {
    val root: JsonNode = jsonMapper.readTree(json)
    root.get("status")?.let { status ->
        throw PeakLookupException("GeoNames returned an error: ${status.get("message")?.asText() ?: status}")
    }
    val geonames = root.get("geonames") ?: return emptyList()
    if (!geonames.isArray) return emptyList()
    return geonames.mapNotNull { node ->
        val fcode = node.get("fcode")?.asText()
        if (fcode !in PEAK_FEATURE_CODES) return@mapNotNull null
        val name = node.get("name")?.asText()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val lat = node.get("lat")?.asText()?.toDoubleOrNull() ?: return@mapNotNull null
        val lng = node.get("lng")?.asText()?.toDoubleOrNull() ?: return@mapNotNull null
        val elevation = node.get("elevation")?.asText()?.toDoubleOrNull()
        Peak(name = name, latLng = LatLng(lat, lng), elevation = elevation)
    }
}

/**
 * Fallback peak name lookup, used when [OverpassPeakRepository] has no named `natural=peak` node
 * nearby — OSM's peak coverage is inconsistent outside popular hiking areas, and GeoNames' own
 * gazetteer often fills the gap. Requires a free GeoNames account (see `geonames.username` in
 * `local.properties`, wired through as [BuildConfig.GEONAMES_USERNAME]).
 */
class GeoNamesPeakRepository : PeakLookupRepository {

    override suspend fun findNearestPeak(latLng: LatLng, elevationHint: Double): Peak? {
        try {
            var lastError: Throwable? = null
            for (attempt in 1..MAX_ATTEMPTS) {
                try {
                    val response = Http().get(
                        url = ENDPOINT,
                        params = mapOf(
                            "lat" to latLng.latitude.toString(),
                            "lng" to latLng.longitude.toString(),
                            "featureClass" to "T",
                            "radius" to SEARCH_RADIUS_KM.toString(),
                            "maxRows" to "10",
                            "username" to BuildConfig.GEONAMES_USERNAME
                        ),
                        headers = mapOf("Accept" to "application/json"),
                        timeoutMillis = TIMEOUT_MILLIS
                    )
                    if (response.statusCode in 200..299) {
                        val parsed = parseGeoNamesPeaks(response.body)
                        val chosen = choosePeak(parsed, latLng, elevationHint)
                        android.util.Log.d(
                            "GeoNamesPeak",
                            "query (${latLng.latitude},${latLng.longitude}) r=${SEARCH_RADIUS_KM}km -> " +
                                "${parsed.size} peak(s) [${parsed.joinToString { p -> "${p.name}@${haversineMeters(latLng, p.latLng).toInt()}m" }}], " +
                                "chosen=${chosen?.name ?: "none"}"
                        )
                        return chosen
                    }
                    android.util.Log.w(
                        "GeoNamesPeak",
                        "findNearestPeak HTTP ${response.statusCode} (attempt $attempt/$MAX_ATTEMPTS) body=${response.body.take(300)}"
                    )
                    lastError = PeakLookupException("GeoNames returned HTTP ${response.statusCode}")
                    if (response.statusCode !in RETRYABLE_STATUSES) break
                } catch (e: PeakLookupException) {
                    // Application-level error inside a 200 response (e.g. quota exceeded) — retrying
                    // won't help, unlike a transient HTTP status.
                    throw e
                } catch (e: Exception) {
                    android.util.Log.e("GeoNamesPeak", "attempt $attempt/$MAX_ATTEMPTS error: ${e.message}")
                    lastError = e
                }
                if (attempt < MAX_ATTEMPTS) delay(RETRY_DELAYS_MS[attempt - 1])
            }
            throw PeakLookupException("GeoNames lookup failed after $MAX_ATTEMPTS attempts", lastError)
        } finally {
            delay(THROTTLE_DELAY_MS)
        }
    }

    companion object {
        private const val ENDPOINT = "https://secure.geonames.org/findNearbyJSON"
        private const val SEARCH_RADIUS_KM = 2
        private const val MAX_ATTEMPTS = 3
        private val RETRY_DELAYS_MS = listOf(1000L, 3000L)
        private val RETRYABLE_STATUSES = setOf(429, 500, 502, 503, 504)
        private const val TIMEOUT_MILLIS = 15_000
        private const val THROTTLE_DELAY_MS = 300L
    }
}
