package com.statsup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.statsup.Content.showActivitiesOrEmptyPage
import com.statsup.databinding.ActivityHistoryFragmentBinding

//https://stackoverflow.com/questions/30895580/recyclerview-no-adapter-attached-skipping-layout-for-recyclerview-in-fragmen
class ActivityHistoryFragment : ActivityFragment() {

    private var _binding: ActivityHistoryFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyAdapter: ActivityHistoryAdapter

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = ActivityHistoryFragmentBinding.inflate(inflater, container, false)

        historyAdapter = ActivityHistoryAdapter(ActivityRepository.filterBySelectedSport())
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = historyAdapter
        binding.recyclerView.addItemDecoration(VerticalDividerItemDecoration(40))

        showActivitiesOrEmptyPage(binding.noActivitiesLayout, binding.recyclerView)

        return binding.root
    }

    override fun onFilterChange() {
        historyAdapter.update(ActivityRepository.filterBySelectedSport())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

