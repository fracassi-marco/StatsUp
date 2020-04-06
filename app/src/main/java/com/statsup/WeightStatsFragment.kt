package com.statsup

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.no_items_layout.view.*
import kotlinx.android.synthetic.main.weight_stats_fragment.view.*

class WeightStatsFragment : NoMenuFragment() {

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.weight_stats_fragment, container, false)
        val viewPager = view.stats_view_pager.apply {
            adapter = WeightStatsPagerAdapter(childFragmentManager)
        }

        view.stats_tab_layout.also {
            it.setupWithViewPager(viewPager)
            it.setSelectedTabIndicatorColor(WeightTabs.at(0).color)
            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(p0: TabLayout.Tab) {

                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                    it.setSelectedTabIndicatorColor(Color.rgb(0, 0, 0))
                }

                override fun onTabSelected(tab: TabLayout.Tab) {
                    it.setSelectedTabIndicatorColor(WeightTabs.at(tab.position).color)
                }
            })
        }

        showActivitiesOrEmptyPage(view.no_items_layout, viewPager)

        return view
    }

    private fun showActivitiesOrEmptyPage(noItemLayout: View, viewPager: View) {
        if (WeightRepository.any()) {
            noItemLayout.visibility = View.GONE
            viewPager.visibility = View.VISIBLE
        } else {
            noItemLayout.visibility = View.VISIBLE
            viewPager.visibility = View.GONE
        }
    }
}

