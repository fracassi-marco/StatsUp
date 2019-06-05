package com.statsup

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ActivityStatsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_stats_fragment, container, false)

        val viewPager = view.findViewById<ViewPager>(R.id.stats_view_pager)
        viewPager.adapter = ActivityStatsPagerAdapter(childFragmentManager)

        view.findViewById<TabLayout>(R.id.stats_tab_layout).also {
            it.setupWithViewPager(viewPager)
            it.setSelectedTabIndicatorColor(Tabs.at(0).color)
            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(p0: TabLayout.Tab?) {

                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                    it.setSelectedTabIndicatorColor(Color.rgb(0, 0, 0))
                }

                override fun onTabSelected(tab: TabLayout.Tab) {
                    it.setSelectedTabIndicatorColor(Tabs.at(tab.position).color)
                }
            })
        }

        return view
    }
}

class ActivityStatsPagerAdapter(fragmentManager: FragmentManager?) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return Tabs.at(position).fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return Tabs.at(position).label
    }

    override fun getCount(): Int {
        return Tabs.values().count()
    }
}

enum class Tabs(val position: Int, val label: String, val color: Int, val fragment: Fragment) {
    FREQUENCY(0, "Frequenza", Color.rgb(76, 175, 80), FrequencyFragment()),
    DURATION(1, "Durata", Color.rgb(33, 150, 243), DurationFragment()),
    DISTANCE(2, "Distanza", Color.rgb(244, 67, 54), DistanceFragment());

    companion object {
        fun at(position: Int): Tabs {
            return values().single { it.position == position }
        }
    }
}

