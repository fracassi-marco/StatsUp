package com.statsup.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.Training
import com.statsup.domain.repository.SettingRepository
import com.statsup.domain.repository.TrainingRepository
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class DashboardViewModel(
    private val trainingRepository: TrainingRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    var trainings: List<Training> by mutableStateOf(emptyList())
        private set

    init {
        viewModelScope.launch {
            trainingRepository.all().collect {
                trainings = it
            }
        }
    }

    fun distancePercentage(): Float {
        return totalDistance().toFloat() / settingRepository.loadMonthlyGoal()
    }

    fun totalDistance(): Double {
        return totalOfMonth(ZonedDateTime.now()) { it.sumOf { training -> training.distanceInKilometers() } }
    }

    private fun totalOfMonth(date: ZonedDateTime, provider: (List<Training>) -> Double) = provider(ofMonth(date))

    private fun ofMonth(date: ZonedDateTime) = trainings.filter { it.date.month == date.month && it.date.year == date.year }
}