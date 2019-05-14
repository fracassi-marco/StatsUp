package com.statsup

import org.joda.time.DateTime

data class Activity(val sport: Sports, val distanceInMeters: Float, val durationInSeconds: Int, val date: DateTime)
