package com.statsup.domain

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import kotlinx.coroutines.delay
import com.statsup.domain.repository.AthleteRepository
import com.statsup.domain.repository.GeocodingRepository
import com.statsup.domain.repository.TrainingRepository
import com.statsup.infrastructure.repository.DbBookmarkedTrainingRepository
import kotlin.time.Duration.Companion.milliseconds

class FullImportUseCase(
    private val trainingRepository: TrainingRepository,
    private val athleteRepository: AthleteRepository,
    private val bookmarkedTrainingRepository: DbBookmarkedTrainingRepository,
    private val trainingApi: TrainingApi,
    private val geocodingRepository: GeocodingRepository? = null
) {

    private val jsonMapper = jsonMapper { addModule(kotlinModule()) }.apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    suspend operator fun invoke(token: String, onProgress: (suspend (Int, Int) -> Unit)? = null): List<Training> {
        val savedBookmarks = bookmarkedTrainingRepository.getAllBookmarksList()

        trainingRepository.deleteAll()

        val downloaded = trainingApi.download(token, latest = null)
        val total = downloaded.size
        val trainings = downloaded.mapIndexed { index, training ->
            onProgress?.invoke(index + 1, total)
            val withPolyline = if (training.trip == null) {
                val polyline = trainingApi.fetchPolyline(token, training.id)
                delay(300.milliseconds)
                if (polyline != null) training.copy(map = Route(summaryPolyline = polyline)) else training
            } else training
            val laps = trainingApi.laps(token, training.id)
            val withLaps = if (laps.isNotEmpty()) withPolyline.copy(lapsJson = jsonMapper.writeValueAsString(laps))
                else withPolyline
            val elevPoints = trainingApi.fetchElevationStream(token, training.id)
            val withElevation = if (!elevPoints.isNullOrEmpty()) withLaps.copy(elevationPointsJson = jsonMapper.writeValueAsString(elevPoints))
            else withLaps
            val trip = withElevation.trip
            if (trip != null && geocodingRepository != null) {
                val startLabel = geocodingRepository.reverseGeocode(trip.begin().latitude, trip.begin().longitude)
                val endLabel = geocodingRepository.reverseGeocode(trip.end().latitude, trip.end().longitude)
                withElevation.copy(startLocationLabel = startLabel, endLocationLabel = endLabel)
            } else withElevation
        }
        trainings.forEach { training ->
            val center = training.trip?.centerPoint()
            trainingRepository.add(
                if (center != null) training.copy(centerLat = center.latitude, centerLng = center.longitude)
                else training
            )
        }

        val importedIds = trainings.map { it.id }.toSet()
        savedBookmarks
            .filter { it.trainingId in importedIds }
            .forEach { bookmarkedTrainingRepository.addBookmark(it.copy(id = 0)) }

        val athlete = trainingApi.athlete(token)
        athleteRepository.update(athlete)
        return trainings
    }
}
