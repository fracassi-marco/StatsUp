package com.statsup

import android.graphics.Color
import android.widget.TextView
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import java.util.*
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR
import kotlin.math.max

class MonthOverMonthChart(private val chart: LineChartView, private val title: TextView, private val color: Int) {
    fun refresh(frequencies: Value) {
        val currentMonth = GregorianCalendar()
        val previousMonth = GregorianCalendar()
        previousMonth.add(MONTH, -1)
        title.text = "Rincorsa ${Months.labelOf(currentMonth.get(MONTH) + 1)} ${currentMonth.get(YEAR)} su ${Months.labelOf(previousMonth.get(MONTH) + 1)} ${previousMonth.get(YEAR)}"
        val cumulativeOfCurrentMont = frequencies.cumulativeOfCurrentMont()
        val currentLine = line(cumulativeOfCurrentMont, color).apply {
            strokeWidth = 5
        }

        val cumulativeOfPreviousMont = frequencies.cumulativeOfPreviousMont()
        val previousLine = line(cumulativeOfPreviousMont, Color.rgb(128, 128, 128))

        val maxValue = max(cumulativeOfPreviousMont.last(), cumulativeOfCurrentMont.last())
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
