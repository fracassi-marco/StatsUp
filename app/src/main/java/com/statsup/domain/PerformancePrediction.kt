package com.statsup.domain

data class PerformancePrediction(
    val label: String,
    val distanceMeters: Double,
    val timeSeconds: Int,
    val paceMinPerKm: Double
)
