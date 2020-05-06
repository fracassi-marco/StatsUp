package com.statsup

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class WeightStatsPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

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