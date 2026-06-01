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
        val maxWeight = entries.maxOf { it.weightKg }
        val lostFromMax = maxWeight - latestWeight
        val totalMeasurements = entries.size

        return listOf(
            badge("weight_first", earned = true),
            badge("weight_minus1", earned = lostFromMax >= 1.0, currentValue = lostFromMax, targetValue = 1.0, unit = "kg"),
            badge("weight_minus5", earned = lostFromMax >= 5.0, currentValue = lostFromMax, targetValue = 5.0, unit = "kg"),
            badge("weight_minus10", earned = lostFromMax >= 10.0, currentValue = lostFromMax, targetValue = 10.0, unit = "kg"),
            badge("weight_minus20", earned = lostFromMax >= 20.0, currentValue = lostFromMax, targetValue = 20.0, unit = "kg"),
            badge("weight_target", earned = targetKg > 0 && latestWeight <= targetKg),
            badge("weight_streak7", earned = streak >= 7, currentValue = streak.toDouble(), targetValue = 7.0, unit = "weeks"),
            badge("weight_100", earned = totalMeasurements >= 100, currentValue = totalMeasurements.toDouble(), targetValue = 100.0, unit = "entries"),
            badge("weight_bmi_normal", earned = bmi != null && bmi < 25.0 && heightCm > 0)
        ).filter { it.earned || (it.currentValue != null && it.currentValue > 0) }
    }

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
        "weight_minus5" -> "💪"
        "weight_minus10" -> "🏋️"
        "weight_minus20" -> "🌟"
        "weight_target" -> "🏆"
        "weight_streak7" -> "📅"
        "weight_100" -> "💯"
        "weight_bmi_normal" -> "🔄"
        else -> "⭐"
    }
}
