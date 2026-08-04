package com.statsup.domain

import java.time.ZonedDateTime

enum class FitnessFactorType {
    TRAINING_LOAD, RECOVERY, CONSISTENCY, LOAD_BALANCE, INTENSITY_BALANCE,
    SPORT_VARIETY, PERFORMANCE_TREND, MEDIUM_TERM_TREND, WEIGHT_TREND
}

data class FitnessScoreFactor(
    val type: FitnessFactorType,
    val score: Double,
    val weightPercent: Double,
    val baseWeightPercent: Double,
    val included: Boolean
)

data class FitnessScore(
    val score: Int = 0,
    val factors: List<FitnessScoreFactor> = emptyList(),
    val recoveryHours: Double = 0.0,
    val activeDays28: Int = 0,
    val weeklyWeightRateKg: Double? = null
)

data class FitnessScoreTrendPoint(
    val date: ZonedDateTime,
    val score: Int
)
