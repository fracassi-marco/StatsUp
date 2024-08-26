package com.statsup.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.Provider
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

    private fun provider() = Provider.byIndex(selectedProvider)

    fun hideMonthChart(): Boolean {
        return selectedSpan != 0
    }

    fun hideYearChart(): Boolean {
        return selectedSpan != 1
    }

    fun switchProvider(index: Int) {
        selectedProvider = index
    }

    fun groupByDay(): Map<Int, Double> {
        return Trainings(trainings, provider = provider()).groupByDay()
    }

    fun groupByMonth(): Map<Month, Double> {
        return Trainings(trainings, provider = provider()).byMonth()
    }

    init {
        viewModelScope.launch {
            trainingRepository.all().collect {
                trainings = it
            }
        }
    }
}

