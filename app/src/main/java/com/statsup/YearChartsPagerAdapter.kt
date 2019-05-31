package com.statsup

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry

class YearChartsPagerAdapter(private val context: Context, private val allActivities: List<Activity>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.chart_pager_item, container, false)
        val label = view.findViewById<TextView>(R.id.year_label)
        val chart = view.findViewById<BarChart>(R.id.year_frequency_bar_chart)

        val activities = Activities(allActivities)
        label.text = activities.yearInPosition(position).toString()

        configureChart(chart)
        refresh(position, chart, activities)

        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    }

    override fun isViewFromObject(view: View, instance: Any): Boolean {
        return view == instance
    }

    override fun getCount(): Int {
        return Activities(allActivities).years().size
    }

    private fun refresh(position: Int,
                        chart: BarChart,
                        activities: Activities
    ) {
        val byMonth = activities.ofYearInPosition(position)

        if (byMonth.isEmpty()) {
            chart.data = BarData(emptyList())
            chart.invalidate()
            return
        }

        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = activities.maxMonthlyFrequency()
        val averageMonthlyFrequency = activities.averageMonthlyFrequency()
        chart.axisLeft.addLimitLine(LimitLine(averageMonthlyFrequency, "Media: ${String.format("%.2f", averageMonthlyFrequency)}").apply {
            textSize = 10f
        })

        var count = 0
        val data = byMonth.map {
            BarEntry(count++.toFloat(), it.value.size.toFloat())
        }

        val barDataSet = BarDataSet(data, "Numero di attività").apply {
            valueFormatter = BarChartValueFormatter()
        }
        chart.data = BarData(barDataSet)

        chart.invalidate()
    }

    private fun configureChart(barChart: BarChart) {
        barChart.axisLeft.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.valueFormatter = MonthLabels()
        barChart.setTouchEnabled(false)
        barChart.description = null
    }
}