package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.statsup.ActivityTabs.FREQUENCY
import com.statsup.barchart.Bar
import com.statsup.barchart.HorizontalBarChart
import kotlinx.android.synthetic.main.frequency_fragment.view.*
import lecho.lib.hellocharts.view.LineChartView
import kotlin.math.roundToInt


class FrequencyFragment : Fragment() {

    private fun listener(
        dayOfWeekChart: HorizontalBarChart,
        monthOverMonthChart: LineChartView,
        monthOverMonthTitle: TextView,
        viewpager: ViewPager
    ) = object : Listener<List<Activity>> {
        override fun update(subject: List<Activity>) {

            if (subject.isEmpty()) {
                return
            }

            val activities = Activities(subject)
            refreshBarCharts(activities, viewpager)
            refreshPieChart(activities, dayOfWeekChart)
            refreshMonthOverMonthChart(activities, monthOverMonthChart, monthOverMonthTitle)
        }
    }

    private fun refreshBarCharts(
        activities: Activities,
        viewpager: ViewPager
    ) {
        val adapter = YearlyChartsPagerAdapter(
            context!!,
            activities,
            FREQUENCY.color,
            "Numero di allenamenti ",
            Frequencies(activities)
        )
        viewpager.adapter = adapter
        viewpager.currentItem = adapter.count - 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frequency_fragment, container, false)
        val viewpager = view.frequency_view_pager
        val dayOfWeekChart = view.day_of_week_cart
        val monthOverMonthChart = view.month_over_month_chart
        val monthOverMonthChartTitle = view.month_over_month_title

        val listener = listener(dayOfWeekChart, monthOverMonthChart, monthOverMonthChartTitle, viewpager)
        ActivityRepository.listen("FrequencyFragment", listener)

        return view
    }

    private fun refreshMonthOverMonthChart(
        activities: Activities,
        monthOverMonthChart: LineChartView,
        monthOverMonthTitle: TextView
    ) {
        val value = Frequencies(activities)
        MonthOverMonthChart(monthOverMonthChart, monthOverMonthTitle, FREQUENCY.color).refresh(value)
    }

    private fun refreshPieChart(activities: Activities, dayOfWeekChart: HorizontalBarChart) {
        val bars = activities.frequencyByDay().map {
            Bar(percentage(it.value.size, activities.count()), FREQUENCY.color, day(it.key))
        }

        dayOfWeekChart.setData(100, bars)
    }

    private fun percentage(actualCounter: Int, allCounter: Int): Int {
        return (actualCounter/allCounter.toFloat() * 100).roundToInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        ActivityRepository.removeListener("FrequencyFragment")
    }

    private fun day(position: Int) : String {
        return when(position) {
            1 -> "Lun"
            2 -> "Mar"
            3 -> "Mer"
            4 -> "Gio"
            5 -> "Ven"
            6 -> "Sab"
            else -> "Dom"
        }
    }
}
