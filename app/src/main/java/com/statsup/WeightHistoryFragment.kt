package com.statsup

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.no_items_layout.view.*
import kotlinx.android.synthetic.main.weight_history_fragment.view.*
import org.joda.time.DateTime

class WeightHistoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.weight_history_fragment, container, false)
        val noItemLayout = view.no_items_layout

        val adapter = WeightHistoryAdapter()
        val recyclerView = view.recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(VerticalDividerItemDecoration(40))

        var latestWeight = Weight(50.0, DateTime().millis)
        view.add_weight_button.setOnClickListener {
            val intent = Intent(context, WeightEditorView::class.java)
            intent.putExtra("latestKilograms", latestWeight.kilograms)
            startActivity(intent)
        }

        val listener: Listener<List<Weight>> = object : Listener<List<Weight>> {
            override fun update(subject: List<Weight>) {
                if(subject.isEmpty()) {
                    return
                }

                val items = subject.sortedByDescending { it.dateInMillis }
                latestWeight = items.first()
                adapter.update(items)
                recyclerView.scrollToPosition(0)
            }
        }
        val noItemListener: Listener<List<Weight>> = NoItemsListener(recyclerView, noItemLayout)

        WeightRepository.listen("WeightHistoryFragment1", listener)
        WeightRepository.listen("WeightHistoryFragment2", noItemListener)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        WeightRepository.removeListener("WeightHistoryFragment1")
        WeightRepository.removeListener("WeightHistoryFragment2")
    }
}
