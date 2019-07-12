package com.statsup

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.no_items_layout.view.*
import kotlinx.android.synthetic.main.weight_stats_fragment.view.*
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView


class WeightStatsFragment : Fragment() {
    private lateinit var lineChart: LineChartView
    private lateinit var noItemListener: Listener<List<Weight>>
    private val listener = object : Listener<List<Weight>> {
        override fun update(subject: List<Weight>) {
            if (subject.isEmpty()) {
                return
            }

            updateChart(subject)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.weight_stats_fragment, container, false)
        lineChart = view.line_chart

        val noItemLayout = view.no_item_layout.apply {
            label_text.text = resources.getString(R.string.empty_weight)
        }

        noItemListener = NoActivitiesListener(lineChart, noItemLayout)
        WeightRepository.listen(listener, noItemListener)

        return view
    }

    private fun updateChart(subject: List<Weight>) {
        lineChart.lineChartData = generateData(subject)
        lineChart.currentViewport = Viewport(lineChart.maximumViewport.right - 90, lineChart.maximumViewport.top, lineChart.maximumViewport.right, lineChart.maximumViewport.bottom);
        lineChart.isViewportCalculationEnabled = false;
    }

    private fun daysBetween(from: Long, to: Long): Float {
        val deltaMillis = to - from
        return (deltaMillis / 86400000).toFloat()
    }

    private fun generateData(weights: List<Weight>): LineChartData {
        val orderedWeights = weights.sortedBy { it.dateInMillis }

        val line = Line(values(orderedWeights)).also {
            it.shape = ValueShape.CIRCLE
            it.pointRadius = 4
            it.setHasLabels(false)
            it.setHasLabelsOnlyForSelected(true)
            it.setHasLines(true)
            it.setHasPoints(true)
            it.color = Color.rgb(255, 185, 97)
        }

        return LineChartData(listOf(line)).also {
            it.axisXBottom = Axis(axisXLabels(orderedWeights)).also {
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
                    "${weight.date().dayOfMonth}/${weight.date().monthOfYear}/${weight.date().year.toString().substring(2)}"
                it.setLabel(date)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        WeightRepository.removeListener(listener, noItemListener)
    }
}
