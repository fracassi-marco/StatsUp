package com.statsup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.statsup.Content.showActivitiesOrEmptyPage
import com.statsup.databinding.ActivityStatsFragmentBinding

class ActivityStatsFragment : PeriodActivityFragment() {

    private var _binding: ActivityStatsFragmentBinding? = null
    private val binding get() = _binding!!
    private var latestPeriod = -1
    private var latestSport = -1

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = ActivityStatsFragmentBinding.inflate(inflater, container, false)
        binding.statsViewPager.adapter = ActivityStatsPagerAdapter(this)
        binding.statsViewPager.offscreenPageLimit = 3
        binding.statsViewPager.isUserInputEnabled = false

        TabLayoutMediator(binding.statsTabLayout, binding.statsViewPager) { tab, position ->
            tab.text = Stats.at(position).title
        }.attach()

        binding.statsTabLayout.also {
            it.setSelectedTabIndicatorColor(Stats.at(0).color)
            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(p0: TabLayout.Tab) {
                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                }

                override fun onTabSelected(tab: TabLayout.Tab) {
                    it.setSelectedTabIndicatorColor(Stats.at(tab.position).color)
                }
            })
        }

        binding.updateActivities.setOnClickListener {
            (binding.root.context as MainActivity).startActivitiesImport()
        }

        showActivitiesOrEmptyPage(binding.noActivitiesLayout, binding.statsViewPager)

        return binding.root
    }

    override fun onFilterChange() {
        latestPeriod = PeriodFilter.current.ordinal
        latestSport = ActivityRepository.selectedSportPosition
        binding.statsViewPager.adapter!!.notifyItemChanged(binding.statsViewPager.currentItem)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


