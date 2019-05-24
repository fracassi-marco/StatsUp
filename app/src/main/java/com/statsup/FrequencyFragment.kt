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
        if (subject.isEmpty()) {
            year = 0
            yearLabel.text = ""
            yearBarChart.data = BarData(emptyList())
            yearBarChart.invalidate()
            return
        }
        val groups = Group().byMonths(subject)
        if (year == 0) {
            year = groups.keys.last().year
        }

        yearBarChart.axisLeft.axisMinimum = 0f
        yearBarChart.axisLeft.axisMaximum = groups.values.maxBy { it.size }!!.size.toFloat()

        var count = 0
        val data = groups.filter { it.key.year == year }.map {
            BarEntry(count++.toFloat(), it.value.size.toFloat())
        }

        val barDataSet = BarDataSet(data, "number of activities").apply {
            valueFormatter = BarChartValueFormatter()
        }
        yearBarChart.data = BarData(barDataSet)
        yearBarChart.setVisibleXRangeMaximum(12.toFloat())
        yearBarChart.invalidate()
        yearLabel.text = year.toString()

        previousYearButton.setOnClickListener {
            if (year > groups.keys.first().year) {
                year += -1
                refresh(subject)
            }
        }
        nextYearButton.setOnClickListener {
            if (year < groups.keys.last().year) {
                year += 1
                refresh(subject)
            }
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