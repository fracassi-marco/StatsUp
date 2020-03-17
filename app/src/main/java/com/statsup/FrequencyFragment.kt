package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.statsup.ActivityTabs.FREQUENCY
import kotlinx.android.synthetic.main.frequency_fragment.view.*
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.view.LineChartView
import lecho.lib.hellocharts.view.PieChartView
import kotlin.math.roundToInt


class FrequencyFragment : Fragment() {

    private fun listener(
        pieChart: PieChartView,
        monthOverMonthChart: LineChartView,
        viewpager: ViewPager
    ) = object : Listener<List<Activity>> {
        override fun update(subject: List<Activity>) {

            if (subject.isEmpty()) {
                return
            }

            val activities = Activities(subject)
            refreshBarCharts(activities, viewpager)
            refreshPieChart(activities, pieChart)
            refreshMonthOverMonthChart(activities, monthOverMonthChart)
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
        val pieChart = view.frequency_pie_chart
        pieChart.isInteractive = false
        val monthOverMonthChart = view.month_over_month_chart

        val listener = listener(pieChart, monthOverMonthChart, viewpager)
        ActivityRepository.listen("FrequencyFragment", listener)

        return view
    }

    private fun refreshMonthOverMonthChart(
        activities: Activities,
        monthOverMonthChart: LineChartView
    ) {
        val value = Frequencies(activities)
        MonthOverMonthChart(monthOverMonthChart, FREQUENCY.color).refresh(value)
    }

    private fun refreshPieChart(
        activities: Activities,
        pieChart: PieChartView
    ) {
        val byDay = activities.frequencyByDay()

        val values = byDay
            .map {
            val value = SliceValue(it.value.size.toFloat(), FREQUENCY.color)
            value.setLabel("${day(it.key)} ${percentage(it.value.size, activities.count())}%")
            value
        }

        val data = PieChartData(values).apply { isValueLabelBackgroundEnabled = false }
        data.setHasLabels(true)
        data.setHasCenterCircle(true)

        data.centerText1 = "Distribuzione"
        data.centerText1FontSize = 25
        data.centerText2 = "per giorno"
        data.centerText2FontSize = 25

        pieChart.pieChartData = data
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
