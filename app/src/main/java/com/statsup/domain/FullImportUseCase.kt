package com.statsup.domain

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.statsup.domain.repository.AthleteRepository
import com.statsup.domain.repository.GeocodingRepository
import com.statsup.domain.repository.PeakLookupRepository
import com.statsup.domain.repository.TrainingRepository
import com.statsup.infrastructure.repository.DbBookmarkedTrainingRepository

class FullImportUseCase(
    private val trainingRepository: TrainingRepository,
    private val athleteRepository: AthleteRepository,
    private val bookmarkedTrainingRepository: DbBookmarkedTrainingRepository,
    private val trainingApi: TrainingApi,
    private val geocodingRepository: GeocodingRepository? = null,
    private val peakLookupRepository: PeakLookupRepository? = null
) {

    private val jsonMapper = jsonMapper { addModule(kotlinModule()) }.apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    /**
     * Fetches and enriches every training from the API before touching the database at all,
     * then swaps the whole history in one atomic [TrainingRepository.replaceAll]. If fetching
     * fails or the import is interrupted partway, the existing data is never deleted — the
     * import can simply be retried from scratch without losing anything.
     */
    suspend operator fun invoke(token: String, onProgress: (suspend (Int, Int) -> Unit)? = null): Int {
        val savedBookmarks = bookmarkedTrainingRepository.getAllBookmarksList()

        val downloaded = trainingApi.download(token, latest = null)
        val total = downloaded.size
        var processed = 0
        val enrichedTrainings = ArrayList<Training>(total)

        processChunksPipelined(downloaded, token, trainingApi, geocodingRepository, jsonMapper) { enrichedChunk ->
            enrichedChunk.forEach { (enriched, elevPoints) ->
                val withPeak = resolvePeak(enriched, elevPoints, peakLookupRepository)
                val center = withPeak.trip?.centerPoint()
                enrichedTrainings.add(
                    if (center != null) withPeak.copy(centerLat = center.latitude, centerLng = center.longitude)
                    else withPeak
                )
                processed++
                onProgress?.invoke(processed, total)
            }
        }

        trainingRepository.replaceAll(enrichedTrainings)

        val importedIds = enrichedTrainings.mapTo(HashSet(enrichedTrainings.size)) { it.id }
        savedBookmarks
            .filter { it.trainingId in importedIds }
            .forEach { bookmarkedTrainingRepository.addBookmark(it.copy(id = 0)) }

        val athlete = trainingApi.athlete(token)
        athleteRepository.update(athlete)
        return total
    }
}
