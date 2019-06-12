package com.statsup

import android.graphics.Color
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.ComboLineColumnChartView


class YearlyChart(
    private val chart: ComboLineColumnChartView,
    private val barColor: Int,
    private val label: String,
    private val maxValue: Float,
    private val valueProvider: (List<Activity>) -> Float
) {

    init {
        chart.isInteractive = false
    }

    fun refresh(activities: Activities, position: Int) {
        val byMonth = activities.ofYearInPosition(position)
        if (byMonth.isEmpty()) {
            chart.comboLineColumnChartData = ComboLineColumnChartData(ColumnChartData(emptyList()), LineChartData(emptyList())
            )
        } else {
            val line = Line(listOf(PointValue(0f, 5f), PointValue(11f, 5f)))
            line.setHasLabels(true)
            line.setHasLines(true)
            line.setHasPoints(false)

            val lineChartData = LineChartData(listOf(line))
            chart.comboLineColumnChartData = ComboLineColumnChartData(data(byMonth), lineChartData)

            setMinAndMax()
        }
        chart.invalidate()
    }

    private fun data(byMonth: Map<Month, List<Activity>>): ColumnChartData {
        val columns = byMonth.map {
            Column(listOf(SubcolumnValue(valueProvider.invoke(it.value), barColor))).apply {
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

    private fun setMinAndMax() {
        chart.maximumViewport.apply {
            bottom = 0f
            top = maxValue
        }
        chart.currentViewport = chart.maximumViewport
        chart.isViewportCalculationEnabled = false;
    }

}
