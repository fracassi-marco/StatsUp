package com.statsup

import kotlin.math.roundToInt

object Variation {
    fun percentage(actualCounter: Double, allCounter: Double): Int {
        return (actualCounter / allCounter.toFloat() * 100).roundToInt()
    }
}