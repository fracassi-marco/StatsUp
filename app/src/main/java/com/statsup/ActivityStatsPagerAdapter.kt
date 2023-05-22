package com.statsup

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder


class ActivityStatsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val items: MutableList<StatsFragment> = mutableListOf()
    override fun getItemCount() = Stats.values().size

    override fun createFragment(position: Int): Fragment {
        val fragment = StatsFragment().apply {
            arguments = Bundle().apply {
                putString("stats", Stats.at(position).name)
            }
        }
        items.add(fragment)
        return fragment
    }

    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        items.forEach { it.onFilterChange() }
    }
}
