package com.statsup.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.Training
import com.statsup.domain.Trainings
import com.statsup.domain.repository.SettingRepository
import com.statsup.domain.repository.TrainingRepository
import kotlinx.coroutines.launch
import java.time.Year
import java.time.ZonedDateTime

class DashboardViewModel(
    private val trainingRepository: TrainingRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    private var trainings: List<Training> by mutableStateOf(emptyList())

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
        return Trainings(trainings) { it.sumOf { training -> training.distanceInKilometers() } }.overMonth()
    }

    fun totalFrequency(): Double {
        return Trainings(trainings) { it.count().toDouble() }.overMonth()
    }

    fun totalDuration(): Double {
        return Trainings(trainings) { it.sumOf { training -> training.durationInHours() } }.overMonth()
    }

    fun cumulativeDuration(): Map<Int, Double> {
        return Trainings(trainings) { it.sumOf { training -> training.durationInHours() } }.cumulativeDays(
            Year.from(ZonedDateTime.now()),
            ZonedDateTime.now().month
        )
    }

    fun maxElevation(): Double {
        return Trainings(trainings){ if(it.isEmpty()) 0.0 else it.maxOf { training -> training.totalElevationGain } }.overMonth()
    }
}