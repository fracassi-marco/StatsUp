package com.statsup

import org.joda.time.DateTime
import kotlin.Int.Companion.MAX_VALUE

data class Activity(val id: Long, val sport: Sports, val distanceInMeters: Float, val durationInSeconds: Int, val dateInMillis: Long, val title: String, val maxSpeedInMetersPerSecond: Double, val elevationInMeters: Double) {

    fun date() : DateTime {
        return DateTime(dateInMillis)
    }

    fun durationInHours() : Double {
        return durationInSeconds / 3600.0
    }

    fun distanceInKilometers(): Double {
        return distanceInMeters / 1000.0
    }

    fun averageSpeedInKilometersPerHours() = distanceInKilometers() / durationInHours()

    fun maxSpeedInKilometersPerHours() = maxSpeedInMetersPerSecond * 3.6

    fun paceInSecondsPerKilometer(): Int = if(distanceInMeters == 0f) MAX_VALUE else  (durationInSeconds / distanceInKilometers()).toInt()
}
