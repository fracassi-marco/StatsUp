package com.statsup

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.statsup.Content.showWeightsOrEmptyPage
import com.statsup.databinding.WeightHistoryFragmentBinding


class WeightHistoryFragment : NoMenuFragment() {

    private var _binding: WeightHistoryFragmentBinding? = null
    private val binding get() = _binding!!
    private val adapter = WeightHistoryAdapter()

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = WeightHistoryFragmentBinding.inflate(inflater, container, false)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager =
            LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(VerticalDividerItemDecoration(40))

        binding.addWeightButton.setOnClickListener {
            val intent = Intent(context, WeightEditorView::class.java)
            intent.putExtra("latestKilograms", WeightRepository.latest().kilograms)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if(WeightRepository.any()) {
            val items = WeightRepository.all()
            adapter.update(items)
        }

        showWeightsOrEmptyPage(binding.noItemsLayout, binding.recyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
