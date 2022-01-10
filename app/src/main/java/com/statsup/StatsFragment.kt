package com.statsup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.statsup.databinding.StatsFragmentBinding

class StatsFragment : PeriodActivityFragment() {

    private lateinit var stats: Stats
    private var latestPeriod = -1
    private var latestSport = -1
    private var _binding: StatsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = StatsFragmentBinding.inflate(inflater, container, false)
        stats = arguments!!.get("stats") as Stats

        onResume()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (latestPeriod != PeriodFilter.current.ordinal || latestSport != ActivityRepository.selectedSportPosition) {
            onFilterChange()
        }
    }

    override fun onFilterChange() {
        latestPeriod = PeriodFilter.current.ordinal
        latestSport = ActivityRepository.selectedSportPosition
        val activities = ActivityRepository.filterBySelectedSport()
        val pagerAdapter = PeriodFilter.pagerAdapter(context!!).apply {
            update(stats, activities)
        }
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.currentItem = pagerAdapter.count - 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

