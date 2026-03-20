package com.statsup.domain

import com.statsup.infrastructure.repository.DbBookmarkedTrainingRepository

class ManageBookmarkUseCase(
    private val repository: DbBookmarkedTrainingRepository
) {
    suspend fun getBookmark(trainingId: Long): BookmarkedTraining? =
        repository.getBookmarkByTrainingId(trainingId)

    /**
     * Creates a new bookmark or updates the existing one for [trainingId].
     */
    suspend fun addOrUpdate(trainingId: Long, note: String, customTitle: String, difficulty: String) {
        if (repository.getBookmarkByTrainingId(trainingId) != null) {
            repository.updateBookmark(trainingId, note, customTitle, difficulty)
        } else {
            repository.addBookmark(
                BookmarkedTraining(
                    trainingId = trainingId,
                    note = note,
                    customTitle = customTitle,
                    difficulty = difficulty
                )
            )
        }
    }

    suspend fun remove(trainingId: Long) {
        repository.removeBookmarkByTrainingId(trainingId)
    }
}
