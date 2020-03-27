package com.statsup

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_stats_fragment.view.*
import kotlinx.android.synthetic.main.no_activities_layout.view.*

class ActivityStatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_stats_fragment, container, false)
        val viewPager = view.stats_view_pager
        viewPager.adapter = ActivityStatsPagerAdapter(childFragmentManager)

        view.stats_tab_layout.also {
            it.setupWithViewPager(viewPager)
            it.setSelectedTabIndicatorColor(ActivityTabs.at(0).color)
            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(p0: TabLayout.Tab) {
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                }

                override fun onTabSelected(tab: TabLayout.Tab) {
                    it.setSelectedTabIndicatorColor(ActivityTabs.at(tab.position).color)
                }
            })
        }

        view.no_activities_layout.import_button.setOnClickListener {
            (activity as MainActivity).startImportFromStrava()
        }

        showActivitiesOrEmptyPage(view.no_activities_layout, viewPager)

        return view
    }

    private fun showActivitiesOrEmptyPage(
        noItemLayout: ConstraintLayout,
        viewPager: UnswappableViewPager
    ) {
        if (ActivityRepository.anyActivities()) {
            noItemLayout.visibility = GONE
            viewPager.visibility = VISIBLE
        } else {
            noItemLayout.visibility = VISIBLE
            viewPager.visibility = GONE
        }
    }
}

