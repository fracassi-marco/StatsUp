package com.statsup

import android.graphics.Color
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.ColumnChartView

class YearlyChart(
    private val chart: ColumnChartView,
    private val barColor: Int,
    private val label: String
) {

    init {
        chart.isInteractive = false
    }

    fun refresh(value: Value, position: Int) {
        val byMonth = value.ofYear(position)
        if (byMonth.isEmpty()) {
            chart.columnChartData = ColumnChartData(emptyList())
        } else {
            chart.columnChartData = data(byMonth)

            setMinAndMax(value)
        }
        chart.invalidate()
    }

    private fun data(byMonth: List<Double>): ColumnChartData {
        val columns = byMonth.map {
            Column(listOf(SubcolumnValue(it.toFloat(), barColor))).apply {
                this.setHasLabels(true)
            }
        }

        return ColumnChartData(columns).apply {
            axisXBottom = Axis(labels()).apply {
                textColor = Color.BLACK
                name = label
            }
            isValueLabelBackgroundEnabled = false
        }.also {
            it.setValueLabelsTextColor(Color.BLACK)
        }
    }

    private fun labels(): List<AxisValue> {
        return (1..12).map { index ->
            AxisValue(index - 1f).also { it.setLabel(Months.labelOf(index)) }
        }
    }

    private fun setMinAndMax(value: Value) {
        chart.maximumViewport.apply {
            bottom = 0f
            top = value.max().toFloat()
        }
        chart.currentViewport = chart.maximumViewport
        chart.isViewportCalculationEnabled = false
    }
}