package com.statsup.domain

import java.time.ZonedDateTime

class EvaluateBadgesUseCase {

    operator fun invoke(
        trainings: List<Training>,
        monthlyDistanceGoalKm: Int,
        monthlyTrainingGoal: Int,
        strings: Map<String, BadgeStringSet>,
        now: ZonedDateTime = ZonedDateTime.now()
    ): List<Badge> {
        return evaluateMonthly(trainings, monthlyDistanceGoalKm, monthlyTrainingGoal, strings, now) +
            evaluateAnnual(trainings, strings, now) +
            evaluateAllTime(trainings, strings)
    }

    private fun s(strings: Map<String, BadgeStringSet>, id: String) =
        strings[id] ?: BadgeStringSet(id, "")

    private fun evaluateMonthly(
        trainings: List<Training>,
        monthlyDistanceGoalKm: Int,
        monthlyTrainingGoal: Int,
        strings: Map<String, BadgeStringSet>,
        now: ZonedDateTime
    ): List<Badge> {
        val monthTrainings = trainings.filter {
            it.date.month == now.month && it.date.year == now.year
        }
        val monthDistance = monthTrainings.sumOf { it.distanceInKilometers() }
        val monthCount = monthTrainings.size.toDouble()
        val monthElevation = monthTrainings.sumOf { it.totalElevationGain ?: 0.0 }
        val monthBestStreak = maxConsecutiveDays(monthTrainings).toDouble()
        val distancePct = if (monthlyDistanceGoalKm > 0) monthDistance / monthlyDistanceGoalKm else 0.0
        val trainingPct = if (monthlyTrainingGoal > 0) monthCount / monthlyTrainingGoal else 0.0

        return listOf(
            badge("monthly_first", strings, "👟", BadgeCategory.MONTHLY,
                earned = monthCount >= 1, currentValue = monthCount, targetValue = 1.0),
            badge("monthly_bronze", strings, "🥉", BadgeCategory.MONTHLY,
                earned = monthDistance >= 50.0, currentValue = monthDistance, targetValue = 50.0),
            badge("monthly_silver", strings, "🥈", BadgeCategory.MONTHLY,
                earned = monthDistance >= 100.0, currentValue = monthDistance, targetValue = 100.0),
            badge("monthly_gold", strings, "🥇", BadgeCategory.MONTHLY,
                earned = monthDistance >= 150.0, currentValue = monthDistance, targetValue = 150.0),
            badge("monthly_diamond", strings, "💎", BadgeCategory.MONTHLY,
                earned = monthDistance >= 200.0, currentValue = monthDistance, targetValue = 200.0),
            badge("monthly_goal_dist", strings, "🎯", BadgeCategory.MONTHLY,
                earned = distancePct >= 1.0, currentValue = monthDistance, targetValue = monthlyDistanceGoalKm.toDouble()),
            badge("monthly_goal_freq", strings, "✅", BadgeCategory.MONTHLY,
                earned = trainingPct >= 1.0, currentValue = monthCount, targetValue = monthlyTrainingGoal.toDouble()),
            badge("monthly_streak_week", strings, "🔥", BadgeCategory.MONTHLY,
                earned = monthBestStreak >= 7, currentValue = monthBestStreak, targetValue = 7.0),
            badge("monthly_elevation_2k", strings, "🏔️", BadgeCategory.MONTHLY,
                earned = monthElevation >= 2000.0, currentValue = monthElevation, targetValue = 2000.0),
            badge("monthly_elevation_5k", strings, "🗻", BadgeCategory.MONTHLY,
                earned = monthElevation >= 5000.0, currentValue = monthElevation, targetValue = 5000.0)
        )
    }

    private fun evaluateAnnual(
        trainings: List<Training>,
        strings: Map<String, BadgeStringSet>,
        now: ZonedDateTime
    ): List<Badge> {
        val yearTrainings = trainings.filter { it.date.year == now.year }
        val yearDistance = yearTrainings.sumOf { it.distanceInKilometers() }
        val yearCount = yearTrainings.size.toDouble()
        val yearElevation = yearTrainings.sumOf { it.totalElevationGain ?: 0.0 }
        val yearBestStreak = bestStreakOf(yearTrainings).toDouble()
        val monthsCovered = (1..now.monthValue).count { month ->
            yearTrainings.any { it.date.monthValue == month }
        }.toDouble()
        val allMonthsCovered = monthsCovered >= now.monthValue

        return listOf(
            badge("yearly_500km", strings, "⭐", BadgeCategory.ANNUAL,
                earned = yearDistance >= 500.0, currentValue = yearDistance, targetValue = 500.0),
            badge("yearly_1000km", strings, "🌟", BadgeCategory.ANNUAL,
                earned = yearDistance >= 1000.0, currentValue = yearDistance, targetValue = 1000.0),
            badge("yearly_2000km", strings, "🚀", BadgeCategory.ANNUAL,
                earned = yearDistance >= 2000.0, currentValue = yearDistance, targetValue = 2000.0),
            badge("yearly_50act", strings, "💪", BadgeCategory.ANNUAL,
                earned = yearCount >= 50, currentValue = yearCount, targetValue = 50.0),
            badge("yearly_100act", strings, "🏋️", BadgeCategory.ANNUAL,
                earned = yearCount >= 100, currentValue = yearCount, targetValue = 100.0),
            badge("yearly_streak30", strings, "⚡", BadgeCategory.ANNUAL,
                earned = yearBestStreak >= 30, currentValue = yearBestStreak, targetValue = 30.0),
            badge("yearly_all_months", strings, "📅", BadgeCategory.ANNUAL,
                earned = allMonthsCovered, currentValue = monthsCovered, targetValue = now.monthValue.toDouble()),
            badge("yearly_everest", strings, "🌋", BadgeCategory.ANNUAL,
                earned = yearElevation >= 8848.0, currentValue = yearElevation, targetValue = 8848.0),
            badge("yearly_elevation_20k", strings, "🛸", BadgeCategory.ANNUAL,
                earned = yearElevation >= 20000.0, currentValue = yearElevation, targetValue = 20000.0)
        )
    }

    private fun evaluateAllTime(
        trainings: List<Training>,
        strings: Map<String, BadgeStringSet>
    ): List<Badge> {
        val totalCount = trainings.size.toDouble()
        val allTimeBestStreak = bestStreakOf(trainings).toDouble()
        val bestSingleDistanceKm = trainings.maxOfOrNull { it.distanceInKilometers() } ?: 0.0
        val bestSingleElevation = trainings.maxOfOrNull { it.totalElevationGain ?: 0.0 } ?: 0.0
        val maxAltitudeEver = trainings.maxOfOrNull { it.elevHigh ?: 0.0 } ?: 0.0

        return listOf(
            badge("alltime_first", strings, "🎬", BadgeCategory.ALL_TIME,
                earned = totalCount >= 1),
            badge("alltime_halfmarathon", strings, "🏅", BadgeCategory.ALL_TIME,
                earned = bestSingleDistanceKm >= 21.097, currentValue = bestSingleDistanceKm, targetValue = 21.097),
            badge("alltime_marathon", strings, "🏆", BadgeCategory.ALL_TIME,
                earned = bestSingleDistanceKm >= 42.195, currentValue = bestSingleDistanceKm, targetValue = 42.195),
            badge("alltime_elevation1k", strings, "⛰️", BadgeCategory.ALL_TIME,
                earned = bestSingleElevation >= 1000.0, currentValue = bestSingleElevation, targetValue = 1000.0),
            badge("alltime_elevation2k", strings, "🗻", BadgeCategory.ALL_TIME,
                earned = bestSingleElevation >= 2000.0, currentValue = bestSingleElevation, targetValue = 2000.0),
            badge("alltime_altitude2k", strings, "❄️", BadgeCategory.ALL_TIME,
                earned = maxAltitudeEver >= 2000.0, currentValue = maxAltitudeEver, targetValue = 2000.0),
            badge("alltime_altitude3k", strings, "🌨️", BadgeCategory.ALL_TIME,
                earned = maxAltitudeEver >= 3000.0, currentValue = maxAltitudeEver, targetValue = 3000.0),
            badge("alltime_altitude4k", strings, "✈️", BadgeCategory.ALL_TIME,
                earned = maxAltitudeEver >= 4000.0, currentValue = maxAltitudeEver, targetValue = 4000.0),
            badge("alltime_100act", strings, "💯", BadgeCategory.ALL_TIME,
                earned = totalCount >= 100, currentValue = totalCount, targetValue = 100.0),
            badge("alltime_500act", strings, "🛡️", BadgeCategory.ALL_TIME,
                earned = totalCount >= 500, currentValue = totalCount, targetValue = 500.0),
            badge("alltime_best_streak", strings, "👑", BadgeCategory.ALL_TIME,
                earned = allTimeBestStreak >= 60, currentValue = allTimeBestStreak, targetValue = 60.0)
        )
    }

    private fun badge(
        id: String,
        strings: Map<String, BadgeStringSet>,
        emoji: String,
        category: BadgeCategory,
        earned: Boolean,
        currentValue: Double? = null,
        targetValue: Double? = null
    ): Badge {
        val str = s(strings, id)
        return Badge(
            id = id,
            name = str.name,
            description = str.description,
            emoji = emoji,
            category = category,
            earned = earned,
            currentValue = currentValue,
            targetValue = targetValue,
            unit = str.unit
        )
    }

    private fun maxConsecutiveDays(trainings: List<Training>): Int {
        val distinctDays = trainings.map { it.date.dayOfMonth }.toSet().sorted()
        if (distinctDays.isEmpty()) return 0
        var best = 1
        var current = 1
        for (i in 1 until distinctDays.size) {
            if (distinctDays[i] == distinctDays[i - 1] + 1) {
                current++
                if (current > best) best = current
            } else {
                current = 1
            }
        }
        return best
    }

    private fun bestStreakOf(trainings: List<Training>): Int {
        if (trainings.isEmpty()) return 0
        val sortedDays = trainings.map { it.date.toLocalDate() }.distinct().sorted()
        if (sortedDays.isEmpty()) return 0
        var best = 1
        var current = 1
        for (i in 1 until sortedDays.size) {
            if (sortedDays[i] == sortedDays[i - 1].plusDays(1)) {
                current++
                if (current > best) best = current
            } else {
                current = 1
            }
        }
        return best
    }
}
