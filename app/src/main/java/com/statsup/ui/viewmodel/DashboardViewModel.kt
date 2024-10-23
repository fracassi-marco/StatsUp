package com.statsup.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.Provider
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
        return Trainings(trainings, provider = Provider.Distance).overMonth()
    }

    fun totalFrequency(): Double {
        return Trainings(trainings, provider = Provider.Frequency).overMonth()
    }

    fun totalDuration(): Double {
        return Trainings(trainings, provider = Provider.Duration).overMonth()
    }

    fun cumulativeDuration(): Map<Int, Double> {
        return Trainings(trainings, provider = Provider.Duration).cumulativeDays()
    }

    fun cumulativeDistance(): Map<Int, Double> {
        return Trainings(trainings, provider = Provider.Distance).cumulativeDays()
    }

    fun pastCumulativeDistance(): Map<Int, Double> {
        return Trainings(trainings, provider = Provider.Distance, now = ZonedDateTime.now().minusMonths(1)).cumulativeDays()
    }

    fun maxElevationGain(): Double {
        return Trainings(trainings, provider = Provider.Elevation).maxOfMonth()
    }

    fun maxAltitude(): Double {
        return Trainings(trainings, provider = Provider.Altitude).maxOfMonth()
    }

    fun maxHeartRate(): Double {
        return Trainings(trainings, provider = Provider.HeartRate).maxOfMonth()
    }

    fun monthlyDistanceGoal() = settingRepository.loadMonthlyGoal().toFloat()

    fun topTrainings(): Map<String, List<Training>> {
        return Trainings(trainings, provider = Provider.None).ofMonth().groupBy { it.type!! }
    }
}