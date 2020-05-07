package com.statsup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.stats_fragment.view.*

class StatsFragment : PeriodActivityFragment() {

    private var viewpager: DynamicHeightViewPager? = null
    private lateinit var stats: Stats
    private lateinit var latestPeriodPosition: Period

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.stats_fragment, container, false)
        stats = arguments!!.get("stats") as Stats
        viewpager = view.view_pager

        latestPeriodPosition = PeriodFilter.current
        onFilterChange()

        return view
    }

    override fun onResume() {
        super.onResume()

        onFilterChange()
    }

    override fun onFilterChange() {
        val activities = ActivityRepository.filterBySelectedSport()
        val pagerAdapter = PeriodFilter.pagerAdapter(context!!).apply {
            update(stats, activities)
        }
        viewpager!!.adapter = pagerAdapter
        viewpager!!.currentItem = pagerAdapter.count - 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewpager = null
    }
}

