package com.statsup

import android.graphics.Color
import android.widget.TextView
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import org.joda.time.DateTime
import java.util.*
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import kotlin.math.max

class MonthOverMonthChart(private val chart: LineChartView, private val title: TextView, private val color: Int) {
    fun refresh(current: Activities) {
        val previous = current.addMonths(-1)
        title.text = "Rincorsa ${current.month().asString()} su ${previous.month().asString()}"
        val cumulativeOfCurrent = current.cumulativeByDay()
        val currentLine = line(cumulativeOfCurrent, color).apply {
            strokeWidth = 5
        }

        val cumulativeOfPrevious = previous.cumulativeByDay()
        val previousLine = line(cumulativeOfPrevious, Color.rgb(128, 128, 128))

        val maxValue = max(cumulativeOfPrevious.last(), cumulativeOfCurrent.last())
        chart.lineChartData = LineChartData(listOf(currentLine, previousLine)).apply {
            axisXBottom = Axis(labels(1..31)).apply {
                textColor = Color.BLACK

            }
            axisYLeft = Axis(labels(0..maxValue.toInt())).apply {
                textColor = Color.BLACK
            }
            isValueLabelBackgroundEnabled = false
            setValueLabelsTextColor(Color.BLACK)

        }
        chart.maximumViewport.apply {
            bottom = 0f
            left = 1f
            right = 31f
            top = maxValue.toFloat()

        }
        chart.currentViewport = chart.maximumViewport
        chart.isViewportCalculationEnabled = false
        chart.isInteractive = false
        chart.invalidate()
    }

    private fun line(values: List<Double>, aColor: Int): Line {
        val previous = values.mapIndexed { index, item ->
            PointValue(index + 1f, item.toFloat())
        }
        val previousLine = Line(previous).apply {
            color = aColor
            setHasLabels(false)
            setHasPoints(false)
        }
        return previousLine
    }

    private fun labels(range: IntRange): List<AxisValue> {
        return range.map { AxisValue(it.toFloat()).apply { setLabel(it.toString()) } }
    }
}

class YearOverYearChart(private val chart: LineChartView, private val title: TextView, private val color: Int) {

    fun refresh(current: Activities) {
        val previous = current.addYears(-1)
        title.text = "Rincorsa ${current.year().asString()} su ${previous.year().asString()}"
        val cumulativeOfCurrent = current.cumulativeByMonth()
        val currentLine = line(cumulativeOfCurrent, color).apply {
            strokeWidth = 5
        }

        val cumulativeOfPrevious = previous.cumulativeByMonth()
        val previousLine = line(cumulativeOfPrevious, Color.rgb(128, 128, 128))

        val maxValue = max(cumulativeOfPrevious.last(), cumulativeOfCurrent.last())
        chart.lineChartData = LineChartData(listOf(currentLine, previousLine)).apply {
            axisXBottom = Axis(labels(1..12)).apply {
                textColor = Color.BLACK

            }
            axisYLeft = Axis(labels(0..maxValue.toInt())).apply {
                textColor = Color.BLACK
            }
            isValueLabelBackgroundEnabled = false
            setValueLabelsTextColor(Color.BLACK)
        }
        chart.maximumViewport.apply {
            bottom = 0f
            left = 1f
            right = 12f
            top = maxValue.toFloat()

        }
        chart.currentViewport = chart.maximumViewport
        chart.isViewportCalculationEnabled = false
        chart.isInteractive = false
        chart.invalidate()
    }

    private fun line(values: List<Double>, aColor: Int): Line {
        val previous = values.mapIndexed { index, item ->
            PointValue(index + 1f, item.toFloat())
        }
        val previousLine = Line(previous).apply {
            color = aColor
            setHasLabels(false)
            setHasPoints(false)
        }
        return previousLine
    }

    private fun labels(range: IntRange): List<AxisValue> {
        return range.map { AxisValue(it.toFloat()).apply { setLabel(it.toString()) } }
    }
}
