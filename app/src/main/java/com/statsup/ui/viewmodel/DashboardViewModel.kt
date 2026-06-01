package com.statsup.ui.viewmodel

import android.app.Application
import com.statsup.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.Badge
import com.statsup.domain.BestEffort
import com.statsup.domain.RecoveryContribution
import com.statsup.domain.CheckGoalAchievementUseCase
import com.statsup.domain.EvaluateBadgesUseCase
import com.statsup.domain.EvaluateLevelUseCase
import com.statsup.domain.GoalAchievement
import com.statsup.domain.Level
import com.statsup.domain.Provider
import com.statsup.domain.SuggestAutoTargetsUseCase
import com.statsup.domain.TargetSuggestion
import com.statsup.domain.Training
import com.statsup.domain.Trainings
import com.statsup.domain.repository.SettingRepository
import com.statsup.domain.repository.TrainingRepository
import com.statsup.ui.buildBadgeStringMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime

class DashboardViewModel(
    application: Application,
    private val trainingRepository: TrainingRepository,
    private val settingRepository: SettingRepository,
) : AndroidViewModel(application) {

    private data class Computed(
        val totalDistance: Double = 0.0,
        val totalFrequency: Double = 0.0,
        val totalDuration: Double = 0.0,
        val cumulativeDuration: Map<Int, Double> = emptyMap(),
        val cumulativeDistance: Map<Int, Double> = emptyMap(),
        val pastCumulativeDistance: Map<Int, Double> = emptyMap(),
        val maxElevationGain: Double = 0.0,
        val maxAltitude: Double = 0.0,
        val maxHeartRate: Double = 0.0,
        val topTrainings: Map<String, List<Training>> = emptyMap(),
        val currentStreak: Int = 0,
        val bestStreak: Int = 0,
        val projectedCumulativeDistance: Map<Int, Double> = emptyMap(),
        val activityHeatmap: Map<LocalDate, Double> = emptyMap(),
        val bestEfforts: List<BestEffort> = emptyList(),
        val hrZoneDistribution: Map<Int, Int> = emptyMap(),
        val effectiveMonthlyDistanceGoal: Int = 0,
        val effectiveMonthlyTrainingGoal: Int = 0,
        val recoveryTime: Double = 0.0,
        val recoveryBreakdown: List<RecoveryContribution> = emptyList(),
        val level: Level = Level(1, R.string.level_name_1, "🌱", 0, 0, 200, false, 0, 0),
    )

    private var trainings: List<Training> = emptyList()
    private var computed: Computed by mutableStateOf(Computed())

    private val _goalAchieved = MutableSharedFlow<GoalAchievement>(extraBufferCapacity = 1)
    val goalAchieved: SharedFlow<GoalAchievement> = _goalAchieved.asSharedFlow()

    var targetSuggestion: TargetSuggestion? by mutableStateOf(null)
        private set

    private var previousDistancePercentage = 0f
    private var previousTrainingGoalPercentage = 0f
    private var isFirstEmission = true

    private val checkAchievement = CheckGoalAchievementUseCase()
    private val suggestTargets = SuggestAutoTargetsUseCase()
    private val evaluateBadges = EvaluateBadgesUseCase()
    private val evaluateLevel = EvaluateLevelUseCase()

    private val _badgesEarned = MutableSharedFlow<List<Badge>>(extraBufferCapacity = 10)
    val badgesEarned: SharedFlow<List<Badge>> = _badgesEarned.asSharedFlow()

    var currentLevelUp: Level? by mutableStateOf(null)
        private set

    private var previousEarnedBadgeIds: Set<String>? = null
    private var previousLevel: Int? = null

    init {
        viewModelScope.launch {
            trainingRepository.all().collect { newTrainings ->
                trainings = newTrainings
                computed = withContext(Dispatchers.Default) { computeAll(newTrainings) }
                checkGoalAchievement()
                checkBadgeEarning()
                checkLevelUp()
                if (!isFirstEmission) checkTargetSuggestion()
            }
        }
    }

    private fun computeAll(newTrainings: List<Training>): Computed {
        val distT = Trainings(newTrainings, provider = Provider.Distance)
        val freqT = Trainings(newTrainings, provider = Provider.Frequency)
        val durT = Trainings(newTrainings, provider = Provider.Duration)
        val noneT = Trainings(newTrainings, provider = Provider.None)
        val pastDistT = Trainings(newTrainings, provider = Provider.Distance, now = ZonedDateTime.now().minusMonths(1))
        val autoTargets = settingRepository.loadAutoTargets()
        val monthlyGoal = settingRepository.loadMonthlyGoal()
        val monthlyTrainGoal = settingRepository.loadMonthlyTrainingGoal()
        val effectiveDistGoal = if (autoTargets) distT.autoDistanceTarget(fallbackKm = monthlyGoal) else monthlyGoal
        val effectiveFreqGoal = if (autoTargets) freqT.autoTrainingTarget(fallbackCount = monthlyTrainGoal) else monthlyTrainGoal
        val projectedCumDist = distT.cumulativeDaysTrend()
        val ofMonth = noneT.ofMonth()
        return Computed(
            totalDistance = distT.overMonth(),
            totalFrequency = freqT.overMonth(),
            totalDuration = durT.overMonth(),
            cumulativeDuration = durT.cumulativeDays(),
            cumulativeDistance = distT.cumulativeDays(),
            pastCumulativeDistance = pastDistT.cumulativeDays(),
            maxElevationGain = Trainings(newTrainings, provider = Provider.Elevation).maxOfMonth(),
            maxAltitude = Trainings(newTrainings, provider = Provider.Altitude).maxOfMonth(),
            maxHeartRate = Trainings(newTrainings, provider = Provider.HeartRate).maxOfMonth(),
            topTrainings = ofMonth.groupBy { it.sportType ?: it.type ?: "Unknown" },
            currentStreak = noneT.currentStreak(),
            bestStreak = noneT.bestStreak(),
            projectedCumulativeDistance = projectedCumDist,
            activityHeatmap = distT.heatmapByDay(),
            bestEfforts = Trainings(ofMonth, provider = Provider.None).bestEfforts(),
            hrZoneDistribution = noneT.hrZoneDistribution(),
            effectiveMonthlyDistanceGoal = effectiveDistGoal,
            effectiveMonthlyTrainingGoal = effectiveFreqGoal,
            recoveryTime = noneT.recoveryTime(),
            recoveryBreakdown = noneT.recoveryBreakdown(),
            level = evaluateLevel(newTrainings),
        )
    }

    private fun checkGoalAchievement() {
        val currentDistance = distancePercentage()
        val currentTraining = trainingGoalPercentage()

        if (isFirstEmission) {
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
            settingRepository.saveLastSuggestedYearMonth(currentYearMonth)
            return
        }

        targetSuggestion = suggestion
    }

    private fun checkBadgeEarning() {
        val distGoal = effectiveMonthlyDistanceGoal()
        val trainGoal = effectiveMonthlyTrainingGoal()
        val badges = evaluateBadges(
            trainings = trainings,
            monthlyDistanceGoalKm = distGoal,
            monthlyTrainingGoal = trainGoal,
            strings = buildBadgeStringMap(getApplication(), distGoal, trainGoal)
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

    private fun checkLevelUp() {
        val current = computed.level.number
        val prev = previousLevel
        if (prev != null && current > prev) {
            currentLevelUp = computed.level
        }
        previousLevel = current
    }

    fun dismissLevelUp() {
        currentLevelUp = null
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
        targetSuggestion = null
    }

    fun distancePercentage(): Float {
        return computed.totalDistance.toFloat() / computed.effectiveMonthlyDistanceGoal
    }

    fun totalDistance(): Double = computed.totalDistance
    fun totalFrequencyInt(): Int = computed.totalFrequency.toInt()

    fun trainingGoalPercentage(): Float {
        val goal = effectiveMonthlyTrainingGoal()
        if (goal == 0) return 0f
        return totalFrequencyInt().toFloat() / goal.toFloat()
    }

    fun monthlyTrainingGoal(): Int = effectiveMonthlyTrainingGoal()

    fun effectiveMonthlyDistanceGoal(): Int = computed.effectiveMonthlyDistanceGoal
    fun effectiveMonthlyTrainingGoal(): Int = computed.effectiveMonthlyTrainingGoal

    fun totalDuration(): Double = computed.totalDuration
    fun cumulativeDuration(): Map<Int, Double> = computed.cumulativeDuration
    fun cumulativeDistance(): Map<Int, Double> = computed.cumulativeDistance
    fun pastCumulativeDistance(): Map<Int, Double> = computed.pastCumulativeDistance
    fun maxElevationGain(): Double = computed.maxElevationGain
    fun maxAltitude(): Double = computed.maxAltitude
    fun monthlyDistanceGoal(): Float = computed.effectiveMonthlyDistanceGoal.toFloat()
    fun topTrainings(): Map<String, List<Training>> = computed.topTrainings
    fun currentStreak(): Int = computed.currentStreak
    fun bestStreak(): Int = computed.bestStreak
    fun projectedDistanceEndOfMonth(): Double = computed.projectedCumulativeDistance.values.lastOrNull() ?: 0.0
    fun projectedCumulativeDistance(): Map<Int, Double> = computed.projectedCumulativeDistance
    fun activityHeatmap(): Map<LocalDate, Double> = computed.activityHeatmap
    fun bestEfforts(): List<BestEffort> = computed.bestEfforts
    fun hrZoneDistribution(): Map<Int, Int> = computed.hrZoneDistribution
    fun recoveryTime(): Double = computed.recoveryTime
    fun recoveryBreakdown(): List<RecoveryContribution> = computed.recoveryBreakdown

    fun streakPercentage(): Float {
        val best = bestStreak()
        if (best == 0) return 0f
        return currentStreak().toFloat() / best.toFloat()
    }

    fun level(): Level = computed.level
}
