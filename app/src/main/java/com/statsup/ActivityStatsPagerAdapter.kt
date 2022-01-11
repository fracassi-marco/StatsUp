package com.statsup

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter


class ActivityStatsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = Stats.values().size

    override fun createFragment(position: Int) = StatsFragment().apply {
        arguments = Bundle().apply {
            putSerializable("stats", Stats.at(position))
        }
    }
}
