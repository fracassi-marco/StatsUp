package com.statsup.domain

data class BookmarkedTrainingWithDetails(
    val training: Training,
    val customTitle: String,
    val difficulty: String,
    val note: String
)

