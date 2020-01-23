package com.statsup

import android.graphics.Color
import android.support.v4.app.Fragment

enum class WeightTabs(val position: Int, val label: String, val color: Int, val fragment: Fragment) {
    BALANCE(0, "Peso", Color.rgb(76, 175, 80), BalanceFragment()),
    BMI(1, "BMI", Color.rgb(33, 150, 243), BmiFragment()),
    INFO(2, "Info", Color.rgb(244, 67, 54), WeightInfoFragment());

    companion object {
        fun at(position: Int): WeightTabs {
            return values().single { it.position == position }
        }
    }
}