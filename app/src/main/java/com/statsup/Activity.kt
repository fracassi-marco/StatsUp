package com.statsup

import org.joda.time.DateTime

data class Activity(val sport: Sports, val distanceInMeters: Float, val durationInSeconds: Int, val dateInMillis: Long, val title: String) {

    constructor() : this(Sports.WORKOUT, 0.toFloat(), 0, DateTime.now().millis, "")

    lateinit var id: String

    fun date() : DateTime {
        return DateTime(dateInMillis)
    }
}
