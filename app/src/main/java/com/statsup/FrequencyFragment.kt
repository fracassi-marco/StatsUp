package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.util.ChartUtils
import lecho.lib.hellocharts.view.PieChartView


class FrequencyFragment : Fragment() {

    private lateinit var pieChart: PieChartView
    private lateinit var viewpager: ViewPager

    private val listener = object : Listener<List<Activity>> {
        override fun update(subject: List<Activity>) {

            if (subject.isEmpty()) {
                return
            }

            val activities = Activities(subject)
            refreshBarCharts(activities)
            refreshPieChart(activities)
        }
    }

    private fun refreshBarCharts(activities: Activities) {
        val adapter = YearlyChartsPagerAdapter(
            context!!,
            activities,
            Tabs.FREQUENCY.color,
            "Frequanza ",
            activities.maxMonthlyFrequency(),
            activities.averageMonthlyFrequency()
        ) {
            it.size.toFloat()
        }
        viewpager.adapter = adapter
        viewpager.currentItem = adapter.count - 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frequency_fragment, container, false)
        viewpager = view.findViewById(R.id.frequency_view_pager)
        pieChart = view.findViewById(R.id.frequency_pie_chart)

        ActivityRepository.listen(listener)

        return view
    }

    private fun refreshPieChart(activities: Activities) {
        val byDay = activities.frequencyByDay()

        val values = byDay.map {
            val value = SliceValue(it.value.size.toFloat(), ChartUtils.pickColor())
            value.setLabel(label(it.key) + "\n" + it.value.size)
            value
        }

        val data = PieChartData(values)
        data.setHasLabels(true)
        data.setHasCenterCircle(true)

        data.centerText1 = "Distribuzione"
        data.centerText1FontSize = 25
        data.centerText2 = "per giorno"
        data.centerText2FontSize = 25

        pieChart.pieChartData = data
    }

    override fun onDestroyView() {
        super.onDestroyView()

        ActivityRepository.removeListener(listener)
    }

    private fun label(position: Int) : String {
        return when(position) {
            1 -> "Lunedì"
            2 -> "Martedì"
            3 -> "Mercoledì"
            4 -> "Giovedì"
            5 -> "Venerdì"
            6 -> "Sabato"
            else -> "Domenica"
        }
    }
}
