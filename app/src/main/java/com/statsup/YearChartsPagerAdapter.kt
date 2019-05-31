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

class YearChartsPagerAdapter(private val context: Context, private val activities: List<Activity>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.chart_pager_item, container, false)
        val label = view.findViewById<TextView>(R.id.year_label)
        val chart = view.findViewById<BarChart>(R.id.year_frequency_bar_chart)

        val maxMonthlyFrequency = Group(activities).maxMonthlyFrequency()
        val averageMonthlyFrequency = Group(activities).averageMonthlyFrequency()
        val years = Group(activities).years()
        val activitiesOfYear = Group(activities).ofYear(years[position])

        label.text = years[position].toString()

        configureChart(chart)
        refresh(chart, activitiesOfYear, maxMonthlyFrequency, averageMonthlyFrequency)

        container.addView(view)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    }

    override fun isViewFromObject(view: View, instance: Any): Boolean {
        return view == instance
    }

    override fun getCount(): Int {
        return Group(activities).years().size
    }

    private fun refresh(
        chart: BarChart,
        activities: List<Activity>,
        maxHeight: Float,
        averageMonthlyFrequency: Float
    ) {
        val subject = Group(activities).byMonths()

        if (subject.isEmpty()) {
            chart.data = BarData(emptyList())
            chart.invalidate()
            return
        }

        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = maxHeight
        chart.axisLeft.addLimitLine(LimitLine(averageMonthlyFrequency, "Media: ${String.format("%.2f", averageMonthlyFrequency)}").apply { textSize = 10f })

        var count = 0
        val data = subject.map {
            BarEntry(count++.toFloat(), it.value.size.toFloat())
        }

        val barDataSet = BarDataSet(data, "Numero di attività").apply {
            valueFormatter = BarChartValueFormatter()
        }
        chart.data = BarData(barDataSet)
        chart.setVisibleXRangeMaximum(12.toFloat())
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