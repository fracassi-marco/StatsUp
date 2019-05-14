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
            5 -> "Mag"
            6 -> "Giu"
            7 -> "Lug"
            8 -> "Ago"
            9 -> "Set"
            10 -> "Ott"
            11 -> "Nov"
            else -> "Dic"
        }
    }
}
