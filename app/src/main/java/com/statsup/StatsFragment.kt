package com.statsup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.frequency_fragment.view.*

class StatsFragment : PeriodActivityFragment() {

    private var viewpager: DynamicHeightViewPager? = null
    private lateinit var stats: Stats
    private var first = true

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.monthly_frequency_fragment, container, false)
        stats = arguments!!.get("stats") as Stats
        viewpager = view.view_pager

        onPeriodUpdate()

        return view
    }

    override fun onSportUpdate() {
        val activities = ActivityRepository.filterBySelectedSport()
        val pagerAdapter = Period.pagerAdapter(context!!).apply {
            update(stats, activities)
        }
        viewpager!!.adapter = pagerAdapter
        if(first) {
            first = false
            viewpager!!.currentItem = pagerAdapter.count - 1
        }
    }

    override fun onPeriodUpdate() {
        val activities = ActivityRepository.filterBySelectedSport()
        val pagerAdapter = Period.pagerAdapter(context!!).apply {
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

