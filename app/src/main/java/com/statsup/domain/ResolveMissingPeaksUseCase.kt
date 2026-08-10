package com.statsup.domain

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.statsup.domain.repository.PeakLookupRepository
import com.statsup.domain.repository.TrainingRepository

/**
 * Retries peak resolution for trainings left unresolved (`peakName == null`) by a past
 * import/reimport — e.g. because Overpass was rate-limited at the time. Unlike
 * [FullImportUseCase], this does not delete or re-download anything: it only fills in the
 * missing peak for trainings already in the DB, reusing their stored elevation stream when
 * available.
 */
class ResolveMissingPeaksUseCase(
    private val trainingRepository: TrainingRepository,
    private val trainingApi: TrainingApi,
    private val peakLookupRepository: PeakLookupRepository? = null
) {

    private val jsonMapper = jsonMapper { addModule(kotlinModule()) }
    private val elevationListType = jsonMapper.typeFactory.constructCollectionType(List::class.java, Double::class.java)

    suspend operator fun invoke(token: String, onProgress: (suspend (Int, Int) -> Unit)? = null): Int {
        val candidates = trainingRepository.getAllTrainings()
            .filter { it.peakName == null && it.elevHigh >= MIN_PEAK_ELEVATION_METERS }
        val total = candidates.size
        var resolvedCount = 0

        candidates.forEachIndexed { index, training ->
            onProgress?.invoke(index + 1, total)
            val storedElevPoints = training.elevationPointsJson?.let { jsonMapper.readValue<List<Double>>(it, elevationListType) }
            val elevPoints = storedElevPoints ?: trainingApi.fetchElevationStream(token, training.id)
            val withElevation = if (storedElevPoints == null && !elevPoints.isNullOrEmpty()) {
                training.copy(elevationPointsJson = jsonMapper.writeValueAsString(elevPoints))
            } else training

            val resolved = resolvePeak(withElevation, elevPoints, peakLookupRepository)
            trainingRepository.add(resolved)
            if (!resolved.peakName.isNullOrBlank()) resolvedCount++
        }

        return resolvedCount
    }
}
