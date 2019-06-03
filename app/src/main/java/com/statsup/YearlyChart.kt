package com.statsup

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class YearlyChart(
    private val chart: BarChart,
    private val label: String,
    private val maxValue: Float,
    private val averageValue: Float,
    private val valueProvider: (List<Activity>) -> Float
) {

    init {
        chart.axisLeft.isEnabled = false
        chart.axisLeft.axisMinimum = 0f
        chart.axisRight.isEnabled = false
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.valueFormatter = MonthLabels()
        chart.setTouchEnabled(false)
        chart.description = null
    }

    fun refresh(activities: Activities, position: Int) {
        val byMonth = activities.ofYearInPosition(position)
        if (byMonth.isEmpty()) {
            chart.data = BarData(emptyList())
        } else {
            chart.axisLeft.axisMaximum = maxValue
            val average = averageValue
            chart.axisLeft.addLimitLine(LimitLine(average, "Media: ${String.format("%.2f", average)}").apply {
                textSize = 10f
            })

            var count = 0
            val data = byMonth.map {
                BarEntry(count++.toFloat(), valueProvider.invoke(it.value))
            }

            val barDataSet = BarDataSet(data, label).apply {
                valueFormatter = BarChartValueFormatter()
            }
            chart.data = BarData(barDataSet)
        }
        chart.invalidate()
    }

}
