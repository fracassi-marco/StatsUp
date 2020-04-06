package com.statsup

import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import kotlinx.android.synthetic.main.no_items_layout.view.*
import kotlinx.android.synthetic.main.weight_history_fragment.view.*
import org.joda.time.DateTime

class WeightHistoryFragment : NoMenuFragment() {
    private var noItemsLayout: ConstraintLayout? = null
    private var recyclerView: RecyclerView? = null
    private val adapter = WeightHistoryAdapter()

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.weight_history_fragment, container, false)

        recyclerView = view.recycler_view
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
        recyclerView!!.addItemDecoration(VerticalDividerItemDecoration(40))
        noItemsLayout = view.no_items_layout

        view.add_weight_button.setOnClickListener {
            val intent = Intent(context, WeightEditorView::class.java)
            intent.putExtra("latestKilograms", WeightRepository.latest().kilograms)
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if(WeightRepository.any()) {
            val items = WeightRepository.all()
            adapter.update(items)
        }

        showContentOrEmptyPage(noItemsLayout!!, recyclerView!!)
    }

    private fun showContentOrEmptyPage(noItemView: View, contentView: View) {
        if (WeightRepository.any()) {
            noItemView.visibility = GONE
            contentView.visibility = VISIBLE
        } else {
            noItemView.visibility = VISIBLE
            contentView.visibility = GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        noItemsLayout = null
        recyclerView = null
    }
}
