package com.statsup

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class WeightStatsPagerAdapter(fragmentManager: FragmentManager?) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return WeightTabs.at(position).fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return WeightTabs.at(position).label
    }

    override fun getCount(): Int {
        return WeightTabs.values().count()
    }
}