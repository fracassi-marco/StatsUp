package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter


class ActivityStatsPagerAdapter(private val period: String, fragmentManager: FragmentManager?) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {
        return StatsFragment().apply { arguments =
            Bundle().apply {
                putSerializable("stats", Stats.at(position))
                putSerializable("period", period)
            }
        }
    }

    override fun getPageTitle(position: Int) = Stats.at(position).title

    override fun getCount()= Stats.values().size
}
