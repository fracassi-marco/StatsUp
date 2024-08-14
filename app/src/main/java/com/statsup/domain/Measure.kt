package com.statsup.domain

object Measure {

    fun hm(seconds: Int): String {
        val hoursFragment = seconds / 3600
        val minutesFragment = (seconds % 3600) / 60
        if (hoursFragment > 0)
            return "${hoursFragment}h ${minutesFragment}m"

        return "${minutesFragment}m"
    }
}