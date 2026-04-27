package com.statsup.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.Training
import com.statsup.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllRoutesViewModel(
    private val trainingRepository: TrainingRepository
) : ViewModel() {

    private val _trainings = MutableStateFlow<List<Training>>(emptyList())
    val trainings: StateFlow<List<Training>> = _trainings.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var backfillDone = false

    init {
        loadAllTrainingsWithRoutes()
    }

    private fun loadAllTrainingsWithRoutes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                trainingRepository.all().collect { allTrainings ->
                    val withRoutes = allTrainings
                        .filter { it.map?.summaryPolyline?.isNotBlank() == true }
                        .sortedByDescending { it.date }
                    _trainings.value = withRoutes
                    _isLoading.value = false

                    if (!backfillDone) {
                        backfillDone = true
                        launch { backfillMissingCenters(withRoutes) }
                    }
                }
            } catch (_: Exception) {
                _trainings.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    private suspend fun backfillMissingCenters(trainings: List<Training>) {
        trainings
            .filter { it.centerLat == null }
            .forEach { training ->
                val center = training.trip?.centerPoint() ?: return@forEach
                trainingRepository.updateCenter(training.id, center.latitude, center.longitude)
            }
    }

    fun refresh() {
        backfillDone = false
        loadAllTrainingsWithRoutes()
    }
}
