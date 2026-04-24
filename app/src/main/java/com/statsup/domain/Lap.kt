package com.statsup.domain

data class Lap(
    val split: Int,
    val distance: Double,
    val movingTime: Int,
    val elapsedTime: Int,
    val elevationDifference: Double? = null,
    val averageSpeed: Double? = null,
    val averageHeartrate: Double? = null,
    val paceZone: Int? = null
) {
    val distanceKm: Double get() = distance / 1000.0

    fun pace(): Double = if (distanceKm > 0) movingTime / distanceKm / 60.0 else 0.0
}
