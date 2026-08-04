package com.statsup.domain

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sign

class FitnessScoreUseCase {

    private val weightStatsUseCase = WeightStatsUseCase()

    operator fun invoke(
        trainings: List<Training>,
        weightEntries: List<WeightEntry>,
        weightTargetKg: Double,
        now: ZonedDateTime = ZonedDateTime.now()
    ): FitnessScore {
        val windowHours = WINDOW_DAYS * 24
        val recentTrainings = trainings.filter { ChronoUnit.HOURS.between(it.date, now) in 0..windowHours }
        val last7dTrainings = recentTrainings.filter { ChronoUnit.HOURS.between(it.date, now) in 0..(7 * 24) }

        val recoveryHours = Trainings(trainings, now, Provider.None).recoveryTime()
        val activeDays28 = recentTrainings.map { it.date.toLocalDate() }.distinct().count()

        val weightStats = weightStatsUseCase(weightEntries.sortedBy { it.date }, heightCm = 0, targetKg = weightTargetKg)
        val weightIncluded = weightStats.totalMeasurements > 0

        val values: Map<FitnessFactorType, Double?> = mapOf(
            FitnessFactorType.TRAINING_LOAD to trainingLoadScore(recentTrainings, now, windowHours),
            FitnessFactorType.RECOVERY to 100.0 * (1.0 - recoveryHours / RECOVERY_CAP_HOURS),
            FitnessFactorType.CONSISTENCY to (activeDays28 / IDEAL_ACTIVE_DAYS * 100.0).coerceAtMost(100.0),
            FitnessFactorType.LOAD_BALANCE to loadBalanceScore(last7dTrainings, recentTrainings),
            FitnessFactorType.INTENSITY_BALANCE to intensityBalanceScore(recentTrainings),
            FitnessFactorType.SPORT_VARIETY to sportVarietyScore(recentTrainings),
            FitnessFactorType.PERFORMANCE_TREND to performanceTrendScore(trainings, now),
            FitnessFactorType.MEDIUM_TERM_TREND to mediumTermTrendScore(trainings, now),
            FitnessFactorType.WEIGHT_TREND to (if (weightIncluded) weightTrendScore(weightStats, weightTargetKg) else null)
        )

        val totalActiveWeight = BASE_WEIGHTS.filterKeys { values.getValue(it) != null }.values.sum()
        val factors = FitnessFactorType.entries.map { type ->
            val baseWeight = BASE_WEIGHTS.getValue(type)
            val value = values.getValue(type)
            FitnessScoreFactor(
                type = type,
                score = value ?: 0.0,
                weightPercent = if (value != null) baseWeight / totalActiveWeight * 100.0 else 0.0,
                baseWeightPercent = baseWeight,
                included = value != null
            )
        }

        val finalScore = factors.filter { it.included }
            .sumOf { it.score * (it.weightPercent / 100.0) }
            .roundToInt()
            .coerceIn(0, 100)

        return FitnessScore(
            score = finalScore,
            factors = factors,
            recoveryHours = recoveryHours,
            activeDays28 = activeDays28,
            weeklyWeightRateKg = if (weightIncluded) weightStats.weeklyRate else null
        )
    }

    private fun trainingLoadScore(recentTrainings: List<Training>, now: ZonedDateTime, windowHours: Long): Double {
        val recentLoad = recentTrainings.sumOf { t ->
            val hoursSince = ChronoUnit.HOURS.between(t.date, now).toDouble()
            val recencyWeight = (1.0 - hoursSince / windowHours).coerceAtLeast(0.0)
            activityLoad(t) * recencyWeight
        }
        return 100.0 * (1.0 - exp(-recentLoad / TRAINING_LOAD_SATURATION_K))
    }

    /**
     * Acute:Chronic Workload Ratio — average daily load of the last 7 days vs the last 28 days.
     * Sweet spot [0.8, 1.3]; penalized harder above 1.3 (injury risk) than below 0.8 (already
     * covered by Consistency).
     */
    private fun loadBalanceScore(last7dTrainings: List<Training>, recentTrainings: List<Training>): Double? {
        val chronicLoad28 = recentTrainings.sumOf { activityLoad(it) } / WINDOW_DAYS
        if (chronicLoad28 <= 0.0) return null
        val acuteLoad7 = last7dTrainings.sumOf { activityLoad(it) } / 7.0
        val ratio = acuteLoad7 / chronicLoad28
        return if (ratio <= ACWR_UPPER_SAFE) {
            100.0 - (ACWR_LOWER_SAFE - ratio).coerceAtLeast(0.0) * 40.0
        } else {
            (100.0 - (ratio - ACWR_UPPER_SAFE) * 140.0).coerceAtLeast(0.0)
        }
    }

    /** Polarized-training balance: reward ~80% easy / 20% hard among heart-rate-tagged sessions. */
    private fun intensityBalanceScore(recentTrainings: List<Training>): Double? {
        val classified = recentTrainings.mapNotNull { t ->
            if (t.hasHeartrate == true && (t.averageHeartrate ?: 0.0) > 0) hrZone(t.averageHeartrate!!) else null
        }
        if (classified.isEmpty()) return null
        val easyRatio = classified.count { it <= 2 }.toDouble() / classified.size
        return (100.0 - abs(easyRatio - IDEAL_EASY_RATIO) * 125.0).coerceIn(0.0, 100.0)
    }

    private fun hrZone(averageHeartrate: Double): Int {
        val pct = averageHeartrate / MAX_HR_ESTIMATE
        return when {
            pct < 0.60 -> 1
            pct < 0.70 -> 2
            pct < 0.80 -> 3
            pct < 0.90 -> 4
            else -> 5
        }
    }

    private fun sportVarietyScore(recentTrainings: List<Training>): Double? {
        if (recentTrainings.isEmpty()) return null
        val distinctSports = recentTrainings.mapNotNull { it.sportType ?: it.type }.distinct().size
        return when {
            distinctSports <= 1 -> 70.0
            distinctSports == 2 -> 90.0
            else -> 100.0
        }
    }

    /** Compares best running pace (per standard race distance) in the last 90 days vs the 90 days before that. */
    private fun performanceTrendScore(trainings: List<Training>, now: ZonedDateTime): Double? {
        val runs = trainings.filter { it.sportType == "Run" || it.type == "Run" }
        val windowHours = PERFORMANCE_WINDOW_DAYS * 24
        val recentRuns = runs.filter { ChronoUnit.HOURS.between(it.date, now) in 0 until windowHours }
        val baselineRuns = runs.filter { ChronoUnit.HOURS.between(it.date, now) in windowHours until (2 * windowHours) }

        val improvements = PERFORMANCE_TARGET_DISTANCES.mapNotNull { targetDist ->
            val recentPace = bestPaceSecPerMeter(recentRuns, targetDist) ?: return@mapNotNull null
            val baselinePace = bestPaceSecPerMeter(baselineRuns, targetDist) ?: return@mapNotNull null
            (baselinePace - recentPace) / baselinePace
        }
        if (improvements.isEmpty()) return null
        return (50.0 + improvements.average() * 500.0).coerceIn(0.0, 100.0)
    }

    private fun bestPaceSecPerMeter(runs: List<Training>, minDistanceMeters: Double): Double? =
        runs.filter { it.distance >= minDistanceMeters }.minOfOrNull { it.movingTime.toDouble() / it.distance }

    /** Average distance of the last 3 completed months vs the median of the 12 completed months before that. */
    private fun mediumTermTrendScore(trainings: List<Training>, now: ZonedDateTime): Double? {
        fun monthlyDistanceKm(monthsAgo: Long): Double {
            val target = now.minusMonths(monthsAgo)
            return trainings.filter { it.date.month == target.month && it.date.year == target.year }
                .sumOf { it.distanceInKilometers() }
        }

        val recentAvg = (1L..3L).map { monthlyDistanceKm(it) }.average()
        val baselineValues = (4L..15L).map { monthlyDistanceKm(it) }.filter { it > 0.0 }
        if (baselineValues.size < MIN_BASELINE_MONTHS) return null
        val baselineMedian = median(baselineValues)
        if (baselineMedian <= 0.0) return null
        return (recentAvg / baselineMedian * 100.0).coerceIn(0.0, 100.0)
    }

    private fun median(values: List<Double>): Double {
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 0) (sorted[mid - 1] + sorted[mid]) / 2.0 else sorted[mid]
    }

    private fun weightTrendScore(stats: WeightStats, weightTargetKg: Double): Double {
        val latest = stats.latestWeight ?: return 100.0
        val hasActiveTarget = weightTargetKg > 0 && abs(weightTargetKg - latest) >= WEIGHT_TARGET_TOLERANCE_KG
        return if (hasActiveTarget) {
            val desiredSign = sign(weightTargetKg - latest)
            val alignment = stats.weeklyRate * desiredSign
            (50.0 + (alignment * 50.0).coerceIn(-50.0, 50.0)).coerceIn(0.0, 100.0)
        } else {
            (100.0 - 50.0 * abs(stats.weeklyRate)).coerceIn(0.0, 100.0)
        }
    }

    companion object {
        private const val WINDOW_DAYS = 28L
        private const val IDEAL_ACTIVE_DAYS = 16.0
        private const val RECOVERY_CAP_HOURS = 72.0
        private const val TRAINING_LOAD_SATURATION_K = 900.0
        private const val WEIGHT_TARGET_TOLERANCE_KG = 0.5
        private const val ACWR_LOWER_SAFE = 0.8
        private const val ACWR_UPPER_SAFE = 1.3
        private const val MAX_HR_ESTIMATE = 190.0
        private const val IDEAL_EASY_RATIO = 0.8
        private const val PERFORMANCE_WINDOW_DAYS = 90L
        private const val MIN_BASELINE_MONTHS = 3
        private val PERFORMANCE_TARGET_DISTANCES = listOf(1_000.0, 5_000.0, 10_000.0, 21_097.5, 42_195.0)

        private val BASE_WEIGHTS = mapOf(
            FitnessFactorType.TRAINING_LOAD to 20.0,
            FitnessFactorType.RECOVERY to 15.0,
            FitnessFactorType.CONSISTENCY to 15.0,
            FitnessFactorType.LOAD_BALANCE to 12.0,
            FitnessFactorType.INTENSITY_BALANCE to 8.0,
            FitnessFactorType.SPORT_VARIETY to 5.0,
            FitnessFactorType.PERFORMANCE_TREND to 8.0,
            FitnessFactorType.MEDIUM_TERM_TREND to 7.0,
            FitnessFactorType.WEIGHT_TREND to 10.0
        )
    }
}
