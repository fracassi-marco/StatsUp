package com.statsup

import kotlin.math.roundToInt

fun Float.round(decimals: Int): Float {
    var multiplier = 1f
    repeat(decimals) { multiplier *= 10 }
    return  (this * multiplier).roundToInt() / multiplier
}