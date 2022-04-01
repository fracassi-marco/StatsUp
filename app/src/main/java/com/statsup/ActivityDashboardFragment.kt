package com.statsup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.statsup.databinding.ActivityDashboardFragmentBinding

class ActivityDashboardFragment : Fragment() {

    private var _binding: ActivityDashboardFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityDashboardFragmentBinding.inflate(inflater, container, false)
        setHasOptionsMenu(false)

        val activities = ActivityRepository.ofMonth(Month().previous())
        binding.value1.text = Measure.frequency(Stats.FREQUENCY.provider(activities))
        binding.value2.text = Measure.hm(activities.sumOf { activity -> activity.durationInSeconds })
        binding.value3.text = Measure.of(Stats.DISTANCE.provider(activities), "Km", "", "- ")

        Content.showActivitiesOrEmptyPage(binding.noActivitiesLayout, binding.content)

        return binding.root
    }
}