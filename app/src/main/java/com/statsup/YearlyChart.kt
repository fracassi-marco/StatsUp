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
            label
        }.also {
            it.setValueLabelsTextColor(Color.BLACK)
        }
    }

    private fun labels(): List<AxisValue> {
        return listOf(
            AxisValue(0f).also { it.setLabel("Gen") },
            AxisValue(1f).also { it.setLabel("Feb") },
            AxisValue(2f).also { it.setLabel("Mar") },
            AxisValue(3f).also { it.setLabel("Apr") },
            AxisValue(4f).also { it.setLabel("Mag") },
            AxisValue(5f).also { it.setLabel("Giu") },
            AxisValue(6f).also { it.setLabel("Lug") },
            AxisValue(7f).also { it.setLabel("Ago") },
            AxisValue(8f).also { it.setLabel("Set") },
            AxisValue(9f).also { it.setLabel("Ott") },
            AxisValue(10f).also { it.setLabel("Nov") },
            AxisValue(11f).also { it.setLabel("Dic") })
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
