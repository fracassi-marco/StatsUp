package com.statsup.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.Training
import com.statsup.domain.Trainings
import com.statsup.domain.repository.TrainingRepository
import kotlinx.coroutines.launch
import java.time.Month
import java.time.ZonedDateTime

class StatsViewModel(
    private val trainingRepository: TrainingRepository
) : ViewModel() {
    private var trainings: List<Training> by mutableStateOf(emptyList())
    private val distance: (List<Training>) -> Double = { it.sumOf { training -> training.distanceInKilometers() } }
    private val frequency: (List<Training>) -> Double = { it.count().toDouble() }
    private val duration: (List<Training>) -> Double = { it.sumOf { training -> training.durationInHours() } }
    private val elevation: (List<Training>) -> Double = { it.sumOf { training -> training.totalElevationGain } }

    var selectedSpan by mutableIntStateOf(0)
        private set
    var selectedProvider by mutableIntStateOf(0)

    fun switchSpan(index: Int) {
        selectedSpan = index
    }

    fun cumulativeMonth(): Map<Int, Double> {
        return Trainings(trainings, provider = provider()).cumulativeDays()
    }

    fun pastCumulativeMonth(): Map<Int, Double> {
        return Trainings(trainings, provider = provider(), now = ZonedDateTime.now().minusMonths(1)).cumulativeDays()
    }

    fun cumulativeYear(): LinkedHashMap<Month, Double> {
        return Trainings(trainings, provider = provider()).cumulativeMonths()
    }

    fun pastCumulativeYear(): LinkedHashMap<Month, Double> {
        return Trainings(trainings, provider = provider(), now = ZonedDateTime.now().minusYears(1)).cumulativeMonths()
    }

    private fun provider() = when (selectedProvider) {
        0 -> distance
        1 -> frequency
        2 -> duration
        else -> elevation
    }

    fun hideMonthChart(): Boolean {
        return selectedSpan != 0
    }

    fun hideYearChart(): Boolean {
        return selectedSpan != 1
    }

    fun switchProvider(index: Int) {
        selectedProvider = index
    }

    init {
        viewModelScope.launch {
            trainingRepository.all().collect {
                trainings = it
            }
        }
    }
}