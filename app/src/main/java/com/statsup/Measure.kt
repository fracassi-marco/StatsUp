package com.statsup

import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.Int.Companion.MAX_VALUE

object Measure {
    fun of(value: Double, label: String, positiveSign: String = "+", zeroSign: String = "0.00"): String {
        if(value == 0.0 || value == POSITIVE_INFINITY) {
            return "$zeroSign$label"
        }
        var result = ""
        if(value > 0) {
            result = positiveSign
        }
        return result + String.format("%.2f", value) + label
    }

    fun timeFragments(seconds: Int): String {
        val hoursFragment = seconds / 3600
        val minutesFragment = (seconds % 3600) / 60
        val secondsFragment = seconds % 60
        if(hoursFragment > 0)
            return "${hoursFragment}h ${minutesFragment}m ${secondsFragment}s"

        return "${minutesFragment}m ${secondsFragment}s"
    }

    fun minutesAndSeconds(seconds: Int, label: String): String {
        if(seconds == 0 || seconds == MAX_VALUE)
            return "- $label"

        val minutesFragment = pad(seconds / 60)
        val secondsFragment = pad(seconds % 60)
        return "${minutesFragment}:${secondsFragment}$label"
    }

    private fun pad(value: Int) = value.toString().padStart(2, '0')
}
