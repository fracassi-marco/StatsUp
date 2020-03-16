package com.statsup

import org.joda.time.DateTime

data class Weight(val kilograms: Double, val dateInMillis: Long) {

    constructor() : this(0.0, DateTime.now().millis)

    fun date() : DateTime {
        return DateTime(dateInMillis)
    }
}
