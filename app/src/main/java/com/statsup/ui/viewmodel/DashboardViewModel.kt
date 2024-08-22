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
import java.time.ZonedDateTime

class DashboardViewModel(
    private val trainingRepository: TrainingRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    private var trainings: List<Training> by mutableStateOf(emptyList())
    private val distance: (List<Training>) -> Double = { it.sumOf { training -> training.distanceInKilometers() } }

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
        return Trainings(trainings, provider = distance).overMonth()
    }

    fun totalFrequency(): Double {
        return Trainings(trainings) { it.count().toDouble() }.overMonth()
    }

    fun totalDuration(): Double {
        return Trainings(trainings) { it.sumOf { training -> training.durationInHours() } }.overMonth()
    }

    fun cumulativeDuration(): Map<Int, Double> {
        return Trainings(trainings) { it.sumOf { training -> training.durationInHours() } }.cumulativeDays()
    }

    fun cumulativeDistance(): Map<Int, Double> {
        return Trainings(trainings, provider = distance).cumulativeDays()
    }

    fun pastCumulativeDistance(): Map<Int, Double> {
        return Trainings(trainings, provider = distance, now = ZonedDateTime.now().minusMonths(1)).cumulativeDays()
    }

    fun maxElevationGain(): Double {
        return Trainings(trainings) { if (it.isEmpty()) 0.0 else it.maxOf { training -> training.totalElevationGain } }.overMonth()
    }

    fun maxAltitude(): Double {
        return Trainings(trainings) { if (it.isEmpty()) 0.0 else it.maxOf { training -> training.elevHigh } }.overMonth()
    }

    fun maxHeartRate(): Double {
        return Trainings(trainings) { if (it.isEmpty()) 0.0 else it.maxOf { training -> training.maxHeartrate } }.overMonth()
    }

    fun monthlyDistanceGoal() = settingRepository.loadMonthlyGoal().toFloat()

    fun topTrainings(): Map<String, List<Training>> {
        return Trainings(trainings) { 0.0 }.ofMonth().groupBy { it.type!! }
    }
}