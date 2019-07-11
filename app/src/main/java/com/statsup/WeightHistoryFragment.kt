package com.statsup

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.weight_history_fragment.view.*
import org.joda.time.DateTime

class WeightHistoryFragment : Fragment() {
    //private lateinit var noActivitiesLayout: View
    private lateinit var recyclerView: RecyclerView
    private val adapter = WeightHistoryAdapter()
    //private lateinit var noActivitiesListener: Listener<List<Weight>>
    private lateinit var listener: Listener<List<Weight>>
    private var latestWeight = Weight(50.0, DateTime().millis)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.weight_history_fragment, container, false)
        //noActivitiesLayout = view.findViewById(R.id.no_activities_layout)

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
        //noActivitiesListener = NoActivitiesListener(recyclerView, noActivitiesLayout)

        WeightRepository.listen(listener/*, noActivitiesListener*/)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        WeightRepository.removeListener(listener/*, noActivitiesListener*/)
    }
}
