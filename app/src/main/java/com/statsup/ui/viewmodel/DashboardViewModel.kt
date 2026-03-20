package com.statsup.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.Badge
import com.statsup.domain.BestEffort
import com.statsup.domain.CheckGoalAchievementUseCase
import com.statsup.domain.EvaluateBadgesUseCase
import com.statsup.domain.GoalAchievement
import com.statsup.domain.Provider
import com.statsup.domain.SuggestAutoTargetsUseCase
import com.statsup.domain.TargetSuggestion
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

    private val checkAchievement = CheckGoalAchievementUseCase()
    private val suggestTargets = SuggestAutoTargetsUseCase()
    private val evaluateBadges = EvaluateBadgesUseCase()

    private val _badgesEarned = MutableSharedFlow<List<Badge>>(extraBufferCapacity = 10)
    val badgesEarned: SharedFlow<List<Badge>> = _badgesEarned.asSharedFlow()

    // null = not yet initialized (first emission); used to skip initial state
    private var previousEarnedBadgeIds: Set<String>? = null

    init {
        viewModelScope.launch {
            trainingRepository.all().collect {
                trainings = it
                checkGoalAchievement()
                checkBadgeEarning()
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

        val achievement = checkAchievement(
            previousDistance = previousDistancePercentage,
            currentDistance = currentDistance,
            previousTraining = previousTrainingGoalPercentage,
            currentTraining = currentTraining
        )

        previousDistancePercentage = currentDistance
        previousTrainingGoalPercentage = currentTraining

        if (achievement != null) {
            viewModelScope.launch {
                _goalAchieved.emit(achievement)
            }
        }
    }

    private fun checkTargetSuggestion() {
        if (settingRepository.loadAutoTargets()) return

        val currentYearMonth = YearMonth.now().toString()
        if (settingRepository.loadLastSuggestedYearMonth() == currentYearMonth) return

        val suggestion = suggestTargets(
            trainings = trainings,
            currentDistanceGoalKm = settingRepository.loadMonthlyGoal(),
            currentTrainingGoal = settingRepository.loadMonthlyTrainingGoal()
        )

        if (suggestion == null) {
            // Nothing meaningful to suggest — mark as seen so we don't check again this month
            settingRepository.saveLastSuggestedYearMonth(currentYearMonth)
            return
        }

        targetSuggestion = suggestion
    }

    private fun checkBadgeEarning() {
        val badges = evaluateBadges(
            trainings = trainings,
            monthlyDistanceGoalKm = effectiveMonthlyDistanceGoal(),
            monthlyTrainingGoal = effectiveMonthlyTrainingGoal()
        )
        val earnedIds = badges.filter { it.earned }.map { it.id }.toSet()
        val prev = previousEarnedBadgeIds
        if (prev != null) {
            val newlyEarned = badges.filter { it.earned && it.id !in prev }
            if (newlyEarned.isNotEmpty()) {
                viewModelScope.launch { _badgesEarned.emit(newlyEarned) }
            }
        }
        previousEarnedBadgeIds = earnedIds
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

    fun bestEfforts(): List<BestEffort> {
        return Trainings(trainings, provider = Provider.None).ofMonth().let {
            Trainings(it, provider = Provider.None).bestEfforts()
        }
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
