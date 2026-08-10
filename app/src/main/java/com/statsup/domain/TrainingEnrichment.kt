package com.statsup.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.statsup.domain.repository.GeocodingRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * How many trainings are enriched concurrently per import batch. Bounded (rather than
 * unlimited) to avoid bursting intervals.icu with too many simultaneous requests; each
 * training itself fans out into several concurrent calls (polyline/laps/elevation/geocoding),
 * so the real peak concurrency is a small multiple of this value.
 */
const val IMPORT_FETCH_CONCURRENCY = 5

/**
 * Fetches polyline/laps/elevation/geocoding for a single [training] concurrently instead of
 * one round-trip at a time. Every underlying call already degrades to null/empty on failure
 * (see [com.statsup.infrastructure.IntervalsIcuTrainingApi]), so a failure on one field never
 * throws and never drops the others — this only changes how many calls are in flight at once,
 * not the fallback behavior.
 *
 * Returns the enriched [Training] together with the elevation stream, which the caller needs
 * separately to resolve the peak afterward.
 */
suspend fun enrichTrainingDetails(
    training: Training,
    token: String,
    trainingApi: TrainingApi,
    geocodingRepository: GeocodingRepository?,
    jsonMapper: ObjectMapper
): Pair<Training, List<Double>?> = coroutineScope {
    val lapsDeferred = async { trainingApi.laps(token, training.id) }
    val elevationDeferred = async { trainingApi.fetchElevationStream(token, training.id) }

    val withPolyline = if (training.trip == null) {
        val polyline = trainingApi.fetchPolyline(token, training.id)
        if (polyline != null) training.copy(map = Route(summaryPolyline = polyline)) else training
    } else training

    val trip = withPolyline.trip
    val geoDeferred = if (trip != null && geocodingRepository != null) {
        val startDeferred = async { geocodingRepository.reverseGeocode(trip.begin().latitude, trip.begin().longitude) }
        val endDeferred = async { geocodingRepository.reverseGeocode(trip.end().latitude, trip.end().longitude) }
        startDeferred to endDeferred
    } else null

    val laps = lapsDeferred.await()
    val elevPoints = elevationDeferred.await()

    val withLaps = if (laps.isNotEmpty()) withPolyline.copy(lapsJson = jsonMapper.writeValueAsString(laps)) else withPolyline
    val withElevation = if (!elevPoints.isNullOrEmpty()) {
        withLaps.copy(elevationPointsJson = jsonMapper.writeValueAsString(elevPoints))
    } else withLaps

    val enriched = if (geoDeferred != null) {
        val startLabel = geoDeferred.first.await()
        val endLabel = geoDeferred.second.await()
        withElevation.copy(startLocationLabel = startLabel, endLocationLabel = endLabel)
    } else withElevation

    enriched to elevPoints
}

/**
 * Runs [enrichTrainingDetails] for a whole chunk concurrently (bounded by the chunk's size)
 * and returns the results in the same order as [chunk].
 */
suspend fun fetchChunk(
    chunk: List<Training>,
    token: String,
    trainingApi: TrainingApi,
    geocodingRepository: GeocodingRepository?,
    jsonMapper: ObjectMapper
): List<Pair<Training, List<Double>?>> = coroutineScope {
    chunk.map { training ->
        async { enrichTrainingDetails(training, token, trainingApi, geocodingRepository, jsonMapper) }
    }.awaitAll()
}

/**
 * Splits [trainings] into chunks and runs [onChunk] (typically: sequential, throttle-bound
 * peak resolution + DB write) for each one — while the *next* chunk's concurrent fetch is
 * already running in the background. Without this, peak resolution against Overpass (which
 * must stay strictly one-at-a-time, see [com.statsup.infrastructure.repository.OverpassPeakRepository])
 * would otherwise stall the next chunk's network fetch instead of overlapping with it.
 */
suspend fun processChunksPipelined(
    trainings: List<Training>,
    token: String,
    trainingApi: TrainingApi,
    geocodingRepository: GeocodingRepository?,
    jsonMapper: ObjectMapper,
    onChunk: suspend (List<Pair<Training, List<Double>?>>) -> Unit
) {
    val chunks = trainings.chunked(IMPORT_FETCH_CONCURRENCY)
    coroutineScope {
        var prefetched: Deferred<List<Pair<Training, List<Double>?>>>? = null
        for (i in chunks.indices) {
            val current = prefetched ?: async { fetchChunk(chunks[i], token, trainingApi, geocodingRepository, jsonMapper) }
            prefetched = if (i + 1 < chunks.size) {
                async { fetchChunk(chunks[i + 1], token, trainingApi, geocodingRepository, jsonMapper) }
            } else null
            onChunk(current.await())
        }
    }
}
