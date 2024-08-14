package com.statsup.domain

import com.statsup.domain.repository.AthleteRepository
import com.statsup.domain.repository.TrainingRepository

class UpdateTrainingsUseCase(
    private val trainingRepository: TrainingRepository,
    private val athleteRepository: AthleteRepository,
    private val trainingApi: TrainingApi) {

    suspend operator fun invoke(token: String): List<Training> {
        val latestTraining = trainingRepository.latest()
        val trainings = trainingApi.download(token, latestTraining)
        trainings.forEach { trainingRepository.add(it) }
        val athlete = trainingApi.athlete(token)
        athleteRepository.update(athlete)
        return trainings
    }
}