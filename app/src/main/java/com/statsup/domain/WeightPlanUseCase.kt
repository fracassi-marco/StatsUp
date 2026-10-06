package com.statsup.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class WeightPlan(
    val windowStart: LocalDate,
    val windowEnd: LocalDate,
    val targetDate: LocalDate,
    val targetKg: Double,
    /** True when the plan is about losing weight (target below the starting weight). */
    val isLossGoal: Boolean,
    /** One planned weight per day of the window, from [windowStart] to [windowEnd]. */
    val plannedPoints: List<Pair<LocalDate, Double>>,
    /** Actual measurements inside the window (last measurement of each day). */
    val actualPoints: List<Pair<LocalDate, Double>>,
    /** Weight the plan expects today. */
    val plannedToday: Double,
    /** Latest actual weight minus [plannedToday]. */
    val deltaFromPlan: Double,
    /** Weekly rate (kg/week) required from the latest measurement to hit the target on time. */
    val requiredWeeklyRate: Double?
)

/**
 * Builds the "ideal path" to the weight target: a straight line from the weight measured
 * when the plan started ([planStart]) to [targetKg] on [targetDate], compared with the
 * actual measurements of the last [windowDays] days.
 */
class WeightPlanUseCase {

    operator fun invoke(
        entries: List<WeightEntry>,
        targetKg: Double,
        targetDate: LocalDate?,
        planStart: LocalDate?,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault(),
        windowDays: Int = 30
    ): WeightPlan? {
        if (targetKg <= 0 || targetDate == null || entries.isEmpty()) return null

        val byDay = entries
            .sortedBy { it.date }
            .map { Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() to it.weightKg }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.last() }
            .toSortedMap()

        val start = (planStart ?: today).coerceAtMost(targetDate)
        val startKg = byDay.headMap(start.plusDays(1)).values.lastOrNull() ?: byDay.values.first()

        val totalDays = ChronoUnit.DAYS.between(start, targetDate).toDouble()
        fun plannedAt(day: LocalDate): Double {
            if (totalDays <= 0 || !day.isBefore(targetDate)) return targetKg
            val elapsed = ChronoUnit.DAYS.between(start, day).toDouble()
            return startKg + (targetKg - startKg) * (elapsed / totalDays)
        }

        val windowStart = today.minusDays((windowDays - 1).toLong())
        val planned = (0 until windowDays).map { i ->
            val day = windowStart.plusDays(i.toLong())
            day to plannedAt(day)
        }
        val actual = byDay.filterKeys { !it.isBefore(windowStart) && !it.isAfter(today) }.toList()

        val latestKg = byDay.values.last()
        val plannedToday = plannedAt(today)
        val daysLeft = ChronoUnit.DAYS.between(today, targetDate)
        val requiredWeeklyRate = if (daysLeft > 0) (targetKg - latestKg) / daysLeft * 7 else null

        return WeightPlan(
            windowStart = windowStart,
            windowEnd = today,
            targetDate = targetDate,
            targetKg = targetKg,
            isLossGoal = targetKg < startKg,
            plannedPoints = planned,
            actualPoints = actual,
            plannedToday = plannedToday,
            deltaFromPlan = latestKg - plannedToday,
            requiredWeeklyRate = requiredWeeklyRate
        )
    }
}
