package com.statsup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.Training
import com.statsup.infrastructure.repository.DbBookmarkedTrainingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BookmarksViewModel(
    private val bookmarkedTrainingRepository: DbBookmarkedTrainingRepository
) : ViewModel() {

    val bookmarkedTrainings: StateFlow<List<Training>> =
        bookmarkedTrainingRepository.getBookmarkedTrainings()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}

