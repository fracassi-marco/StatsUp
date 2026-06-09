package com.statsup.domain

import java.time.ZonedDateTime

data class BestEffort(
    val label: String,
    val distanceMeters: Double,
    val timeSeconds: Int,
    val paceMinPerKm: Double,
    val trainingId: String,
    val date: ZonedDateTime
)
