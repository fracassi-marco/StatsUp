package com.statsup.domain

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.statsup.domain.repository.AthleteRepository
import com.statsup.domain.repository.TrainingRepository

class UpdateTrainingsUseCase(
    private val trainingRepository: TrainingRepository,
    private val athleteRepository: AthleteRepository,
    private val trainingApi: TrainingApi) {

    private val jsonMapper = jsonMapper { addModule(kotlinModule()) }.apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    suspend operator fun invoke(token: String): List<Training> {
        val latestTraining = trainingRepository.latest()
        val downloaded = trainingApi.download(token, latestTraining)
        val trainings = downloaded.map { training ->
            val withPolyline = if (training.trip == null) {
                val polyline = trainingApi.fetchPolyline(token, training.id)
                if (polyline != null) training.copy(map = Route(summaryPolyline = polyline)) else training
            } else training
            val laps = trainingApi.laps(token, training.id)
            val withLaps = if (laps.isNotEmpty()) withPolyline.copy(lapsJson = jsonMapper.writeValueAsString(laps))
                else withPolyline
            val elevPoints = trainingApi.fetchElevationStream(token, training.id)
            if (!elevPoints.isNullOrEmpty()) withLaps.copy(elevationPointsJson = jsonMapper.writeValueAsString(elevPoints))
            else withLaps
        }
        trainings.forEach { training ->
            val center = training.trip?.centerPoint()
            trainingRepository.add(
                if (center != null) training.copy(centerLat = center.latitude, centerLng = center.longitude)
                else training
            )
        }
        val athlete = trainingApi.athlete(token)
        athleteRepository.update(athlete)
        return trainings
    }
}