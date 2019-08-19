package com.statsup

import android.graphics.Color
import android.support.v4.app.Fragment

enum class WeightTabs(val position: Int, val label: String, val color: Int, val fragment: Fragment) {
    BALANCE(0, "Peso", Color.rgb(76, 175, 80), BalanceFragment()),
    BMI(1, "BMI", Color.rgb(33, 150, 243), BmiFragment());

    companion object {
        fun at(position: Int): WeightTabs {
            return values().single { it.position == position }
        }
    }
}