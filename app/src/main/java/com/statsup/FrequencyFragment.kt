package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.view.PieChartView
import kotlin.math.roundToInt


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
            ActivityTabs.FREQUENCY.color,
            "Numero di allenamenti ",
            Frequencies(activities)
        )
        viewpager.adapter = adapter
        viewpager.currentItem = adapter.count - 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frequency_fragment, container, false)
        viewpager = view.findViewById(R.id.frequency_view_pager)
        pieChart = view.findViewById(R.id.frequency_pie_chart)
        pieChart.isInteractive = false

        ActivityRepository.listen(listener)

        return view
    }

    private fun refreshPieChart(activities: Activities) {
        val byDay = activities.frequencyByDay()

        val values = byDay
            .map {
            val value = SliceValue(it.value.size.toFloat(), ActivityTabs.FREQUENCY.color)
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

        ActivityRepository.removeListener(listener)
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
