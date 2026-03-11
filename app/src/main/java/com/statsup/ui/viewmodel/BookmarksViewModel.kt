package com.statsup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.BookmarkedTrainingWithDetails
import com.statsup.infrastructure.repository.DbBookmarkedTrainingRepository
import com.statsup.infrastructure.repository.DbTrainingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class BookmarksViewModel(
    private val bookmarkedTrainingRepository: DbBookmarkedTrainingRepository,
    private val trainingRepository: DbTrainingRepository
) : ViewModel() {

    val bookmarkedTrainings: StateFlow<List<BookmarkedTrainingWithDetails>> =
        combine(
            bookmarkedTrainingRepository.getBookmarkedTrainings(),
            bookmarkedTrainingRepository.getAllBookmarksFlow()
        ) { trainings, bookmarks ->
            trainings.mapNotNull { training ->
                val bookmark = bookmarks.find { it.trainingId == training.id }
                bookmark?.let {
                    BookmarkedTrainingWithDetails(
                        training = training,
                        customTitle = it.customTitle,
                        difficulty = it.difficulty,
                        note = it.note
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

