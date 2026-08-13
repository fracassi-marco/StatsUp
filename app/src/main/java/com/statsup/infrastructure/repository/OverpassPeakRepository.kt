package com.statsup.infrastructure.repository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.google.android.gms.maps.model.LatLng
import com.statsup.domain.repository.Peak
import com.statsup.domain.repository.PeakLookupException
import com.statsup.domain.repository.PeakLookupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.random.Random
import topinambur.Http
import topinambur.ServerResponse

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

/**
 * One instance is created per import run (see `ImportForegroundService`) and discarded
 * afterward, so [resolvedNearby] naturally scopes its cache to a single import.
 */
class OverpassPeakRepository : PeakLookupRepository {

    // Only ever touched sequentially: peak lookups are deliberately never run concurrently
    // (see findNearestPeak's throttle below), so no synchronization is needed here.
    private val resolvedNearby = mutableListOf<Pair<LatLng, Peak?>>()

    override suspend fun findNearestPeak(latLng: LatLng, elevationHint: Double): Peak? {
        // Repeat visits to the same summit (e.g. a recurring training route) land at slightly
        // different GPS-estimated points; reusing a nearby already-resolved result skips a
        // network round-trip entirely instead of re-querying Overpass for the same answer.
        resolvedNearby.firstOrNull { haversineMeters(latLng, it.first) <= CACHE_RADIUS_METERS }?.let {
            android.util.Log.d("OverpassPeak", "cache hit near (${latLng.latitude},${latLng.longitude}) -> ${it.second?.name}")
            return it.second
        }
        val result = queryOverpass(latLng)
        resolvedNearby.add(latLng to result)
        return result
    }

    private suspend fun queryOverpass(latLng: LatLng): Peak? {
        val query = "[out:json][timeout:15];" +
            "node[\"natural\"=\"peak\"][\"name\"]" +
            "(around:$SEARCH_RADIUS_METERS,${latLng.latitude},${latLng.longitude});" +
            "out body;"
        var lastError: Throwable? = null
        var globalAttempt = 0
        val totalAttempts = OVERPASS_ENDPOINTS.size * ATTEMPTS_PER_ENDPOINT
        try {
            for (endpoint in OVERPASS_ENDPOINTS) {
                for (attemptOnEndpoint in 1..ATTEMPTS_PER_ENDPOINT) {
                    globalAttempt++
                    try {
                        val response = fetchWithHardTimeout(endpoint, query)
                        if (response.statusCode in 200..299) {
                            val parsed = parseOverpassPeaks(response.body)
                            val chosen = parsed.minByOrNull { haversineMeters(latLng, it.latLng) }
                            android.util.Log.d(
                                "OverpassPeak",
                                "query via $endpoint (${latLng.latitude},${latLng.longitude}) r=${SEARCH_RADIUS_METERS}m -> " +
                                    "${parsed.size} named peak(s) [${parsed.joinToString { p -> "${p.name}@${haversineMeters(latLng, p.latLng).toInt()}m" }}], " +
                                    "chosen=${chosen?.name ?: "none"}"
                            )
                            return chosen
                        }
                        android.util.Log.w(
                            "OverpassPeak",
                            "findNearestPeak via $endpoint HTTP ${response.statusCode} (attempt $attemptOnEndpoint/$ATTEMPTS_PER_ENDPOINT) body=${response.body.take(300)}"
                        )
                        lastError = PeakLookupException("Overpass ($endpoint) returned HTTP ${response.statusCode}")
                        // A non-retryable status (e.g. a malformed query) will fail identically on
                        // every mirror, so move straight to the next endpoint instead of burning
                        // the remaining attempts on this one.
                        if (response.statusCode !in RETRYABLE_STATUSES) break
                    } catch (e: Exception) {
                        android.util.Log.e("OverpassPeak", "findNearestPeak via $endpoint error (attempt $attemptOnEndpoint/$ATTEMPTS_PER_ENDPOINT): ${e.message}")
                        lastError = e
                    }
                    if (globalAttempt < totalAttempts) delay(backoffWithJitterMillis(globalAttempt))
                }
            }
            android.util.Log.e(
                "OverpassPeak",
                "giving up on (${latLng.latitude},${latLng.longitude}) after $globalAttempt attempts across ${OVERPASS_ENDPOINTS.size} endpoints: ${lastError?.message}"
            )
            throw PeakLookupException("Overpass lookup failed after $globalAttempt attempts across ${OVERPASS_ENDPOINTS.size} endpoints", lastError)
        } finally {
            delay(THROTTLE_DELAY_MS)
        }
    }

    /**
     * `topinambur.Http` sets both connect and read timeouts on the underlying
     * `HttpURLConnection`, but DNS resolution isn't bounded by either — a stalled DNS lookup
     * (e.g. right after a network transition) can hang the whole call indefinitely. Running it
     * on a dedicated executor and bounding it with [ExecutorService.submit]'s `Future.get(timeout)`
     * guarantees this suspend function always returns within [HARD_TIMEOUT_MILLIS], even if the
     * abandoned worker thread itself never comes back.
     */
    private suspend fun fetchWithHardTimeout(endpoint: String, query: String): ServerResponse =
        withContext(Dispatchers.IO) {
            val future = ioExecutor.submit(
                Callable {
                    Http().get(
                        url = endpoint,
                        params = mapOf("data" to query),
                        headers = mapOf("Accept" to "application/json"),
                        timeoutMillis = OVERPASS_TIMEOUT_MILLIS
                    )
                }
            )
            try {
                future.get(HARD_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
            } catch (e: TimeoutException) {
                future.cancel(true)
                throw PeakLookupException("Overpass call to $endpoint exceeded hard timeout (possible DNS/socket stall)", e)
            }
        }

    private fun backoffWithJitterMillis(attempt: Int): Long {
        val exponential = (BASE_BACKOFF_MILLIS * (1L shl (attempt - 1))).coerceAtMost(MAX_BACKOFF_MILLIS)
        return exponential + Random.nextLong(0, exponential / 2 + 1)
    }

    companion object {
        // Public Overpass mirrors, tried in order after the primary exhausts its attempts —
        // overpass-api.de rate-limits (429) or times out (502/504) bursty single-IP usage, which
        // a bulk import inherently is (one sequential request per training).
        private val OVERPASS_ENDPOINTS = listOf(
            "https://overpass-api.de/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter",
            "https://overpass.openstreetmap.ru/api/interpreter"
        )
        private const val ATTEMPTS_PER_ENDPOINT = 2
        private const val BASE_BACKOFF_MILLIS = 1000L
        private const val MAX_BACKOFF_MILLIS = 8000L
        private val RETRYABLE_STATUSES = setOf(429, 500, 502, 503, 504)
        private const val THROTTLE_DELAY_MS = 300L
        private const val OVERPASS_TIMEOUT_MILLIS = 20_000
        private const val HARD_TIMEOUT_MILLIS = OVERPASS_TIMEOUT_MILLIS + 5_000L
        private const val CACHE_RADIUS_METERS = 200.0

        // A fresh thread is spun up per call rather than reused, so a call abandoned after
        // HARD_TIMEOUT_MILLIS (its worker thread still blocked on a stalled DNS lookup) can never
        // block a subsequent lookup the way a fixed-size/single-thread pool would.
        private val ioExecutor: ExecutorService = Executors.newCachedThreadPool { r ->
            Thread(r, "OverpassPeakIO").apply { isDaemon = true }
        }
    }
}
