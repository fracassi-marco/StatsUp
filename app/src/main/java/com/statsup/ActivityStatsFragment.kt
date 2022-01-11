package com.statsup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.statsup.Content.showActivitiesOrEmptyPage
import com.statsup.databinding.ActivityStatsFragmentBinding

class ActivityStatsFragment : Fragment() {

    private var _binding: ActivityStatsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityStatsFragmentBinding.inflate(inflater, container, false)
        binding.statsViewPager.adapter = ActivityStatsPagerAdapter(this)
        binding.statsViewPager.offscreenPageLimit = 3

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

        showActivitiesOrEmptyPage(binding.noActivitiesLayout, binding.statsViewPager)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


