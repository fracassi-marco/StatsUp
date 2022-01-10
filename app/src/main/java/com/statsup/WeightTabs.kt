package com.statsup

import android.graphics.Color
import androidx.fragment.app.Fragment

enum class WeightTabs(val position: Int, val label: String, val color: Int) {
    BALANCE(0, "Peso", Color.rgb(76, 175, 80)),
    BMI(1, "BMI", Color.rgb(33, 150, 243)),
    INFO(2, "Info", Color.rgb(244, 67, 54));

    companion object {
        fun at(position: Int): WeightTabs {
            return values().single { it.position == position }
        }

        fun fragment(position: Int): Fragment {
            return when(position) {
                0 -> BalanceFragment()
                1 -> BmiFragment()
                else -> WeightInfoFragment()
            }
        }
    }
}