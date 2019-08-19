package com.statsup

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class ActivityStatsPagerAdapter(fragmentManager: FragmentManager?) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return ActivityTabs.at(position).fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return ActivityTabs.at(position).label
    }

    override fun getCount(): Int {
        return ActivityTabs.values().count()
    }
}
