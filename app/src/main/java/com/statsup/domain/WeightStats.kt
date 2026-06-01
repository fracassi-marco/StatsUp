package com.statsup.domain

import java.time.LocalDate

enum class BmiCategory { UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE_1, OBESE_2, OBESE_3 }

data class WeightStats(
    val latestWeight: Double? = null,
    val previousWeight: Double? = null,
    val bmi: Double? = null,
    val bmiCategory: BmiCategory = BmiCategory.NORMAL,
    val weightLostFromMax: Double = 0.0,
    val personalBest: Double? = null,
    val predictedTargetDate: LocalDate? = null,
    val canReachTarget: Boolean = true,
    val weeklyRate: Double = 0.0,
    val measurementStreak: Int = 0,
    val totalMeasurements: Int = 0,
    val chartPoints: List<Pair<Long, Double>> = emptyList(),
    val earnedBadges: List<Badge> = emptyList()
)
