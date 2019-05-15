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
import org.joda.time.DateTime

class FrequencyFragment : Fragment() {

    private var year: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frequency_fragment, container, false)
        val yearBarChart = view.findViewById<BarChart>(R.id.year_frequency_bar_chart)
        val yearLabel = view.findViewById<TextView>(R.id.year_label)

        val activities = ActivityRepository().all()
        /*val activities = listOf(
            Activity(Sports.ALPINE_SKI, 1f, 1, DateTime.parse("2019-01-01T01:01:01Z")),
            Activity(Sports.ALPINE_SKI, 1f, 1, DateTime.parse("2019-12-31T01:01:01Z")),
            Activity(Sports.ALPINE_SKI, 1f, 1, DateTime.parse("2018-01-01T01:01:01Z")),
            Activity(Sports.ALPINE_SKI, 1f, 1, DateTime.parse("2017-01-01T01:01:01Z"))
        )*/
        val groups = Group().byMonths(activities)

        var count = 0
        val data = groups.map {
            BarEntry(count++.toFloat(), it.value.size.toFloat())
        }

        val barDataSet = BarDataSet(data, "number of activities").apply {
            valueFormatter = BarChartValueFormatter()
        }
        yearBarChart.data = BarData(barDataSet)

        yearBarChart.setVisibleXRangeMaximum(12.toFloat())
        year = groups.keys.last().year
        moveIndex(groups, 0, yearBarChart, yearLabel)

        yearBarChart.axisLeft.isEnabled = false
        yearBarChart.axisLeft.setDrawGridLines(false)
        yearBarChart.axisRight.isEnabled = false
        yearBarChart.axisRight.setDrawGridLines(false)
        yearBarChart.xAxis.granularity = 1f
        yearBarChart.xAxis.setDrawGridLines(false)
        yearBarChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        yearBarChart.xAxis.valueFormatter = MonthLabels()
        yearBarChart.setTouchEnabled(false)

        yearBarChart.invalidate()

        val previousYearButton = view.findViewById<ImageView>(R.id.previous_year_button)
        previousYearButton.setOnClickListener {
            moveIndex(groups, -1, yearBarChart, yearLabel)
        }
        val nextYearButton = view.findViewById<ImageView>(R.id.next_year_button)
        nextYearButton.setOnClickListener {
            moveIndex(groups, 1, yearBarChart, yearLabel)
        }

        return view
    }

    private fun moveIndex(
        groups: Map<Month, List<Activity>>,
        amount: Int,
        yearBarChart: BarChart,
        yearLabel: TextView
    ) {
        println("year = ${year} amount = $amount")
        val newYear = year + amount
        if(newYear >= groups.keys.first().year && newYear <= groups.keys.last().year) {

            val offset = newYear - groups.keys.last().year

            yearBarChart.moveViewToX(yearBarChart.xChartMax + (offset * 12) - 12)
            yearLabel.text = newYear.toString()
            year = newYear
        }
    }
}

