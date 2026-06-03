package com.statsup.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class WeightStatsUseCase {

    operator fun invoke(entries: List<WeightEntry>, heightCm: Int, targetKg: Double): WeightStats {
        if (entries.isEmpty()) return WeightStats()

        val sorted = entries.sortedBy { it.date }
        val latestWeight = sorted.last().weightKg
        val previousWeight = if (sorted.size >= 2) sorted[sorted.size - 2].weightKg else null
        val personalBest = sorted.minOf { it.weightKg }
        val maxWeight = sorted.maxOf { it.weightKg }
        val weightLostFromMax = maxWeight - latestWeight

        val bmi = if (heightCm > 0) {
            val hm = heightCm / 100.0
            latestWeight / (hm * hm)
        } else null

        val bmiCategory = bmi?.let { bmiCategory(it) } ?: BmiCategory.NORMAL

        val chartPoints = generateDailyPoints(sorted)

        val regressionWindow = sorted.takeLast(60)
        val (slope, _) = linearRegression(regressionWindow)
        val weeklyRate = slope * 7

        val (predictedTargetDate, canReachTarget) = if (targetKg > 0 && sorted.size >= 2) {
            predictTargetDate(latestWeight, targetKg, slope)
        } else Pair(null, true)

        val measurementStreak = computeStreak(sorted)

        val badges = WeightBadgesUseCase()(sorted, latestWeight, targetKg, heightCm, measurementStreak, bmi)

        return WeightStats(
            latestWeight = latestWeight,
            previousWeight = previousWeight,
            bmi = bmi,
            bmiCategory = bmiCategory,
            weightLostFromMax = weightLostFromMax,
            personalBest = personalBest,
            maxWeight = maxWeight,
            predictedTargetDate = predictedTargetDate,
            canReachTarget = canReachTarget,
            weeklyRate = weeklyRate,
            measurementStreak = measurementStreak,
            totalMeasurements = sorted.size,
            chartPoints = chartPoints,
            earnedBadges = badges
        )
    }

    private fun bmiCategory(bmi: Double): BmiCategory = when {
        bmi < 18.5 -> BmiCategory.UNDERWEIGHT
        bmi < 25.0 -> BmiCategory.NORMAL
        bmi < 30.0 -> BmiCategory.OVERWEIGHT
        bmi < 35.0 -> BmiCategory.OBESE_1
        bmi < 40.0 -> BmiCategory.OBESE_2
        else -> BmiCategory.OBESE_3
    }

    private fun linearRegression(entries: List<WeightEntry>): Pair<Double, Double> {
        if (entries.size < 2) return Pair(0.0, entries.firstOrNull()?.weightKg ?: 0.0)
        val firstDate = entries.first().date
        val n = entries.size.toDouble()
        val xs = entries.map { (it.date - firstDate) / 86400000.0 }
        val ys = entries.map { it.weightKg }
        val sumX = xs.sum()
        val sumY = ys.sum()
        val sumXY = xs.zip(ys).sumOf { (x, y) -> x * y }
        val sumX2 = xs.sumOf { it * it }
        val denom = n * sumX2 - sumX * sumX
        if (abs(denom) < 1e-10) return Pair(0.0, sumY / n)
        val slope = (n * sumXY - sumX * sumY) / denom
        val intercept = (sumY - slope * sumX) / n
        return Pair(slope, intercept)
    }

    private fun predictTargetDate(currentWeight: Double, targetKg: Double, slopeKgPerDay: Double): Pair<LocalDate?, Boolean> {
        val diff = targetKg - currentWeight
        if (abs(slopeKgPerDay) < 1e-6) return Pair(null, false)
        if (diff < 0 && slopeKgPerDay >= 0) return Pair(null, false)
        if (diff > 0 && slopeKgPerDay <= 0) return Pair(null, false)
        val daysToTarget = (diff / slopeKgPerDay).toLong()
        if (daysToTarget < 0 || daysToTarget > 3650) return Pair(null, false)
        val predicted = LocalDate.now().plusDays(daysToTarget)
        return Pair(predicted, true)
    }

    private fun computeStreak(sorted: List<WeightEntry>): Int {
        if (sorted.isEmpty()) return 0
        var streak = 1
        var currentWeek = weekOf(sorted.last().date)
        for (i in sorted.indices.reversed().drop(1)) {
            val week = weekOf(sorted[i].date)
            val expectedPrevWeek = currentWeek - 1
            if (week == expectedPrevWeek) {
                streak++
                currentWeek = week
            } else if (week < expectedPrevWeek) {
                break
            }
        }
        return streak
    }

    private fun generateDailyPoints(sorted: List<WeightEntry>): List<Pair<Long, Double>> {
        if (sorted.size < 2) return sorted.map { Pair(it.date, it.weightKg) }
        val zone = ZoneId.systemDefault()
        val entries = sorted.map {
            Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() to it.weightKg
        }
        val firstDate = entries.first().first
        val totalDays = ChronoUnit.DAYS.between(firstDate, entries.last().first).toInt()
        val result = ArrayList<Pair<Long, Double>>(totalDays + 1)
        var seg = 0
        for (day in 0..totalDays) {
            val current = firstDate.plusDays(day.toLong())
            val millis = current.atStartOfDay(zone).toInstant().toEpochMilli()
            while (seg + 1 < entries.size - 1 && entries[seg + 1].first <= current) seg++
            val (prevDate, prevKg) = entries[seg]
            val (nextDate, nextKg) = entries[seg + 1]
            val weight = if (current <= prevDate) {
                prevKg
            } else if (current >= nextDate) {
                nextKg
            } else {
                val span = ChronoUnit.DAYS.between(prevDate, nextDate).toDouble()
                val offset = ChronoUnit.DAYS.between(prevDate, current).toDouble()
                prevKg + (nextKg - prevKg) * (offset / span)
            }
            result.add(Pair(millis, weight))
        }
        return result
    }

    private fun weekOf(epochMillis: Long): Long {
        val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return ChronoUnit.WEEKS.between(LocalDate.ofEpochDay(0), date)
    }
}
