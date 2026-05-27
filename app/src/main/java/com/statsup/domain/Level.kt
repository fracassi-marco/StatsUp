package com.statsup.domain

import androidx.annotation.StringRes

data class Level(
    val number: Int,
    @param:StringRes val nameResId: Int,
    val emoji: String,
    val totalXp: Int,
    val currentLevelXp: Int,
    val nextLevelXp: Int,
    val isDecaying: Boolean,
    val dailyDecayRate: Int,
    val daysSinceLastActivity: Int
) {
    val progress: Float get() = if (nextLevelXp > 0) currentLevelXp.toFloat() / nextLevelXp else 1f
    val isMaxLevel: Boolean get() = number == 20
}
