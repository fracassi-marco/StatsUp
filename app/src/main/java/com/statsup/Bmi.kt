package com.statsup

import kotlin.math.pow

object Bmi {
    fun labelForOxford(weight: Weight, height: Int) : String {
        return Measure.of(oxford(weight, height), "", "")
    }

    fun oxford(weight: Weight, height: Int) : Double {
        return (1.3 * weight.kilograms) / (height / 100.0).pow(2.5)
    }

    fun labelForClassic(weight: Weight, height: Int) : String {
        return Measure.of(classic(weight, height), "", "")
    }

    fun classic(weight: Weight, height: Int) : Double {
        return (weight.kilograms) / (height / 100.0).pow(2)
    }
}
