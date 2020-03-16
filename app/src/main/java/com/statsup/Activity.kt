package com.statsup

import org.joda.time.DateTime

data class Activity(val id: Long, val sport: Sports, val distanceInMeters: Float, val durationInSeconds: Int, val dateInMillis: Long, val title: String) {

    constructor() : this(0, Sports.WORKOUT, 0.toFloat(), 0, DateTime.now().millis, "")

    fun date() : DateTime {
        return DateTime(dateInMillis)
    }

    fun durationInHours() : Double {
        return durationInSeconds / 3600.0
    }

    fun distanceInKilometers(): Double {
        return distanceInMeters / 1000.0
    }
}
