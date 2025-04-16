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

    fun cumulativeMonth() = trainings().cumulativeDaysTrend()

    fun pastCumulativeMonth() = pastMonthTrainings().cumulativeDays()

    private fun pastMonthTrainings() = Trainings(trainings, provider = provider(), now = ZonedDateTime.now().minusMonths(1))
    private fun pastYearTraining() = Trainings(trainings, provider = provider(), now = ZonedDateTime.now().minusYears(1))

    fun cumulativeYear() = trainings().cumulativeMonthsTrend()
    fun pastCumulativeYear() = pastYearTraining().cumulativeMonths()

    fun hideMonthChart() = selectedSpan != 0
    fun hideYearChart() = selectedSpan != 1

    fun groupByDay() = trainings().groupBy31Day()
    fun groupByMonth() = trainings().by12Month()

    fun maxOfMonth() = trainings().groupByDay().maxOf { it.value }

    fun doneOfMonth() = trainings().groupByDay().values.sum()
    fun trendOfMonth() = cumulativeMonth().values.last()
    fun doneOfPastMonth() = pastMonthTrainings().groupByDay().values.sum()

    fun averageOfMonth() = trainings().groupByDay().values.filter { it != 0.0 } .average()

    fun maxOfYear() = trainings().byMonth().maxOf { it.value }

    fun doneOfYear() = trainings().byMonth().values.sum()
    fun trendOfYear() = cumulativeYear().values.last()
    fun doneOfPastYear() = pastYearTraining().byMonth().values.sum()

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

