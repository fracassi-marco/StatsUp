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
        val monthCount = monthTrainings.size.toDouble()
        val monthElevation = monthTrainings.sumOf { it.totalElevationGain ?: 0.0 }
        val monthBestStreak = maxConsecutiveDays(monthTrainings).toDouble()
        val distancePct = if (monthlyDistanceGoalKm > 0) monthDistance / monthlyDistanceGoalKm else 0.0
        val trainingPct = if (monthlyTrainingGoal > 0) monthCount / monthlyTrainingGoal else 0.0

        return listOf(
            Badge(
                id = "monthly_first",
                name = "Prima uscita",
                description = "Almeno 1 allenamento nel mese",
                emoji = "👟",
                category = BadgeCategory.MONTHLY,
                earned = monthCount >= 1,
                currentValue = monthCount,
                targetValue = 1.0,
                unit = "uscite"
            ),
            Badge(
                id = "monthly_bronze",
                name = "50 km",
                description = "Distanza mensile ≥ 50 km",
                emoji = "🥉",
                category = BadgeCategory.MONTHLY,
                earned = monthDistance >= 50.0,
                currentValue = monthDistance,
                targetValue = 50.0,
                unit = "km"
            ),
            Badge(
                id = "monthly_silver",
                name = "100 km",
                description = "Distanza mensile ≥ 100 km",
                emoji = "🥈",
                category = BadgeCategory.MONTHLY,
                earned = monthDistance >= 100.0,
                currentValue = monthDistance,
                targetValue = 100.0,
                unit = "km"
            ),
            Badge(
                id = "monthly_gold",
                name = "150 km",
                description = "Distanza mensile ≥ 150 km",
                emoji = "🥇",
                category = BadgeCategory.MONTHLY,
                earned = monthDistance >= 150.0,
                currentValue = monthDistance,
                targetValue = 150.0,
                unit = "km"
            ),
            Badge(
                id = "monthly_diamond",
                name = "200 km",
                description = "Distanza mensile ≥ 200 km",
                emoji = "💎",
                category = BadgeCategory.MONTHLY,
                earned = monthDistance >= 200.0,
                currentValue = monthDistance,
                targetValue = 200.0,
                unit = "km"
            ),
            Badge(
                id = "monthly_goal_dist",
                name = "${monthlyDistanceGoalKm} km completati",
                description = "Hai raggiunto il tuo obiettivo mensile di $monthlyDistanceGoalKm km",
                emoji = "🎯",
                category = BadgeCategory.MONTHLY,
                earned = distancePct >= 1.0,
                currentValue = monthDistance,
                targetValue = monthlyDistanceGoalKm.toDouble(),
                unit = "km"
            ),
            Badge(
                id = "monthly_goal_freq",
                name = "$monthlyTrainingGoal uscite completate",
                description = "Hai raggiunto il tuo obiettivo di $monthlyTrainingGoal allenamenti nel mese",
                emoji = "✅",
                category = BadgeCategory.MONTHLY,
                earned = trainingPct >= 1.0,
                currentValue = monthCount,
                targetValue = monthlyTrainingGoal.toDouble(),
                unit = "uscite"
            ),
            Badge(
                id = "monthly_streak_week",
                name = "Settimana Perfetta",
                description = "7 allenamenti in 7 giorni consecutivi questo mese",
                emoji = "🔥",
                category = BadgeCategory.MONTHLY,
                earned = monthBestStreak >= 7,
                currentValue = monthBestStreak,
                targetValue = 7.0,
                unit = "giorni consecutivi"
            ),
            Badge(
                id = "monthly_elevation_2k",
                name = "Alpinista",
                description = "2,000 m di dislivello totale accumulato questo mese",
                emoji = "🏔️",
                category = BadgeCategory.MONTHLY,
                earned = monthElevation >= 2000.0,
                currentValue = monthElevation,
                targetValue = 2000.0,
                unit = "m"
            ),
            Badge(
                id = "monthly_elevation_5k",
                name = "Scalatore Pro",
                description = "5,000 m di dislivello totale accumulato questo mese",
                emoji = "🗻",
                category = BadgeCategory.MONTHLY,
                earned = monthElevation >= 5000.0,
                currentValue = monthElevation,
                targetValue = 5000.0,
                unit = "m"
            )
        )
    }

    private fun evaluateAnnual(trainings: List<Training>, now: ZonedDateTime): List<Badge> {
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
            Badge(
                id = "yearly_500km",
                name = "500 km",
                description = "Distanza annuale ≥ 500 km",
                emoji = "⭐",
                category = BadgeCategory.ANNUAL,
                earned = yearDistance >= 500.0,
                currentValue = yearDistance,
                targetValue = 500.0,
                unit = "km"
            ),
            Badge(
                id = "yearly_1000km",
                name = "1000 km",
                description = "Distanza annuale ≥ 1000 km",
                emoji = "🌟",
                category = BadgeCategory.ANNUAL,
                earned = yearDistance >= 1000.0,
                currentValue = yearDistance,
                targetValue = 1000.0,
                unit = "km"
            ),
            Badge(
                id = "yearly_2000km",
                name = "2000 km",
                description = "Distanza annuale ≥ 2000 km",
                emoji = "🚀",
                category = BadgeCategory.ANNUAL,
                earned = yearDistance >= 2000.0,
                currentValue = yearDistance,
                targetValue = 2000.0,
                unit = "km"
            ),
            Badge(
                id = "yearly_50act",
                name = "50 Uscite",
                description = "50 allenamenti nell'anno",
                emoji = "💪",
                category = BadgeCategory.ANNUAL,
                earned = yearCount >= 50,
                currentValue = yearCount,
                targetValue = 50.0,
                unit = "uscite"
            ),
            Badge(
                id = "yearly_100act",
                name = "100 Uscite",
                description = "100 allenamenti nell'anno",
                emoji = "🏋️",
                category = BadgeCategory.ANNUAL,
                earned = yearCount >= 100,
                currentValue = yearCount,
                targetValue = 100.0,
                unit = "uscite"
            ),
            Badge(
                id = "yearly_streak30",
                name = "30 giorni di fila",
                description = "Almeno 30 giorni consecutivi di allenamento in questo anno",
                emoji = "⚡",
                category = BadgeCategory.ANNUAL,
                earned = yearBestStreak >= 30,
                currentValue = yearBestStreak,
                targetValue = 30.0,
                unit = "giorni consecutivi"
            ),
            Badge(
                id = "yearly_all_months",
                name = "Anno senza sosta",
                description = "Almeno 1 uscita in ognuno dei mesi dell'anno",
                emoji = "📅",
                category = BadgeCategory.ANNUAL,
                earned = allMonthsCovered,
                currentValue = monthsCovered,
                targetValue = now.monthValue.toDouble(),
                unit = "mesi attivi"
            ),
            Badge(
                id = "yearly_everest",
                name = "Everest",
                description = "8,848 m di dislivello cumulativo in un anno — come scalare l'Everest",
                emoji = "🌋",
                category = BadgeCategory.ANNUAL,
                earned = yearElevation >= 8848.0,
                currentValue = yearElevation,
                targetValue = 8848.0,
                unit = "m"
            ),
            Badge(
                id = "yearly_elevation_20k",
                name = "Ultra Scalatore",
                description = "20,000 m di dislivello cumulativo in un anno",
                emoji = "🛸",
                category = BadgeCategory.ANNUAL,
                earned = yearElevation >= 20000.0,
                currentValue = yearElevation,
                targetValue = 20000.0,
                unit = "m"
            )
        )
    }

    private fun evaluateAllTime(trainings: List<Training>): List<Badge> {
        val totalCount = trainings.size.toDouble()
        val allTimeBestStreak = bestStreakOf(trainings).toDouble()
        val bestSingleDistanceKm = trainings.maxOfOrNull { it.distanceInKilometers() } ?: 0.0
        val bestSingleElevation = trainings.maxOfOrNull { it.totalElevationGain ?: 0.0 } ?: 0.0
        val maxAltitudeEver = trainings.maxOfOrNull { it.elevHigh ?: 0.0 } ?: 0.0

        return listOf(
            Badge(
                id = "alltime_first",
                name = "Primo passo",
                description = "Hai registrato il tuo primo allenamento",
                emoji = "🎬",
                category = BadgeCategory.ALL_TIME,
                earned = totalCount >= 1
            ),
            Badge(
                id = "alltime_halfmarathon",
                name = "Mezza Maratona",
                description = "Hai corso almeno 21.1 km in una singola uscita",
                emoji = "🏅",
                category = BadgeCategory.ALL_TIME,
                earned = bestSingleDistanceKm >= 21.097,
                currentValue = bestSingleDistanceKm,
                targetValue = 21.097,
                unit = "km (miglior uscita)"
            ),
            Badge(
                id = "alltime_marathon",
                name = "Maratona",
                description = "Hai corso almeno 42.2 km in una singola uscita",
                emoji = "🏆",
                category = BadgeCategory.ALL_TIME,
                earned = bestSingleDistanceKm >= 42.195,
                currentValue = bestSingleDistanceKm,
                targetValue = 42.195,
                unit = "km (miglior uscita)"
            ),
            Badge(
                id = "alltime_elevation1k",
                name = "Scalatore",
                description = "1,000 m di dislivello positivo in una singola uscita",
                emoji = "⛰️",
                category = BadgeCategory.ALL_TIME,
                earned = bestSingleElevation >= 1000.0,
                currentValue = bestSingleElevation,
                targetValue = 1000.0,
                unit = "m (miglior uscita)"
            ),
            Badge(
                id = "alltime_elevation2k",
                name = "Conquistatore",
                description = "2,000 m di dislivello positivo in una singola uscita",
                emoji = "🗻",
                category = BadgeCategory.ALL_TIME,
                earned = bestSingleElevation >= 2000.0,
                currentValue = bestSingleElevation,
                targetValue = 2000.0,
                unit = "m (miglior uscita)"
            ),
            Badge(
                id = "alltime_altitude2k",
                name = "Zona Alpina",
                description = "Hai raggiunto i 2,000 m di altitudine in una uscita",
                emoji = "❄️",
                category = BadgeCategory.ALL_TIME,
                earned = maxAltitudeEver >= 2000.0,
                currentValue = maxAltitudeEver,
                targetValue = 2000.0,
                unit = "m (quota max raggiunta)"
            ),
            Badge(
                id = "alltime_altitude3k",
                name = "Alta Quota",
                description = "Hai raggiunto i 3,000 m di altitudine in una uscita",
                emoji = "🌨️",
                category = BadgeCategory.ALL_TIME,
                earned = maxAltitudeEver >= 3000.0,
                currentValue = maxAltitudeEver,
                targetValue = 3000.0,
                unit = "m (quota max raggiunta)"
            ),
            Badge(
                id = "alltime_altitude4k",
                name = "Oltre le nuvole",
                description = "Hai raggiunto i 4,000 m di altitudine in una uscita",
                emoji = "✈️",
                category = BadgeCategory.ALL_TIME,
                earned = maxAltitudeEver >= 4000.0,
                currentValue = maxAltitudeEver,
                targetValue = 4000.0,
                unit = "m (quota max raggiunta)"
            ),
            Badge(
                id = "alltime_100act",
                name = "100 allenamenti",
                description = "Hai registrato 100 allenamenti in totale",
                emoji = "💯",
                category = BadgeCategory.ALL_TIME,
                earned = totalCount >= 100,
                currentValue = totalCount,
                targetValue = 100.0,
                unit = "allenamenti"
            ),
            Badge(
                id = "alltime_500act",
                name = "500 allenamenti",
                description = "Hai registrato 500 allenamenti in totale",
                emoji = "🛡️",
                category = BadgeCategory.ALL_TIME,
                earned = totalCount >= 500,
                currentValue = totalCount,
                targetValue = 500.0,
                unit = "allenamenti"
            ),
            Badge(
                id = "alltime_best_streak",
                name = "60 giorni di fila",
                description = "Hai mantenuto una striscia di 60 giorni consecutivi di allenamento",
                emoji = "👑",
                category = BadgeCategory.ALL_TIME,
                earned = allTimeBestStreak >= 60,
                currentValue = allTimeBestStreak,
                targetValue = 60.0,
                unit = "giorni consecutivi"
            )
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
