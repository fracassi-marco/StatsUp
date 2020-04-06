package com.statsup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.statsup.ActivityTabs.DISTANCE
import com.statsup.Variation.percentage
import com.statsup.barchart.Bar
import com.statsup.barchart.HorizontalBarChart
import kotlinx.android.synthetic.main.distance_fragment.view.*
import lecho.lib.hellocharts.view.LineChartView

class DistanceFragment : ActivityFragment() {

    private var dayOfWeekChart: HorizontalBarChart? = null
    private var viewpager: DynamicHeightViewPager? = null
    private var monthOverMonthChart: LineChartView? = null
    private var monthOverMonthChartTitle: TextView? = null

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.distance_fragment, container, false)
        monthOverMonthChart = view.month_over_month_chart
        monthOverMonthChartTitle = view.month_over_month_title
        viewpager = view.view_pager
        dayOfWeekChart = view.day_of_week_cart

        onActivityUpdate(ActivityRepository.all())

        return view
    }

    private fun refreshMonthOverMonthChart(values: Value) {
        MonthOverMonthChart(monthOverMonthChart!!, monthOverMonthChartTitle!!, DISTANCE.color).refresh(values)
    }

    private fun refreshDayOfWeekChart(values: Value) {
        val bars = values.groupByDay().map {
            Bar(percentage(it.value, values.total()), DISTANCE.color, it.key.label)
        }

        dayOfWeekChart!!.setData(100, bars)
    }

    private fun refreshBarCharts(values: Value) {
        val adapter = YearlyChartsPagerAdapter(context!!, DISTANCE.color, DISTANCE.unit, values)
        viewpager!!.adapter = adapter
        viewpager!!.currentItem = adapter.count - 1
    }

    override fun onActivityUpdate(activities: List<Activity>) {
        val values = DISTANCE.valueProvider(Activities(activities))
        refreshBarCharts(values)
        refreshDayOfWeekChart(values)
        refreshMonthOverMonthChart(values)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dayOfWeekChart = null
        viewpager = null
        monthOverMonthChart = null
        monthOverMonthChartTitle = null
    }
}

