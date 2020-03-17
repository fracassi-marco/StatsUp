package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.duration_fragment.view.*
import lecho.lib.hellocharts.view.LineChartView

class DurationFragment : Fragment() {

    private fun listener(
        viewpager: ViewPager,
        monthOverMonthChart: LineChartView
    ) = object : Listener<List<Activity>> {
        override fun update(subject: List<Activity>) {

            if (subject.isEmpty()) {
                return
            }

            val activities = Activities(subject)
            val value = Durations(activities)
            val adapter = YearlyChartsPagerAdapter(
                context!!,
                activities,
                ActivityTabs.DURATION.color,
                "Ore di allenamento ",
                value
            )
            viewpager.adapter = adapter
            viewpager.currentItem = adapter.count - 1
            MonthOverMonthChart(monthOverMonthChart, ActivityTabs.DISTANCE.color).refresh(value)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.duration_fragment, container, false)
        val viewpager = view.duration_view_pager
        val monthOverMonthChart = view.month_over_month_chart
        monthOverMonthChart.isInteractive = false

        ActivityRepository.listen("DurationFragment", listener(viewpager, monthOverMonthChart))

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        ActivityRepository.removeListener("DurationFragment")
    }
}
