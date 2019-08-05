package com.statsup

import kotlin.math.pow

object Bmi {
    fun of(weight: Weight, height: Int) : String {
        val bmi = (1.3 * weight.kilograms) / (height / 100.0).pow(2.5)

        return Measure.of(bmi, "", "")
    }
}
