package com.statsup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.stats_fragment.view.*

class StatsFragment : PeriodActivityFragment() {

    private var viewpager: DynamicHeightViewPager? = null
    private lateinit var stats: Stats
    private var latestPeriod = -1
    private var latestSport = -1

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.stats_fragment, container, false)
        stats = arguments!!.get("stats") as Stats
        viewpager = view.view_pager

        onResume()

        return view
    }

    override fun onResume() {
        super.onResume()
        if (latestPeriod != PeriodFilter.current.ordinal || latestSport != ActivityRepository.selectedSportPosition) {
            onFilterChange()
        }
    }

    override fun onFilterChange() {
        latestPeriod = PeriodFilter.current.ordinal
        latestSport = ActivityRepository.selectedSportPosition
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

