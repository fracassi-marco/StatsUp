package com.statsup.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.repository.TrainingRepository
import kotlinx.coroutines.launch

class HistoryViewModel(private val trainingRepository: TrainingRepository) : ViewModel() {

    private val _state = mutableStateOf(HistoryState())
    val state: State<HistoryState> = _state

    init {
        viewModelScope.launch {
            trainingRepository.all().collect {
                _state.value = state.value.copy(
                    activities = it
                )
            }
        }
    }
}
