package com.statsup

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

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