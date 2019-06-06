package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class FrequencyFragment : Fragment() {

    private lateinit var viewpager: ViewPager

    private val listener = object : Listener<List<Activity>> {
        override fun update(subject: List<Activity>) {

            if (subject.isEmpty()) {
                return
            }

            val activities = Activities(subject)
            val adapter = YearlyChartsPagerAdapter(
                context!!,
                activities,
                Tabs.FREQUENCY.color,
                "Frequanza ",
                activities.maxMonthlyFrequency(),
                activities.averageMonthlyFrequency()
            ) {
                it.size.toFloat()
            }
            viewpager.adapter = adapter
            viewpager.currentItem = adapter.count - 1
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frequency_fragment, container, false)
                viewpager = view.findViewById(R.id.frequency_view_pager)

        ActivityRepository.listen(listener)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        ActivityRepository.removeListener(listener)
    }
}
