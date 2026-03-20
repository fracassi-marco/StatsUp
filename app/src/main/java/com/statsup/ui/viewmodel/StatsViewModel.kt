package com.statsup.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.BestEffort
import com.statsup.domain.PerformancePrediction
import com.statsup.domain.Provider
import com.statsup.domain.Training
import com.statsup.domain.Trainings
import com.statsup.domain.repository.TrainingRepository
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale

class StatsViewModel(
    private val trainingRepository: TrainingRepository
) : ViewModel() {
    private var trainings: List<Training> by mutableStateOf(emptyList())

    var selectedSpan by mutableIntStateOf(0)
        private set
    var selectedProvider by mutableIntStateOf(0)

    private var selectedNow: ZonedDateTime by mutableStateOf(ZonedDateTime.now())

    fun switchSpan(index: Int) {
        selectedSpan = index
        selectedNow = ZonedDateTime.now()
    }

    fun switchProvider(index: Int) {
        selectedProvider = index
    }

    fun previousPeriod() {
        selectedNow = if (selectedSpan == 0) selectedNow.minusMonths(1) else selectedNow.minusYears(1)
    }

    fun nextPeriod() {
        if (!isCurrentPeriod()) {
            selectedNow = if (selectedSpan == 0) selectedNow.plusMonths(1) else selectedNow.plusYears(1)
        }
    }

    fun isCurrentPeriod(): Boolean {
        val now = ZonedDateTime.now()
        return if (selectedSpan == 0) {
            selectedNow.year == now.year && selectedNow.month == now.month
        } else {
            selectedNow.year == now.year
        }
    }

    fun periodLabel(): String {
        return if (selectedSpan == 0) {
            val month = selectedNow.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                .replaceFirstChar { it.uppercaseChar() }
            if (selectedNow.year == ZonedDateTime.now().year) month
            else "$month ${selectedNow.year}"
        } else {
            selectedNow.year.toString()
        }
    }

    fun cumulativeMonth() = trainings().cumulativeDaysTrend()

    fun pastCumulativeMonth() = pastMonthTrainings().cumulativeDays()

    private fun pastMonthTrainings() = Trainings(trainings, provider = provider(), now = selectedNow.minusMonths(1))
    private fun pastYearTraining() = Trainings(trainings, provider = provider(), now = selectedNow.minusYears(1))

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

    fun averageOfMonth() = trainings().groupByDay().values.filter { it != 0.0 }.average()

    fun maxOfYear() = trainings().byMonth().maxOf { it.value }

    fun doneOfYear() = trainings().byMonth().values.sum()
    fun trendOfYear() = cumulativeYear().values.last()
    fun doneOfPastYear() = pastYearTraining().byMonth().values.sum()

    fun averageOfYear() = trainings().byMonth().values.filter { it != 0.0 }.average()

    fun performancePredictions(): List<PerformancePrediction> {
        val filtered = if (selectedSpan == 0) {
            trainings.filter { it.date.month == selectedNow.month && it.date.year == selectedNow.year }
        } else {
            trainings.filter { it.date.year == selectedNow.year }
        }
        return Trainings(filtered, provider = Provider.None).performancePredictions()
    }

    fun bestEfforts(): List<BestEffort> {
        val filtered = if (selectedSpan == 0) {
            trainings.filter { it.date.month == selectedNow.month && it.date.year == selectedNow.year }
        } else {
            trainings.filter { it.date.year == selectedNow.year }
        }
        return Trainings(filtered, provider = Provider.None).bestEfforts()
    }

    private fun provider() = Provider.byIndex(selectedProvider)

    private fun trainings() = Trainings(trainings, provider = provider(), now = selectedNow)

    init {
        viewModelScope.launch {
            trainingRepository.all().collect {
                trainings = it
            }
        }
    }
}
