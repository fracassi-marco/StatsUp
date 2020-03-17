package com.statsup

import android.graphics.Color
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import kotlin.math.max

class MonthOverMonthChart(private val monthOverMonthChart: LineChartView, private val color: Int) {
    fun refresh(frequencies: Value) {
        val cumulativeOfCurrentMont = frequencies.cumulativeOfCurrentMont()
        val currentLine = line(cumulativeOfCurrentMont, color).apply {
            strokeWidth = 5
        }

        val cumulativeOfPreviousMont = frequencies.cumulativeOfPreviousMont()
        val previousLine = line(cumulativeOfPreviousMont, Color.rgb(128, 128, 128))

        val maxValue = max(cumulativeOfPreviousMont.last(), cumulativeOfCurrentMont.last())
        monthOverMonthChart.lineChartData = LineChartData(listOf(currentLine, previousLine)).apply {
            axisXBottom = Axis(labels(1..31)).apply {
                textColor = Color.BLACK

            }
            axisYLeft = Axis(labels(0..maxValue.toInt())).apply {
                textColor = Color.BLACK
            }
            isValueLabelBackgroundEnabled = false
            setValueLabelsTextColor(Color.BLACK)

        }
        monthOverMonthChart.maximumViewport.apply {
            bottom = 0f
            left = 1f
            right = 31f
            top = maxValue.toFloat()

        }
        monthOverMonthChart.currentViewport = monthOverMonthChart.maximumViewport
        monthOverMonthChart.isViewportCalculationEnabled = false
        monthOverMonthChart.isInteractive = false
        monthOverMonthChart.invalidate()
    }

    private fun line(cumulativeOfPreviousMont: List<Double>, aColor: Int): Line {
        val previous = cumulativeOfPreviousMont.mapIndexed { index, item ->
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
