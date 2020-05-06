package com.statsup

import android.graphics.Color.BLACK
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.ColumnChartView


class MonthlyChart(
    private val chart: ColumnChartView,
    private val barColor: Int,
    private val label: String
) {

    init {
        chart.isInteractive = false
    }

    fun refresh(value: Activities) {
        val byDay = value.byDay()
        if (byDay.isEmpty()) {
            chart.columnChartData = ColumnChartData(emptyList())
        } else {
            chart.columnChartData = data(byDay)

            setMinAndMax(value)
        }
        chart.invalidate()
    }

    private fun data(byBay: List<Double>): ColumnChartData {
        val columns = byBay.map {
            Column(listOf(SubcolumnValue(it.toFloat(), barColor))).apply {
                this.setHasLabels(true)
            }
        }

        return ColumnChartData(columns).apply {
            axisXBottom = Axis(labels(byBay)).apply {
                textColor = BLACK
                name = label
            }
            isValueLabelBackgroundEnabled = false
            setValueLabelsTextColor(BLACK)
        }
    }

    private fun labels(byBay: List<Double>): List<AxisValue> {
        return (1..byBay.size).map { index ->
            AxisValue(index - 1f).also { it.setLabel(index.toString()) }
        }
    }

    private fun setMinAndMax(value: Activities) {
        chart.maximumViewport.apply {
            bottom = 0f
            top = value.maxByDay().toFloat()
        }
        chart.currentViewport = chart.maximumViewport
        chart.isViewportCalculationEnabled = false
    }
}

class AnnualChart(
    private val chart: ColumnChartView,
    private val barColor: Int,
    private val label: String
) {

    init {
        chart.isInteractive = false
    }

    fun refresh(value: Activities) {
        val byMonth = value.byMonth()
        if (byMonth.isEmpty()) {
            chart.columnChartData = ColumnChartData(emptyList())
        } else {
            chart.columnChartData = data(byMonth)

            setMinAndMax(value)
        }
        chart.invalidate()
    }

    private fun data(values: List<Double>): ColumnChartData {
        val columns = values.map {
            Column(listOf(SubcolumnValue(it.toFloat(), barColor))).apply {
                this.setHasLabels(true)
            }
        }

        return ColumnChartData(columns).apply {
            axisXBottom = Axis(labels(values)).apply {
                textColor = BLACK
                name = label
            }
            isValueLabelBackgroundEnabled = false
            setValueLabelsTextColor(BLACK)
        }
    }

    private fun labels(values: List<Double>): List<AxisValue> {
        return (1..values.size).map { index ->
            AxisValue(index - 1f).also { it.setLabel(index.toString()) }
        }
    }

    private fun setMinAndMax(value: Activities) {
        chart.maximumViewport.apply {
            bottom = 0f
            top = value.maxByMonth().toFloat()
        }
        chart.currentViewport = chart.maximumViewport
        chart.isViewportCalculationEnabled = false
    }
}

class EverChart(
    private val chart: ColumnChartView,
    private val barColor: Int,
    private val label: String
) {

    init {
        chart.isInteractive = false
    }

    fun refresh(value: Activities) {
        val byYear = value.byYear()
        if (byYear.isEmpty()) {
            chart.columnChartData = ColumnChartData(emptyList())
        } else {
            chart.columnChartData = data(byYear)

            setMinAndMax(value)
        }
        chart.invalidate()
    }

    private fun data(values: Map<Int, Double>): ColumnChartData {
        val columns = values.map {
            Column(listOf(SubcolumnValue(it.value.toFloat(), barColor))).apply {
                this.setHasLabels(true)
            }
        }

        return ColumnChartData(columns).apply {
            axisXBottom = Axis(labels(values)).apply {
                textColor = BLACK
                name = label
            }
            isValueLabelBackgroundEnabled = false
            setValueLabelsTextColor(BLACK)
        }
    }

    private fun labels(values: Map<Int, Double>): List<AxisValue> {
        var i = 0f
        return values.map { index ->
            AxisValue(i++).also { it.setLabel(index.key.toString()) }
        }
    }

    private fun setMinAndMax(value: Activities) {
        chart.maximumViewport.apply {
            bottom = 0f
            top = value.maxByYear().toFloat()
        }
        chart.currentViewport = chart.maximumViewport
        chart.isViewportCalculationEnabled = false
    }
}