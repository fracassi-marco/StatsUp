package com.statsup

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.statsup.Content.showWeightsOrEmptyPage
import com.statsup.databinding.WeightStatsFragmentBinding

class WeightStatsFragment : NoMenuFragment() {

    private var _binding: WeightStatsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = WeightStatsFragmentBinding.inflate(inflater, container, false)
        binding.statsViewPager.adapter = WeightStatsPagerAdapter(this)
        binding.statsViewPager.isUserInputEnabled = false

        TabLayoutMediator(binding.statsTabLayout, binding.statsViewPager) { tab, position ->
            tab.text = WeightTabs.at(position).label
        }.attach()

        binding.statsTabLayout.also {
            it.setSelectedTabIndicatorColor(WeightTabs.at(0).color)
            it.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(p0: TabLayout.Tab) {

                }

                override fun onTabUnselected(p0: TabLayout.Tab?) {
                    it.setSelectedTabIndicatorColor(Color.rgb(0, 0, 0))
                }

                override fun onTabSelected(tab: TabLayout.Tab) {
                    it.setSelectedTabIndicatorColor(WeightTabs.at(tab.position).color)
                }
            })
        }

        showWeightsOrEmptyPage(binding.noItemsLayout, binding.statsViewPager)

        return binding.root
    }
}

