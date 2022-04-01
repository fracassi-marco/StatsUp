package com.statsup

import kotlin.math.roundToInt

object Variation {
    fun percentage(actualCounter: Double, allCounter: Double): Int {
        if(allCounter == 0.0)
            return 0
        if(actualCounter > allCounter)
            return 100
        return (actualCounter / allCounter.toFloat() * 100).roundToInt()
    }

    fun percentage(actualCounter: Int, allCounter: Int): Int {
        if(allCounter == 0)
            return 0
        if(actualCounter > allCounter)
            return 100
        return (actualCounter / allCounter.toFloat() * 100).roundToInt()
    }
}