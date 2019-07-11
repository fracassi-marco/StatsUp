package com.statsup

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.no_items_layout.view.*
import kotlinx.android.synthetic.main.weight_history_fragment.view.*
import org.joda.time.DateTime

class WeightHistoryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private val adapter = WeightHistoryAdapter()
    private lateinit var noItemListener: Listener<List<Weight>>
    private lateinit var listener: Listener<List<Weight>>
    private var latestWeight = Weight(50.0, DateTime().millis)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.weight_history_fragment, container, false)
        val noItemLayout = view.findViewById<View>(R.id.no_item_layout)
        noItemLayout.no_activities_text_view.text = resources.getString(R.string.empty_weight)

        recyclerView = view.recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(VerticalDividerItemDecoration(40))

        view.add_weight_button.setOnClickListener { _ ->
            val intent = Intent(context, WeightEditorView::class.java)
            intent.putExtra("latestKilograms", latestWeight.kilograms)
            startActivity(intent)
        }

        listener = object : Listener<List<Weight>> {
            override fun update(subject: List<Weight>) {
                if(subject.isEmpty()) {
                    return
                }

                val items = subject.sortedByDescending { it.dateInMillis }
                latestWeight = items.first()
                adapter.update(items)
            }
        }
        noItemListener = NoActivitiesListener(recyclerView, noItemLayout)

        WeightRepository.listen(listener, noItemListener)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        WeightRepository.removeListener(listener, noItemListener)
    }
}
