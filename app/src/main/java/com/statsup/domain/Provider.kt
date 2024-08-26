package com.statsup.domain

enum class Provider(
    val cumulative: (List<Training>) -> Double,
    val max: (List<Training>) -> Double) {
    Distance(
        cumulative = { it.sumOf { training -> training.distanceInKilometers() } },
        max = { if (it.isEmpty()) 0.0 else it.maxOf { training -> training.distanceInKilometers() } }
    ),
    Duration(
        cumulative = { it.sumOf { training -> training.durationInHours() } },
        max = { if (it.isEmpty()) 0.0 else it.maxOf { training -> training.durationInHours() } }
    ),
    Frequency(
        cumulative = { it.count().toDouble() },
        max = { 0.0 }
    ),
    Elevation(
        cumulative = { it.sumOf { training -> training.totalElevationGain } },
        max = { if (it.isEmpty()) 0.0 else it.maxOf { training -> training.totalElevationGain } }
    ),
    Altitude(
        cumulative = { 0.0 },
        max = { if (it.isEmpty()) 0.0 else it.maxOf { training -> training.elevHigh } }
    ),
    HeartRate(
        cumulative = { 0.0 },
        max = { if (it.isEmpty()) 0.0 else it.maxOf { training -> training.maxHeartrate } }
    ),
    None(
        cumulative = { 0.0 },
        max = { 0.0 }
    );

    companion object {
        fun byIndex(index: Int): Provider {
            return when (index) {
                0 -> Distance
                1 -> Frequency
                2 -> Duration
                else -> Elevation
            }
        }
    }
}