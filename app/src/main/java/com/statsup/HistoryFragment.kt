package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.history_fragment.view.*

//https://stackoverflow.com/questions/30895580/recyclerview-no-adapter-attached-skipping-layout-for-recyclerview-in-fragmen
class HistoryFragment : Fragment() {

    private lateinit var noActivitiesLayout: View
    private lateinit var recyclerView: RecyclerView
    private val adapter: HistoryAdapter = HistoryAdapter()
    private lateinit var noActivitiesListener: Listener<List<Activity>>
    private lateinit var listener: Listener<List<Activity>>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.history_fragment, container, false)
        noActivitiesLayout = view.findViewById(R.id.no_activities_layout)

        recyclerView = view.recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(VerticalDividerItemDecoration(40))

        listener = object : Listener<List<Activity>> {
            override fun update(subject: List<Activity>) {
                adapter.update(subject)
            }
        }
        noActivitiesListener = NoActivitiesListener(recyclerView, noActivitiesLayout)

        ActivityRepository.listen(listener, noActivitiesListener)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        ActivityRepository.removeListener(listener, noActivitiesListener)
    }
}

