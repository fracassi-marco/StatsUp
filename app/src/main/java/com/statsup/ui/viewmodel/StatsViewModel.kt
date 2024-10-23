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

    fun switchProvider(index: Int) {
        selectedProvider = index
    }

    fun cumulativeMonth() = trainings().cumulativeDays()

    fun pastCumulativeMonth() = Trainings(trainings, provider = provider(), now = ZonedDateTime.now().minusMonths(1)).cumulativeDays()

    fun cumulativeYear() = trainings().cumulativeMonths()

    fun pastCumulativeYear() = Trainings(trainings, provider = provider(), now = ZonedDateTime.now().minusYears(1)).cumulativeMonths()

    fun hideMonthChart() = selectedSpan != 0

    fun hideYearChart() = selectedSpan != 1

    fun groupByDay() = trainings().groupByDay()

    fun groupByMonth() = trainings().byMonth()

    fun maxOfMonth() = trainings().groupByDay().maxOf { it.value }

    fun totalAverageOfMonth() = trainings().groupByDay().values.average()

    fun averageOfMonth() = trainings().groupByDay().values.filter { it != 0.0 } .average()

    fun maxOfYear() = trainings().byMonth().maxOf { it.value }

    fun totalAverageOfYear() = trainings().byMonth().values.average()

    fun averageOfYear() = trainings().byMonth().values.filter { it != 0.0 } .average()

    private fun provider() = Provider.byIndex(selectedProvider)

    private fun trainings() = Trainings(trainings, provider = provider())

    init {
        viewModelScope.launch {
            trainingRepository.all().collect {
                trainings = it
            }
        }
    }
}

