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
}
