package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.statsup.R.string.empty_activities
import kotlinx.android.synthetic.main.activity_history_fragment.view.*
import kotlinx.android.synthetic.main.no_items_layout.view.*

//https://stackoverflow.com/questions/30895580/recyclerview-no-adapter-attached-skipping-layout-for-recyclerview-in-fragmen
class ActivityHistoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_history_fragment, container, false)
        val noItemLayout = view.findViewById<View>(R.id.no_item_layout)
        noItemLayout.label_text.text = resources.getString(empty_activities)

        val adapter = ActivityHistoryAdapter()
        val recyclerView =view.recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(VerticalDividerItemDecoration(40))

        val listener = object : Listener<List<Activity>> {
            override fun update(subject: List<Activity>) {
                adapter.update(subject)
                recyclerView.scrollToPosition(0)
            }
        }
        val noActivitiesListener: Listener<List<Activity>> = NoItemsListener(view.recycler_view, noItemLayout)

        ActivityRepository.listen("ActivityHistoryFragment1", listener)
        ActivityRepository.listen("ActivityHistoryFragment2", noActivitiesListener)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        ActivityRepository.removeListener("ActivityHistoryFragment1")
        ActivityRepository.removeListener("ActivityHistoryFragment2")
    }
}

