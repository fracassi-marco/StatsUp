package com.statsup

import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.statsup.Content.showActivitiesOrEmptyPage
import kotlinx.android.synthetic.main.activity_history_fragment.view.*
import kotlinx.android.synthetic.main.no_activities_layout.view.*


//https://stackoverflow.com/questions/30895580/recyclerview-no-adapter-attached-skipping-layout-for-recyclerview-in-fragmen
class ActivityHistoryFragment : ActivityFragment() {

    private lateinit var historyAdapter: ActivityHistoryAdapter

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.activity_history_fragment, container, false)

        historyAdapter = ActivityHistoryAdapter(ActivityRepository.filterBySelectedSport())
        val recyclerView = view.recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = historyAdapter
        recyclerView.addItemDecoration(VerticalDividerItemDecoration(40))

        view.no_activities_layout.import_button.setOnClickListener {
            (activity as MainActivity).startActivitiesImport()
        }

        showActivitiesOrEmptyPage(view.no_activities_layout, view.recycler_view)

        return view
    }

    override fun onFilterChange() {
        historyAdapter.update(ActivityRepository.filterBySelectedSport())
    }
}

