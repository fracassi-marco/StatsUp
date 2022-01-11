package com.statsup

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class WeightStatsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = WeightTabs.values().count()

    override fun createFragment(position: Int) = WeightTabs.fragment(position)
}