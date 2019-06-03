package com.statsup

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
        viewPager.adapter = ActivityStatsPagerAdapter(fragmentManager)

        val tabLayout = view.findViewById<TabLayout>(R.id.stats_tab_layout)
        tabLayout.setupWithViewPager(viewPager)

        return view
    }
}

class ActivityStatsPagerAdapter(fragmentManager: FragmentManager?) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FrequencyFragment()
            1 -> DurationFragment()
            else -> DistanceFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Frequenza"
            1 -> "Durata"
            else -> "Distanza"
        }
    }

    override fun getCount(): Int {
        return 3
    }
}

