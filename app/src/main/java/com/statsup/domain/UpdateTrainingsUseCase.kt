package com.statsup.domain

import android.util.Log
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.statsup.domain.repository.AthleteRepository
import com.statsup.domain.repository.GeocodingRepository
import com.statsup.domain.repository.PeakLookupRepository
import com.statsup.domain.repository.TrainingRepository

class UpdateTrainingsUseCase(
    private val trainingRepository: TrainingRepository,
    private val athleteRepository: AthleteRepository,
    private val trainingApi: TrainingApi,
    private val geocodingRepository: GeocodingRepository? = null,
    private val peakLookupRepository: PeakLookupRepository? = null
) {

    private val jsonMapper = jsonMapper { addModule(kotlinModule()) }.apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    /**
     * Trainings are persisted strictly in chronological order (oldest first), one at a time.
     * This keeps [TrainingRepository.latest] — which drives the next incremental fetch's start
     * date — always pointing at a contiguous, fully-saved prefix: if processing is interrupted
     * partway, nothing after the last successfully-inserted training is ever skipped, so
     * retrying resumes exactly where it left off with no gaps and no duplicates (inserts are
     * REPLACE-on-conflict by id).
     */
    suspend operator fun invoke(token: String, onProgress: (suspend (Int, Int) -> Unit)? = null): Int {
        val latestTraining = trainingRepository.latest()
        val downloaded = trainingApi.download(token, latestTraining).sortedBy { it.startDate }
        val total = downloaded.size
        var processed = 0

        processChunksPipelined(downloaded, token, trainingApi, geocodingRepository, jsonMapper) { enrichedChunk ->
            enrichedChunk.forEach { (enriched, elevPoints) ->
                val withPeak = resolvePeak(enriched, elevPoints, peakLookupRepository)
                val center = withPeak.trip?.centerPoint()
                trainingRepository.add(
                    if (center != null) withPeak.copy(centerLat = center.latitude, centerLng = center.longitude)
                    else withPeak
                )
                processed++
                onProgress?.invoke(processed, total)
            }
        }

        // New trainings are already persisted at this point (one at a time above); a failure
        // refreshing the athlete profile is not worth reporting as a failed import.
        try {
            val athlete = trainingApi.athlete(token)
            athleteRepository.update(athlete)
        } catch (e: Exception) {
            Log.w("UpdateTrainingsUseCase", "Athlete profile refresh failed after a successful import", e)
        }
        return total
    }
}
