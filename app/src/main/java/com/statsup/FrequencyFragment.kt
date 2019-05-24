package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class FrequencyFragment : Fragment() {

    private var year: Int = 0
    private lateinit var yearBarChart: BarChart
    private lateinit var yearLabel: TextView
    private lateinit var previousYearButton: ImageView
    private lateinit var nextYearButton: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frequency_fragment, container, false)
        yearBarChart = view.findViewById(R.id.year_frequency_bar_chart)
        yearLabel = view.findViewById(R.id.year_label)
        previousYearButton = view.findViewById(R.id.previous_year_button)
        nextYearButton = view.findViewById(R.id.next_year_button)
        configureChart()

        ActivityRepository.listen(object : Listener<List<Activity>> {
            override fun update(subject: List<Activity>) {
                refresh(subject)
            }
        })

        return view
    }

    fun refresh(subject: List<Activity>) {
        val groups = Group().byMonths(subject)
        if (groups.isEmpty()) {
            year = 0
            yearBarChart.data = BarData(emptyList())
            yearBarChart.invalidate()
            return
        }
        if (year == 0) {
            year = groups.keys.last().year
        }

        var count = 0
        val data = groups.map {
            BarEntry(count++.toFloat(), it.value.size.toFloat())
        }

        val barDataSet = BarDataSet(data, "number of activities").apply {
            valueFormatter = BarChartValueFormatter()
        }
        yearBarChart.data = BarData(barDataSet)
        yearBarChart.setVisibleXRangeMaximum(12.toFloat())

        previousYearButton.setOnClickListener {
            moveIndex(groups, -1)
        }
        nextYearButton.setOnClickListener {
            moveIndex(groups, 1)
        }

        moveIndex(groups, 0)
    }

    private fun moveIndex(
        groups: Map<Month, List<Activity>>,
        amount: Int
    ) {
        val newYear = year + amount
        if (newYear >= groups.keys.first().year && newYear <= groups.keys.last().year) {

            val offset = newYear - groups.keys.last().year

            yearBarChart.moveViewToX(yearBarChart.xChartMax + (offset * 12) - 12)
            yearLabel.text = newYear.toString()
            year = newYear
            yearBarChart.moveViewToX(yearBarChart.xChartMax + (offset * 12) - 12)
            yearBarChart.invalidate()
        }
    }

    private fun configureChart() {
        yearBarChart.axisLeft.isEnabled = false
        yearBarChart.axisLeft.setDrawGridLines(false)
        yearBarChart.axisRight.isEnabled = false
        yearBarChart.axisRight.setDrawGridLines(false)
        yearBarChart.xAxis.granularity = 1f
        yearBarChart.xAxis.setDrawGridLines(false)
        yearBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        yearBarChart.xAxis.valueFormatter = MonthLabels()
        yearBarChart.setTouchEnabled(false)
        yearBarChart.description = null
        yearBarChart.animateXY(1500, 1500)
    }
}