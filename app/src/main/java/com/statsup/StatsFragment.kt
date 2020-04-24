package com.statsup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.frequency_fragment.view.*

class StatsFragment : ActivityFragment() {

    private var viewpager: DynamicHeightViewPager? = null
    private lateinit var stats: Stats

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.monthly_frequency_fragment, container, false)
        stats = arguments!!.get("stats") as Stats
        viewpager = view.view_pager

        onActivityUpdate(ActivityRepository.filterBySelectedSport())

        return view
    }

    override fun onActivityUpdate(activities: List<Activity>) {
        val adapter = when (arguments!!.getString("period")) {
            "annual" -> AnnualChartsPagerAdapter(context!!, stats, activities)
            "monthly" -> MonthlyChartsPagerAdapter(context!!, stats, activities)
            else -> AnnualChartsPagerAdapter(context!!, stats, activities)
        }
        viewpager!!.adapter = adapter
        viewpager!!.currentItem = adapter.count - 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewpager = null
    }
}

