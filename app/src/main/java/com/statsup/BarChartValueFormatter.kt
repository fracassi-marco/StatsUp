package com.statsup

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler

class BarChartValueFormatter(private val digits: Int = 0) : IValueFormatter {

    override fun getFormattedValue(value: Float, entry: Entry, dataSetIndex: Int, viewPortHandler: ViewPortHandler): String {
        return if (value > 0) {
            DefaultValueFormatter(digits).getFormattedValue(value, entry, dataSetIndex, viewPortHandler)
        } else {
            "0"
        }
    }
}