package com.statsup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.statsup.databinding.StatsFragmentBinding

class StatsFragment : Fragment() {

    private lateinit var stats: Stats
    private var _binding: StatsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = StatsFragmentBinding.inflate(inflater, container, false)
        stats = requireArguments().get("stats") as Stats

        onFilterChange()

        return binding.root
    }

    fun onFilterChange() {
        if(context != null) {
            val pagerAdapter = PeriodFilter.pagerAdapter(requireContext()).apply {
                val activities = ActivityRepository.filterBySelectedSport()
                update(stats, activities)
            }
            binding.viewPager.adapter = pagerAdapter
            binding.viewPager.currentItem = pagerAdapter.count - 1
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

