package com.statsup.domain

import java.time.ZonedDateTime

class EvaluateBadgesUseCase {

    operator fun invoke(
        trainings: List<Training>,
        monthlyDistanceGoalKm: Int,
        monthlyTrainingGoal: Int,
        now: ZonedDateTime = ZonedDateTime.now()
    ): List<Badge> {
        return evaluateMonthly(trainings, monthlyDistanceGoalKm, monthlyTrainingGoal, now) +
            evaluateAnnual(trainings, now) +
            evaluateAllTime(trainings)
    }

    private fun evaluateMonthly(
        trainings: List<Training>,
        monthlyDistanceGoalKm: Int,
        monthlyTrainingGoal: Int,
        now: ZonedDateTime
    ): List<Badge> {
        val monthTrainings = trainings.filter {
            it.date.month == now.month && it.date.year == now.year
        }
        val monthDistance = monthTrainings.sumOf { it.distanceInKilometers() }
        val monthCount = monthTrainings.size
        val monthElevation = monthTrainings.sumOf { it.totalElevationGain ?: 0.0 }
        val distancePct = if (monthlyDistanceGoalKm > 0) monthDistance / monthlyDistanceGoalKm else 0.0
        val trainingPct = if (monthlyTrainingGoal > 0) monthCount.toDouble() / monthlyTrainingGoal else 0.0

        return listOf(
            Badge(
                id = "monthly_first",
                name = "Prima uscita",
                description = "Almeno 1 allenamento nel mese",
                emoji = "👟",
                category = BadgeCategory.MONTHLY,
                earned = monthCount >= 1
            ),
            Badge(
                id = "monthly_bronze",
                name = "50 km",
                description = "Distanza mensile ≥ 50 km",
                emoji = "🥉",
                category = BadgeCategory.MONTHLY,
                earned = monthDistance >= 50.0
            ),
            Badge(
                id = "monthly_silver",
                name = "100 km",
                description = "Distanza mensile ≥ 100 km",
                emoji = "🥈",
                category = BadgeCategory.MONTHLY,
                earned = monthDistance >= 100.0
            ),
            Badge(
                id = "monthly_gold",
                name = "150 km",
                description = "Distanza mensile ≥ 150 km",
                emoji = "🥇",
                category = BadgeCategory.MONTHLY,
                earned = monthDistance >= 150.0
            ),
            Badge(
                id = "monthly_diamond",
                name = "200 km",
                description = "Distanza mensile ≥ 200 km",
                emoji = "💎",
                category = BadgeCategory.MONTHLY,
                earned = monthDistance >= 200.0
            ),
            Badge(
                id = "monthly_goal_dist",
                name = "Goal Distanza",
                description = "Goal distanza mensile raggiunto",
                emoji = "🎯",
                category = BadgeCategory.MONTHLY,
                earned = distancePct >= 1.0
            ),
            Badge(
                id = "monthly_goal_freq",
                name = "Goal Frequenza",
                description = "Goal frequenza mensile raggiunto",
                emoji = "✅",
                category = BadgeCategory.MONTHLY,
                earned = trainingPct >= 1.0
            ),
            Badge(
                id = "monthly_streak_week",
                name = "Settimana Perfetta",
                description = "7 giorni consecutivi nel mese",
                emoji = "🔥",
                category = BadgeCategory.MONTHLY,
                earned = hasConsecutiveDays(monthTrainings, 7)
            ),
            Badge(
                id = "monthly_elevation_2k",
                name = "Alpinista",
                description = "Dislivello mensile totale ≥ 2,000 m",
                emoji = "🏔️",
                category = BadgeCategory.MONTHLY,
                earned = monthElevation >= 2000.0
            ),
            Badge(
                id = "monthly_elevation_5k",
                name = "Scalatore Pro",
                description = "Dislivello mensile totale ≥ 5,000 m",
                emoji = "🗻",
                category = BadgeCategory.MONTHLY,
                earned = monthElevation >= 5000.0
            )
        )
    }

    private fun evaluateAnnual(trainings: List<Training>, now: ZonedDateTime): List<Badge> {
        val yearTrainings = trainings.filter { it.date.year == now.year }
        val yearDistance = yearTrainings.sumOf { it.distanceInKilometers() }
        val yearCount = yearTrainings.size
        val yearElevation = yearTrainings.sumOf { it.totalElevationGain ?: 0.0 }
        val yearBestStreak = bestStreakOf(yearTrainings)
        val allMonthsCovered = (1..now.monthValue).all { month ->
            yearTrainings.any { it.date.monthValue == month }
        }

        return listOf(
            Badge(
                id = "yearly_500km",
                name = "500 km",
                description = "Distanza annuale ≥ 500 km",
                emoji = "⭐",
                category = BadgeCategory.ANNUAL,
                earned = yearDistance >= 500.0
            ),
            Badge(
                id = "yearly_1000km",
                name = "1000 km",
                description = "Distanza annuale ≥ 1000 km",
                emoji = "🌟",
                category = BadgeCategory.ANNUAL,
                earned = yearDistance >= 1000.0
            ),
            Badge(
                id = "yearly_2000km",
                name = "2000 km",
                description = "Distanza annuale ≥ 2000 km",
                emoji = "🚀",
                category = BadgeCategory.ANNUAL,
                earned = yearDistance >= 2000.0
            ),
            Badge(
                id = "yearly_50act",
                name = "50 Uscite",
                description = "50 allenamenti nell'anno",
                emoji = "💪",
                category = BadgeCategory.ANNUAL,
                earned = yearCount >= 50
            ),
            Badge(
                id = "yearly_100act",
                name = "100 Uscite",
                description = "100 allenamenti nell'anno",
                emoji = "🏋️",
                category = BadgeCategory.ANNUAL,
                earned = yearCount >= 100
            ),
            Badge(
                id = "yearly_streak30",
                name = "Streak 30 gg",
                description = "Streak annuale ≥ 30 giorni",
                emoji = "⚡",
                category = BadgeCategory.ANNUAL,
                earned = yearBestStreak >= 30
            ),
            Badge(
                id = "yearly_all_months",
                name = "Tutti i mesi",
                description = "Almeno 1 uscita ogni mese dell'anno",
                emoji = "📅",
                category = BadgeCategory.ANNUAL,
                earned = allMonthsCovered
            ),
            Badge(
                id = "yearly_everest",
                name = "Everest",
                description = "Dislivello annuale cumulativo ≥ 8,848 m",
                emoji = "🌋",
                category = BadgeCategory.ANNUAL,
                earned = yearElevation >= 8848.0
            ),
            Badge(
                id = "yearly_elevation_20k",
                name = "Ultra Scalatore",
                description = "Dislivello annuale cumulativo ≥ 20,000 m",
                emoji = "🛸",
                category = BadgeCategory.ANNUAL,
                earned = yearElevation >= 20000.0
            )
        )
    }

    private fun evaluateAllTime(trainings: List<Training>): List<Badge> {
        val totalCount = trainings.size
        val allTimeBestStreak = bestStreakOf(trainings)
        val hasSingleHalfMarathon = trainings.any { it.distanceInKilometers() >= 21.097 }
        val hasSingleMarathon = trainings.any { it.distanceInKilometers() >= 42.195 }
        val hasElevation1k = trainings.any { (it.totalElevationGain ?: 0.0) >= 1000.0 }
        val hasElevation2k = trainings.any { (it.totalElevationGain ?: 0.0) >= 2000.0 }
        val hasAltitude2k = trainings.any { (it.elevHigh ?: 0.0) >= 2000.0 }
        val hasAltitude3k = trainings.any { (it.elevHigh ?: 0.0) >= 3000.0 }
        val hasAltitude4k = trainings.any { (it.elevHigh ?: 0.0) >= 4000.0 }

        return listOf(
            Badge(
                id = "alltime_first",
                name = "Prima Attività",
                description = "Ha registrato almeno 1 allenamento",
                emoji = "🎬",
                category = BadgeCategory.ALL_TIME,
                earned = totalCount >= 1
            ),
            Badge(
                id = "alltime_halfmarathon",
                name = "Mezza Maratona",
                description = "Distanza singola ≥ 21.1 km",
                emoji = "🏅",
                category = BadgeCategory.ALL_TIME,
                earned = hasSingleHalfMarathon
            ),
            Badge(
                id = "alltime_marathon",
                name = "Maratona",
                description = "Distanza singola ≥ 42.2 km",
                emoji = "🏆",
                category = BadgeCategory.ALL_TIME,
                earned = hasSingleMarathon
            ),
            Badge(
                id = "alltime_elevation1k",
                name = "Scalatore",
                description = "Dislivello singola uscita ≥ 1,000 m",
                emoji = "⛰️",
                category = BadgeCategory.ALL_TIME,
                earned = hasElevation1k
            ),
            Badge(
                id = "alltime_elevation2k",
                name = "Conquistatore",
                description = "Dislivello singola uscita ≥ 2,000 m",
                emoji = "🗻",
                category = BadgeCategory.ALL_TIME,
                earned = hasElevation2k
            ),
            Badge(
                id = "alltime_altitude2k",
                name = "Zona Alpina",
                description = "Altitudine massima ≥ 2,000 m in una uscita",
                emoji = "❄️",
                category = BadgeCategory.ALL_TIME,
                earned = hasAltitude2k
            ),
            Badge(
                id = "alltime_altitude3k",
                name = "Quota Estrema",
                description = "Altitudine massima ≥ 3,000 m in una uscita",
                emoji = "🌨️",
                category = BadgeCategory.ALL_TIME,
                earned = hasAltitude3k
            ),
            Badge(
                id = "alltime_altitude4k",
                name = "Volo Radente",
                description = "Altitudine massima ≥ 4,000 m in una uscita",
                emoji = "✈️",
                category = BadgeCategory.ALL_TIME,
                earned = hasAltitude4k
            ),
            Badge(
                id = "alltime_100act",
                name = "Centenario",
                description = "100 allenamenti totali",
                emoji = "💯",
                category = BadgeCategory.ALL_TIME,
                earned = totalCount >= 100
            ),
            Badge(
                id = "alltime_500act",
                name = "Veterano",
                description = "500 allenamenti totali",
                emoji = "🛡️",
                category = BadgeCategory.ALL_TIME,
                earned = totalCount >= 500
            ),
            Badge(
                id = "alltime_best_streak",
                name = "Streak Leggenda",
                description = "Best streak all-time ≥ 60 giorni",
                emoji = "👑",
                category = BadgeCategory.ALL_TIME,
                earned = allTimeBestStreak >= 60
            )
        )
    }

    private fun hasConsecutiveDays(trainings: List<Training>, days: Int): Boolean {
        val distinctDays = trainings.map { it.date.dayOfMonth }.toSet().sorted()
        if (distinctDays.size < days) return false
        var current = 1
        for (i in 1 until distinctDays.size) {
            if (distinctDays[i] == distinctDays[i - 1] + 1) {
                current++
                if (current >= days) return true
            } else {
                current = 1
            }
        }
        return current >= days
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
