package com.statsup

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class ActivityStatsPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return StatsFragment().apply { arguments =
            Bundle().apply {
                putSerializable("stats", Stats.at(position))
            }
        }
    }

    override fun getPageTitle(position: Int) = Stats.at(position).title

    override fun getCount()= Stats.values().size
}
