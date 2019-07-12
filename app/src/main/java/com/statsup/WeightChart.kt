package com.statsup

import android.graphics.Color
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView

class WeightChart(private val lineChart: LineChartView) {
    fun refresh(weights: List<Weight>) {
        lineChart.lineChartData = generateData(weights)
        lineChart.currentViewport = Viewport(
            lineChart.maximumViewport.right - 90,
            lineChart.maximumViewport.top,
            lineChart.maximumViewport.right,
            lineChart.maximumViewport.bottom
        );
        lineChart.isViewportCalculationEnabled = false;
    }

    private fun generateData(weights: List<Weight>): LineChartData {
        val line = Line(values(weights)).also {
            it.shape = ValueShape.CIRCLE
            it.pointRadius = 4
            it.setHasLabels(false)
            it.setHasLabelsOnlyForSelected(true)
            it.setHasLines(true)
            it.setHasPoints(true)
            it.color = Color.rgb(255, 185, 97)
        }

        return LineChartData(listOf(line)).also {
            it.axisXBottom = Axis(axisXLabels(weights)).also {
                it.setHasTiltedLabels(true)
                it.name = "Peso [Kg]"
                it.textColor = Color.BLACK
            }
            it.axisYLeft = Axis().also {
                it.setHasLines(true)
                it.textColor = Color.BLACK
            }
            it.setValueLabelsTextColor(Color.BLACK)
        }
    }

    private fun values(orderedWeights: List<Weight>): List<PointValue> {
        val zero = orderedWeights.first().dateInMillis
        return orderedWeights.map { weight ->
            val point = daysBetween(zero, weight.dateInMillis)
            PointValue(point, weight.kilograms.toFloat()).also {
                it.setLabel("${weight.kilograms}Kg - ${weight.date().toString("dd/MM/yyyy")}")
            }
        }
    }

    private fun axisXLabels(orderedWeights: List<Weight>): List<AxisValue> {
        val zero = orderedWeights.first().dateInMillis
        return orderedWeights.map { weight ->
            val point = daysBetween(zero, weight.dateInMillis)
            AxisValue(point).also {
                val date =
                    "${weight.date().dayOfMonth}/${weight.date().monthOfYear}/${weight.date().year.toString().substring(
                        2
                    )}"
                it.setLabel(date)
            }
        }
    }

    private fun daysBetween(from: Long, to: Long): Float {
        val deltaMillis = to - from
        return (deltaMillis / 86400000).toFloat()
    }
}