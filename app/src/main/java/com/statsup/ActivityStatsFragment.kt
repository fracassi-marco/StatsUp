package com.statsup

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ActivityStatsFragment : Fragment() {

    private lateinit var listener: Listener<List<Activity>>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_stats_fragment, container, false)
        val noActivitiesLayout = view.findViewById<View>(R.id.no_activities_layout)
        val viewPager = view.findViewById<UnswappableViewPager>(R.id.stats_view_pager)
        viewPager.adapter = ActivityStatsPagerAdapter(childFragmentManager)

        view.findViewById<TabLayout>(R.id.stats_tab_layout).also {
            it.setupWithViewPager(viewPager)
            it.setSelectedTabIndicatorColor(Tabs.at(0).color)
            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(p0: TabLayout.Tab) {

                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                    it.setSelectedTabIndicatorColor(Color.rgb(0, 0, 0))
                }

                override fun onTabSelected(tab: TabLayout.Tab) {
                    it.setSelectedTabIndicatorColor(Tabs.at(tab.position).color)
                }
            })
        }

        listener = NoActivitiesListener(viewPager, noActivitiesLayout)
        ActivityRepository.listen(listener)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        ActivityRepository.removeListener(listener)
    }
}

