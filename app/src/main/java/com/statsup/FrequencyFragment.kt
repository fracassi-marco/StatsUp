package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.statsup.ActivityTabs.FREQUENCY
import com.statsup.Variation.percentage
import com.statsup.barchart.Bar
import com.statsup.barchart.HorizontalBarChart
import kotlinx.android.synthetic.main.frequency_fragment.view.*
import lecho.lib.hellocharts.view.LineChartView

class FrequencyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.frequency_fragment, container, false)
        val monthOverMonthChart = view.month_over_month_chart
        val monthOverMonthChartTitle = view.month_over_month_title

        val subject = ActivityRepository.all()
        val values = Distances(Activities(subject))
        refreshBarCharts(values, view.view_pager)
        refreshDayOfWeekChart(values, view.day_of_week_cart)
        refreshMonthOverMonthChart(values, monthOverMonthChart, monthOverMonthChartTitle)

        return view
    }

    private fun refreshMonthOverMonthChart(
        values: Value,
        monthOverMonthChart: LineChartView,
        monthOverMonthTitle: TextView
    ) {
        MonthOverMonthChart(monthOverMonthChart, monthOverMonthTitle, FREQUENCY.color).refresh(values)
    }

    private fun refreshDayOfWeekChart(values: Value, dayOfWeekChart: HorizontalBarChart) {
        val bars = values.groupByDay().map {
            Bar(percentage(it.value, values.total()), FREQUENCY.color, it.key.label)
        }

        dayOfWeekChart.setData(100, bars)
    }

    private fun refreshBarCharts(values: Value, viewpager: ViewPager) {
        val adapter = YearlyChartsPagerAdapter(context!!, FREQUENCY.color, "Numero di allenamenti ", values)
        viewpager.adapter = adapter
        viewpager.currentItem = adapter.count - 1
    }
}

