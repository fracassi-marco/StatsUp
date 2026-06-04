package com.statsup.domain

class WeightBadgesUseCase {

    operator fun invoke(
        entries: List<WeightEntry>,
        latestWeight: Double,
        targetKg: Double,
        heightCm: Int,
        streak: Int,
        bmi: Double?
    ): List<Badge> {
        if (entries.isEmpty()) return emptyList()
        // entries is already sorted by WeightStatsUseCase
        val maxWeight = entries.maxOf { it.weightKg }
        val lostFromMax = maxWeight - latestWeight
        val totalMeasurements = entries.size

        val now = System.currentTimeMillis()
        val daysSinceFirst = (now - entries.first().date) / 86_400_000.0
        val monthsSinceFirst = daysSinceFirst / 30.44

        val thirtyDaysAgo = now - 30L * 24 * 60 * 60 * 1000
        val recentEntries = entries.filter { it.date >= thirtyDaysAgo }
        val stableWeight = recentEntries.size >= 4 &&
            recentEntries.maxOf { it.weightKg } - recentEntries.minOf { it.weightKg } <= 1.0

        val maxBmi = if (heightCm > 0) {
            val hm = heightCm / 100.0
            maxWeight / (hm * hm)
        } else null
        val exitedObese = bmi != null && bmi < 30.0 && maxBmi != null && maxBmi >= 30.0

        // Period losses: positive value means weight was lost in that window
        val weeklyLoss = weightNDaysAgo(entries, 7, now)?.let { it - latestWeight }
        val monthlyLoss = weightNDaysAgo(entries, 30, now)?.let { it - latestWeight }
        val quarterlyLoss = weightNDaysAgo(entries, 90, now)?.let { it - latestWeight }

        return listOf(
            badge("weight_first", earned = true),
            badge("weight_minus1", earned = lostFromMax >= 1.0, currentValue = lostFromMax, targetValue = 1.0, unit = "kg"),
            badge("weight_minus2", earned = lostFromMax >= 2.0, currentValue = lostFromMax, targetValue = 2.0, unit = "kg"),
            badge("weight_minus5", earned = lostFromMax >= 5.0, currentValue = lostFromMax, targetValue = 5.0, unit = "kg"),
            badge("weight_minus10", earned = lostFromMax >= 10.0, currentValue = lostFromMax, targetValue = 10.0, unit = "kg"),
            badge("weight_minus20", earned = lostFromMax >= 20.0, currentValue = lostFromMax, targetValue = 20.0, unit = "kg"),
            badge("weight_target", earned = targetKg > 0 && latestWeight <= targetKg),
            badge("weight_week_1kg",
                earned = weeklyLoss != null && weeklyLoss >= 1.0,
                currentValue = if (weeklyLoss != null && weeklyLoss > 0) weeklyLoss else null,
                targetValue = 1.0, unit = "kg"),
            badge("weight_month_2kg",
                earned = monthlyLoss != null && monthlyLoss >= 2.0,
                currentValue = if (monthlyLoss != null && monthlyLoss > 0) monthlyLoss else null,
                targetValue = 2.0, unit = "kg"),
            badge("weight_month_4kg",
                earned = monthlyLoss != null && monthlyLoss >= 4.0,
                currentValue = if (monthlyLoss != null && monthlyLoss > 0) monthlyLoss else null,
                targetValue = 4.0, unit = "kg"),
            badge("weight_quarter_5kg",
                earned = quarterlyLoss != null && quarterlyLoss >= 5.0,
                currentValue = if (quarterlyLoss != null && quarterlyLoss > 0) quarterlyLoss else null,
                targetValue = 5.0, unit = "kg"),
            badge("weight_streak3", earned = streak >= 3, currentValue = streak.toDouble(), targetValue = 3.0, unit = "weeks"),
            badge("weight_streak7", earned = streak >= 7, currentValue = streak.toDouble(), targetValue = 7.0, unit = "weeks"),
            badge("weight_streak30", earned = streak >= 30, currentValue = streak.toDouble(), targetValue = 30.0, unit = "weeks"),
            badge("weight_6months", earned = daysSinceFirst >= 180, currentValue = monthsSinceFirst.coerceAtMost(6.0), targetValue = 6.0, unit = "months"),
            badge("weight_1year", earned = daysSinceFirst >= 365, currentValue = monthsSinceFirst.coerceAtMost(12.0), targetValue = 12.0, unit = "months"),
            badge("weight_100", earned = totalMeasurements >= 100, currentValue = totalMeasurements.toDouble(), targetValue = 100.0, unit = "entries"),
            badge("weight_bmi_normal", earned = bmi != null && bmi < 25.0 && heightCm > 0),
            badge("weight_bmi_below30", earned = exitedObese),
            badge("weight_stable", earned = stableWeight)
        ).filter { it.earned || (it.currentValue != null && it.currentValue > 0) }
    }

    // Returns the weight of the most recent entry at or before `daysAgo` days ago.
    private fun weightNDaysAgo(entries: List<WeightEntry>, daysAgo: Int, now: Long): Double? =
        entries.lastOrNull { it.date <= now - daysAgo * 86_400_000L }?.weightKg

    private fun badge(
        id: String,
        earned: Boolean,
        currentValue: Double? = null,
        targetValue: Double? = null,
        unit: String? = null
    ) = Badge(
        id = id,
        name = id,
        description = id,
        emoji = emojiFor(id),
        category = BadgeCategory.ALL_TIME,
        earned = earned,
        currentValue = currentValue,
        targetValue = targetValue,
        unit = unit
    )

    private fun emojiFor(id: String) = when (id) {
        "weight_first" -> "🎯"
        "weight_minus1" -> "⬇️"
        "weight_minus2" -> "📉"
        "weight_minus5" -> "💪"
        "weight_minus10" -> "🏋️"
        "weight_minus20" -> "🌟"
        "weight_target" -> "🏆"
        "weight_streak3" -> "🗓️"
        "weight_streak7" -> "📅"
        "weight_streak30" -> "🔥"
        "weight_6months" -> "📆"
        "weight_1year" -> "🎂"
        "weight_100" -> "💯"
        "weight_bmi_normal" -> "🔄"
        "weight_bmi_below30" -> "⚖️"
        "weight_stable" -> "⚓"
        "weight_week_1kg" -> "⚡"
        "weight_month_2kg" -> "🌙"
        "weight_month_4kg" -> "💫"
        "weight_quarter_5kg" -> "🎖️"
        else -> "⭐"
    }
}
