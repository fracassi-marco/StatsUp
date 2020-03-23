package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.statsup.ActivityTabs.DISTANCE
import kotlinx.android.synthetic.main.distance_fragment.view.*
import lecho.lib.hellocharts.view.LineChartView

class DistanceFragment : Fragment() {

    private fun listener(
        viewpager: ViewPager,
        monthOverMonthChart: LineChartView,
        monthOverMonthTitle: TextView
    ) = object : Listener<List<Activity>> {
        override fun update(subject: List<Activity>) {

            if (subject.isEmpty()) {
                return
            }

            val activities = Activities(subject)
            val value = Distances(activities)
            val adapter = YearlyChartsPagerAdapter(
                context!!,
                activities,
                DISTANCE.color,
                "Chilometri percorsi ",
                value
            )

            viewpager.adapter = adapter
            viewpager.currentItem = adapter.count - 1
            MonthOverMonthChart(monthOverMonthChart, monthOverMonthTitle, DISTANCE.color).refresh(value)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.distance_fragment, container, false)
        val viewpager = view.distance_view_pager
        val monthOverMonthChart = view.month_over_month_chart
        val monthOverMonthTitle = view.month_over_month_title
        monthOverMonthChart.isInteractive = false

        ActivityRepository.listen("DistanceFragment", listener(viewpager, monthOverMonthChart, monthOverMonthTitle))

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        ActivityRepository.removeListener("DistanceFragment")
    }
}

