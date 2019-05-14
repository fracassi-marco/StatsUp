package com.statsup

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter

class MonthLabels : IAxisValueFormatter {

    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        return when(value.toInt()%12) {
            0 -> "Gen"
            1 -> "Feb"
            2 -> "Mar"
            3 -> "Apr"
            4 -> "Mag"
            5 -> "Giu"
            6 -> "Lug"
            7 -> "Ago"
            8 -> "Set"
            9 -> "Ott"
            10 -> "Nov"
            else -> "Dic"
        }
    }
}
