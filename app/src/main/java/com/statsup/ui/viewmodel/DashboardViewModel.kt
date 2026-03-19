package com.statsup.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.GoalAchievement
import com.statsup.domain.Provider
import com.statsup.domain.Training
import com.statsup.domain.Trainings
import com.statsup.domain.repository.SettingRepository
import com.statsup.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime

data class TargetSuggestion(val distanceKm: Int, val trainingCount: Int)

class DashboardViewModel(
    private val trainingRepository: TrainingRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    private var trainings: List<Training> by mutableStateOf(emptyList())

    private val _goalAchieved = MutableSharedFlow<GoalAchievement>(extraBufferCapacity = 1)
    val goalAchieved: SharedFlow<GoalAchievement> = _goalAchieved.asSharedFlow()

    var targetSuggestion: TargetSuggestion? by mutableStateOf(null)
        private set

    // Track previous percentages to detect the crossing of the 1.0 threshold
    private var previousDistancePercentage = 0f
    private var previousTrainingGoalPercentage = 0f
    private var isFirstEmission = true

    init {
        viewModelScope.launch {
            trainingRepository.all().collect {
                trainings = it
                checkGoalAchievement()
                if (isFirstEmission.not()) checkTargetSuggestion()
            }
        }
    }

    private fun checkGoalAchievement() {
        val currentDistance = distancePercentage()
        val currentTraining = trainingGoalPercentage()

        if (isFirstEmission) {
            // On first load, just initialise baselines without triggering celebrations
            previousDistancePercentage = currentDistance
            previousTrainingGoalPercentage = currentTraining
            isFirstEmission = false
            checkTargetSuggestion()
            return
        }

        val distanceGoalJustReached = previousDistancePercentage < 1f && currentDistance >= 1f
        val trainingGoalJustReached = previousTrainingGoalPercentage < 1f && currentTraining >= 1f

        previousDistancePercentage = currentDistance
        previousTrainingGoalPercentage = currentTraining

        val achievement = when {
            distanceGoalJustReached && trainingGoalJustReached -> GoalAchievement.BOTH
            distanceGoalJustReached -> GoalAchievement.DISTANCE
            trainingGoalJustReached -> GoalAchievement.TRAINING_COUNT
            else -> return
        }

        viewModelScope.launch {
            _goalAchieved.emit(achievement)
        }
    }

    private fun checkTargetSuggestion() {
        if (settingRepository.loadAutoTargets()) return

        val currentYearMonth = YearMonth.now().toString() // e.g. "2026-03"
        if (settingRepository.loadLastSuggestedYearMonth() == currentYearMonth) return

        val suggestedDistance = Trainings(trainings, provider = Provider.Distance)
            .autoDistanceTarget(fallbackKm = settingRepository.loadMonthlyGoal())
        val suggestedTraining = Trainings(trainings, provider = Provider.Frequency)
            .autoTrainingTarget(fallbackCount = settingRepository.loadMonthlyTrainingGoal())

        // Only suggest if the computed values differ from current manual targets
        val distanceDiffers = suggestedDistance != settingRepository.loadMonthlyGoal()
        val trainingDiffers = suggestedTraining != settingRepository.loadMonthlyTrainingGoal()
        if (!distanceDiffers && !trainingDiffers) {
            // Nothing meaningful to suggest — mark as seen so we don't check again this month
            settingRepository.saveLastSuggestedYearMonth(currentYearMonth)
            return
        }

        targetSuggestion = TargetSuggestion(suggestedDistance, suggestedTraining)
    }

    fun acceptTargetSuggestion() {
        val suggestion = targetSuggestion ?: return
        settingRepository.saveMonthlyGoal(suggestion.distanceKm)
        settingRepository.saveMonthlyTrainingGoal(suggestion.trainingCount)
        settingRepository.saveLastSuggestedYearMonth(YearMonth.now().toString())
        targetSuggestion = null
    }

    fun dismissTargetSuggestion() {
        settingRepository.saveLastSuggestedYearMonth(YearMonth.now().toString())
        targetSuggestion = null
    }

    fun snoozeTargetSuggestion() {
        // Close the dialog but do NOT mark the month as handled — it will re-appear next launch
        targetSuggestion = null
    }

    suspend fun testGoalAchieved(achievement: GoalAchievement) {
        _goalAchieved.emit(achievement)
    }

    fun distancePercentage(): Float {
        return totalDistance().toFloat() / effectiveMonthlyDistanceGoal()
    }

    fun totalDistance(): Double {
        return Trainings(trainings, provider = Provider.Distance).overMonth()
    }

    fun totalFrequency(): Double {
        return Trainings(trainings, provider = Provider.Frequency).overMonth()
    }

    fun totalFrequencyInt(): Int {
        return totalFrequency().toInt()
    }

    fun trainingGoalPercentage(): Float {
        val goal = effectiveMonthlyTrainingGoal()
        if (goal == 0) return 0f
        return totalFrequencyInt().toFloat() / goal.toFloat()
    }

    fun monthlyTrainingGoal(): Int {
        return effectiveMonthlyTrainingGoal()
    }

    fun effectiveMonthlyDistanceGoal(): Int {
        return if (settingRepository.loadAutoTargets()) {
            Trainings(trainings, provider = Provider.Distance)
                .autoDistanceTarget(fallbackKm = settingRepository.loadMonthlyGoal())
        } else {
            settingRepository.loadMonthlyGoal()
        }
    }

    fun effectiveMonthlyTrainingGoal(): Int {
        return if (settingRepository.loadAutoTargets()) {
            Trainings(trainings, provider = Provider.Frequency)
                .autoTrainingTarget(fallbackCount = settingRepository.loadMonthlyTrainingGoal())
        } else {
            settingRepository.loadMonthlyTrainingGoal()
        }
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

    fun monthlyDistanceGoal() = effectiveMonthlyDistanceGoal().toFloat()

    fun topTrainings(): Map<String, List<Training>> {
        return Trainings(trainings, provider = Provider.None).ofMonth().groupBy { it.sportType ?: it.type ?: "Unknown" }
    }

    fun currentStreak(): Int {
        return Trainings(trainings, provider = Provider.None).currentStreak()
    }

    fun bestStreak(): Int {
        return Trainings(trainings, provider = Provider.None).bestStreak()
    }

    fun projectedDistanceEndOfMonth(): Double {
        return Trainings(trainings, provider = Provider.Distance).cumulativeDaysTrend().values.lastOrNull() ?: 0.0
    }

    fun projectedCumulativeDistance(): Map<Int, Double> {
        return Trainings(trainings, provider = Provider.Distance).cumulativeDaysTrend()
    }

    fun activityHeatmap(): Map<LocalDate, Double> {
        return Trainings(trainings, provider = Provider.Distance).heatmapByDay()
    }

    fun hrZoneDistribution(): Map<Int, Int> {
        return Trainings(trainings, provider = Provider.None).hrZoneDistribution()
    }

    fun streakPercentage(): Float {
        val best = bestStreak()
        if (best == 0) return 0f
        return currentStreak().toFloat() / best.toFloat()
    }
}
