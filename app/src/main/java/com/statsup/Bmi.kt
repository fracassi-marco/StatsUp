package com.statsup

import kotlin.math.pow

object Bmi {
    fun labelFor(weight: Weight, height: Int) : String {
        return Measure.of(valueFor(weight, height), "", "")
    }

    fun valueFor(weight: Weight, height: Int) : Double {
        return (1.3 * weight.kilograms) / (height / 100.0).pow(2.5)
    }
}
