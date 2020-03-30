package com.statsup

object Measure {
    fun of(value: Double, label: String, positiveSign: String = "+"): String {
        var result = ""
        if(value == Double.POSITIVE_INFINITY) {
            return " - "
        }
        if(value > 0) {
            result = positiveSign
        }
        return result + String.format("%.2f", value) + label
    }

    fun timeFragments(seconds: Int): String {
        val hoursFragment = seconds / 3600;
        val minutesFragment = (seconds % 3600) / 60;
        val secondsFragment = seconds % 60;
        return "${hoursFragment}h ${minutesFragment}m ${secondsFragment}s"
    }
}
