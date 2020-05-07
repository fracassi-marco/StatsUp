package com.statsup

import android.graphics.Color
import android.graphics.Color.BLACK
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.model.ValueShape.CIRCLE
import lecho.lib.hellocharts.view.LineChartView
import org.joda.time.Days

class WeightChart(private val lineChart: LineChartView) {
    fun refresh(weights: List<Weight>) {
        lineChart.lineChartData = generateData(weights)
        lineChart.currentViewport = Viewport(
            lineChart.maximumViewport.right - 365,
            lineChart.maximumViewport.top,
            lineChart.maximumViewport.right,
            lineChart.maximumViewport.bottom
        )
        lineChart.isViewportCalculationEnabled = false
    }

    private fun generateData(weights: List<Weight>): LineChartData {
        val values = mutableListOf<PointValue>()
        val labels = mutableListOf<AxisValue>()

        val zero = weights.first().date()
        weights.forEach { weight ->
            val point = Days.daysBetween(zero, weight.date()).days.toFloat()
            values.add(PointValue(point, weight.kilograms.toFloat()).apply {
                setLabel("${weight.kilograms}Kg - ${weight.date().toString("dd/MM/yyyy")}")
            })
            labels.add(AxisValue(point).apply {
                setLabel(weight.date().toString("    MM/yy"))
            })
        }

        val line = Line(values).apply {
            shape = CIRCLE
            pointRadius = 4
            setHasLabelsOnlyForSelected(true)
            setHasLines(true)
            setHasPoints(true)
            color = Color.rgb(255, 185, 97)
        }

        return LineChartData(listOf(line)).apply {
            axisXBottom = Axis(labels).apply {
                setHasTiltedLabels(true)
                setHasLines(true)
                textColor = BLACK
            }
            axisYLeft = Axis().apply {
                setHasLines(true)
                textColor = BLACK
            }
            setValueLabelsTextColor(BLACK)
        }
    }
}