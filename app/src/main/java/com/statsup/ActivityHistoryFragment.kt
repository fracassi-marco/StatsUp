package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.statsup.R.string.empty_activities
import kotlinx.android.synthetic.main.activity_history_fragment.view.*
import kotlinx.android.synthetic.main.no_items_layout.view.*

//https://stackoverflow.com/questions/30895580/recyclerview-no-adapter-attached-skipping-layout-for-recyclerview-in-fragmen
class ActivityHistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_history_fragment, container, false)
        val noItemLayout = view.no_item_layout
        noItemLayout.label_text.text = resources.getString(empty_activities)

        val recyclerView = view.recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ActivityHistoryAdapter(ActivityRepository.all())
        recyclerView.addItemDecoration(VerticalDividerItemDecoration(40))

        showContentOrEmptyPage(noItemLayout, view.recycler_view)

        return view
    }

    private fun showContentOrEmptyPage(noItemLayout: View, viewPager: View) {
        if (ActivityRepository.anyActivities()) {
            noItemLayout.visibility = GONE
            viewPager.visibility = VISIBLE
        } else {
            noItemLayout.visibility = VISIBLE
            viewPager.visibility = GONE
        }
    }
}

