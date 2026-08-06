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

        val withPolyline = if (training.trip == null) {
            val polyline = trainingApi.fetchPolyline(token, training.id)
            if (polyline != null) training.copy(map = Route(summaryPolyline = polyline)) else training
        } else training
        val laps = trainingApi.laps(token, training.id)
        val withLaps = if (laps.isNotEmpty()) withPolyline.copy(lapsJson = jsonMapper.writeValueAsString(laps))
            else withPolyline
        val elevPoints = trainingApi.fetchElevationStream(token, training.id)
        val withElevation = if (!elevPoints.isNullOrEmpty()) withLaps.copy(elevationPointsJson = jsonMapper.writeValueAsString(elevPoints))
            else withLaps
        val trip = withElevation.trip
        val enriched = if (trip != null && geocodingRepository != null) {
            val startLabel = geocodingRepository.reverseGeocode(trip.begin().latitude, trip.begin().longitude)
            val endLabel = geocodingRepository.reverseGeocode(trip.end().latitude, trip.end().longitude)
            withElevation.copy(startLocationLabel = startLabel, endLocationLabel = endLabel)
        } else withElevation
        val withPeak = resolvePeak(enriched, elevPoints)
        val center = withPeak.trip?.centerPoint()
        val finalTraining = if (center != null) withPeak.copy(centerLat = center.latitude, centerLng = center.longitude) else withPeak

        trainingRepository.add(finalTraining)
        return finalTraining
    }

    private suspend fun resolvePeak(training: Training, elevPoints: List<Double>?): Training {
        if (peakLookupRepository == null || training.peakName != null || training.elevHigh < MIN_PEAK_ELEVATION_METERS) {
            return training
        }
        if (elevPoints.isNullOrEmpty()) return training.copy(peakName = "")
        val summit = estimateSummitLatLng(training.trip, elevPoints) ?: return training.copy(peakName = "")
        val peak = peakLookupRepository.findNearestPeak(summit, elevPoints.max())
        return training.copy(peakName = peak?.name ?: "", peakElevation = peak?.elevation)
    }

    companion object {
        private const val MIN_PEAK_ELEVATION_METERS = 1200.0
    }
}
