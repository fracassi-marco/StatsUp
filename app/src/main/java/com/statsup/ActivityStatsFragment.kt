package com.statsup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.statsup.Content.showActivitiesOrEmptyPage
import com.statsup.databinding.ActivityStatsFragmentBinding

class ActivityStatsFragment : Fragment() {

    private var _binding: ActivityStatsFragmentBinding? = null
    private val binding get() = _binding!!
    private var latestPeriod = -1
    private var latestSport = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityStatsFragmentBinding.inflate(inflater, container, false)

        setupSportFilter(binding.toolbar)
        setupPeriodFilter(binding.toolbar)

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

        showActivitiesOrEmptyPage(binding.noActivitiesLayout, binding.statsViewPager)

        return binding.root
    }

    private fun setupPeriodFilter(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.period_filter)
        val spinner = toolbar.menu.findItem(R.id.period_filter).actionView as Spinner

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_dropdown_item,
            Period.values().map { resources.getString(it.label) }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(PeriodFilter.current.ordinal)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                if (PeriodFilter.change(0)) {
                    onFilterChange()
                }
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (PeriodFilter.change(position)) {
                    onFilterChange()
                }
            }
        }
    }

    private fun setupSportFilter(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.sport_filter)
        val spinner = toolbar.menu.findItem(R.id.sport_filter).actionView as Spinner

        val sports = ActivityRepository.sports()
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_dropdown_item,
            sports.map { resources.getString(it.title) }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(ActivityRepository.selectedSportPosition())

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                if(ActivityRepository.changeSport(0)) {
                    onFilterChange()
                }
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if(ActivityRepository.changeSport(position)) {
                    onFilterChange()
                }
            }
        }
    }

    fun onFilterChange() {
        latestPeriod = PeriodFilter.current.ordinal
        latestSport = ActivityRepository.selectedSportPosition
        binding.statsViewPager.adapter!!.notifyItemChanged(binding.statsViewPager.currentItem)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


