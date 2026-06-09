package com.statsup.domain

import com.statsup.domain.repository.AthleteRepository
import com.statsup.domain.repository.TrainingRepository
import com.statsup.infrastructure.repository.DbBookmarkedTrainingRepository

class FullImportUseCase(
    private val trainingRepository: TrainingRepository,
    private val athleteRepository: AthleteRepository,
    private val bookmarkedTrainingRepository: DbBookmarkedTrainingRepository,
    private val trainingApi: TrainingApi
) {

    suspend operator fun invoke(token: String): List<Training> {
        val savedBookmarks = bookmarkedTrainingRepository.getAllBookmarksList()

        trainingRepository.deleteAll()

        val downloaded = trainingApi.download(token, latest = null)
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

        val importedIds = trainings.map { it.id }.toSet()
        savedBookmarks
            .filter { it.trainingId in importedIds }
            .forEach { bookmarkedTrainingRepository.addBookmark(it.copy(id = 0)) }

        val athlete = trainingApi.athlete(token)
        athleteRepository.update(athlete)
        return trainings
    }
}
