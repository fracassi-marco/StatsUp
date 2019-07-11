package com.statsup

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.no_items_layout.view.*
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
        lineChart = view.findViewById(R.id.line_chart)

        val noItemLayout = view.findViewById<View>(R.id.no_item_layout)
        noItemLayout.no_activities_text_view.text = resources.getString(R.string.empty_weight)

        noItemListener = NoActivitiesListener(lineChart, noItemLayout)
        WeightRepository.listen(listener, noItemListener)

        return view
    }

    private fun updateChart(subject: List<Weight>) {
        lineChart.lineChartData = generateData(subject)

        val max = subject.maxBy { it.kilograms }!!.kilograms.toFloat()
        val min = subject.minBy { it.kilograms }!!.kilograms.toFloat()

        lineChart.currentViewport =
            Viewport(lineChart.maximumViewport.right - 31, max, lineChart.maximumViewport.right, min);
    }

    private fun daysBetween(from: Long, to: Long): Float {
        val deltaMillis = to - from
        return (deltaMillis / 86400000).toFloat()
    }

    private fun generateData(weights: List<Weight>): LineChartData {
        val orderedWeights = weights.sortedBy { it.dateInMillis }

        val values = ArrayList<PointValue>()
        val labels = mutableListOf<AxisValue>()
        val zero = orderedWeights.first().dateInMillis
        orderedWeights.forEach { weight ->
            val point = daysBetween(zero, weight.dateInMillis)
            val pointValue = PointValue(point, weight.kilograms.toFloat()).also {
                it.setLabel("${weight.kilograms}Kg - ${weight.date().toString("dd/MM/yyyy")}")
            }
            values.add(pointValue)
            labels.add(AxisValue(point).also {
                val date = "${weight.date().dayOfMonth}/${weight.date().monthOfYear}/${weight.date().year.toString().substring(2)}"
                it.setLabel(date)
            })
        }

        val line = Line(values).also {
            it.shape = ValueShape.CIRCLE
            it.setHasLabels(false)
            it.setHasLabelsOnlyForSelected(true)
            it.setHasLines(true)
            it.setHasPoints(true)
            it.color = Color.rgb(255, 185, 97)
        }

        return LineChartData(listOf(line)).apply {
            axisXBottom = Axis(labels).also {
                it.setHasTiltedLabels(true)
                it.name = "Peso [Kg]"
                it.textColor = Color.BLACK
            }
            axisYLeft = Axis().also {
                it.setHasLines(true)
                it.textColor = Color.BLACK
            }
        }.also {
            it.setValueLabelsTextColor(Color.BLACK)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        WeightRepository.removeListener(listener, noItemListener)
    }
}
