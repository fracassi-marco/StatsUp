package com.statsup

import kotlin.math.roundToInt

object Variation {
    fun percentage(actualCounter: Double, allCounter: Double): Int {
        if(allCounter == 0.0)
            return 0
        return (actualCounter / allCounter.toFloat() * 100).roundToInt()
    }
}