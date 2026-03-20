package com.statsup.domain

enum class BadgeCategory { MONTHLY, ANNUAL, ALL_TIME }

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val category: BadgeCategory,
    val earned: Boolean
)
