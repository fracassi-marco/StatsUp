package com.statsup.domain

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.statsup.domain.repository.GeocodingRepository
import com.statsup.domain.repository.PeakLookupRepository
import com.statsup.domain.repository.TrainingRepository

/**
 * Forces the re-download of a single training from the API, regardless of its date.
 * Useful to bring back a training that was deleted locally: since [UpdateTrainingsUseCase]
 * only fetches activities newer than the most recent one stored locally, a deleted
 * older training would otherwise never reappear via the incremental update.
 */
class ReimportTrainingUseCase(
    private val trainingRepository: TrainingRepository,
    private val trainingApi: TrainingApi,
    private val geocodingRepository: GeocodingRepository? = null,
    private val peakLookupRepository: PeakLookupRepository? = null
) {

    private val jsonMapper = jsonMapper { addModule(kotlinModule()) }.apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    suspend operator fun invoke(token: String, trainingId: String): Training {
        val training = trainingApi.fetchActivityById(token, trainingId)
            ?: throw ApiException(404)

        val (enriched, elevPoints) = enrichTrainingDetails(training, token, trainingApi, geocodingRepository, jsonMapper)
        val existing = existingTraining(trainingId)
        val withPeak = resolvePeak(enriched, elevPoints, peakLookupRepository, existing)
        val center = withPeak.trip?.centerPoint()
        val finalTraining = if (center != null) withPeak.copy(centerLat = center.latitude, centerLng = center.longitude) else withPeak

        trainingRepository.add(finalTraining)
        return finalTraining
    }

    private fun existingTraining(trainingId: String): Training? =
        try {
            trainingRepository.byId(trainingId)
        } catch (e: Exception) {
            null
        }
}
