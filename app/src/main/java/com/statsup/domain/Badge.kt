package com.statsup.domain

enum class BadgeCategory { MONTHLY, ANNUAL, ALL_TIME }

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val category: BadgeCategory,
    val earned: Boolean,
    val currentValue: Double? = null,
    val targetValue: Double? = null,
    val unit: String? = null
) {
    val progress: Float get() = if (currentValue != null && targetValue != null && targetValue > 0)
        (currentValue / targetValue).toFloat().coerceIn(0f, 1f)
    else
        if (earned) 1f else 0f
}
