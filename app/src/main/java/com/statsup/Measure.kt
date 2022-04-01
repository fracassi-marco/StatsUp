package com.statsup

import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.Int.Companion.MAX_VALUE

object Measure {
    fun of(value: Double, unit: String, positiveSign: String = "+", zeroSign: String = "0.00"): String {
        if(value == 0.0 || value == POSITIVE_INFINITY) {
            return "$zeroSign$unit"
        }
        var result = ""
        if(value > 0) {
            result = positiveSign
        }
        return result + String.format("%.2f", value) + unit
    }

    fun hms(seconds: Int): String {
        val hoursFragment = seconds / 3600
        val minutesFragment = (seconds % 3600) / 60
        val secondsFragment = seconds % 60
        if(hoursFragment > 0)
            return "${hoursFragment}h ${minutesFragment}m ${secondsFragment}s"

        return "${minutesFragment}m ${secondsFragment}s"
    }

    fun hm(seconds: Int): String {
        val hoursFragment = seconds / 3600
        val minutesFragment = (seconds % 3600) / 60
        if(hoursFragment > 0)
            return "${hoursFragment}h ${minutesFragment}m"

        return "${minutesFragment}m"
    }

    fun minutesAndSeconds(seconds: Int, label: String): String {
        if(seconds == 0 || seconds == MAX_VALUE)
            return "- $label"

        val minutesFragment = pad(seconds / 60)
        val secondsFragment = pad(seconds % 60)
        return "${minutesFragment}:${secondsFragment}$label"
    }

    fun frequency(value: Double) = "#${value.toInt()}"

    private fun pad(value: Int) = value.toString().padStart(2, '0')
}
