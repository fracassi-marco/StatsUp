package com.statsup

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.statsup.Content.showActivitiesOrEmptyPage
import kotlinx.android.synthetic.main.activity_stats_fragment.view.*
import kotlinx.android.synthetic.main.no_activities_layout.view.*

class ActivityStatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_stats_fragment, container, false)
        val viewPager = view.stats_view_pager
        val period = arguments!!.get("period") as String
        viewPager.adapter = ActivityStatsPagerAdapter(period, childFragmentManager)

        view.stats_tab_layout.also {
            it.setupWithViewPager(viewPager)
            it.setSelectedTabIndicatorColor(Stats.at(0).color)
            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(p0: TabLayout.Tab) {
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                }

                override fun onTabSelected(tab: TabLayout.Tab) {
                    it.setSelectedTabIndicatorColor(Stats.at(tab.position).color)
                }
            })
        }

        view.no_activities_layout.import_button.setOnClickListener {
            (activity as MainActivity).startActivitiesImport()
        }

        showActivitiesOrEmptyPage(view.no_activities_layout, viewPager)

        return view
    }
}


