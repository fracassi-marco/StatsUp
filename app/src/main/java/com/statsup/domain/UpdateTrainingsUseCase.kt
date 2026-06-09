package com.statsup.domain

import com.statsup.domain.repository.AthleteRepository
import com.statsup.domain.repository.TrainingRepository

class UpdateTrainingsUseCase(
    private val trainingRepository: TrainingRepository,
    private val athleteRepository: AthleteRepository,
    private val trainingApi: TrainingApi) {

    suspend operator fun invoke(token: String): List<Training> {
        val latestTraining = trainingRepository.latest()
        val downloaded = trainingApi.download(token, latestTraining)
        val trainings = downloaded.map { training ->
            if (training.trip == null) {
                val polyline = trainingApi.fetchPolyline(token, training.id)
                if (polyline != null) training.copy(map = Route(summaryPolyline = polyline)) else training
            } else training
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