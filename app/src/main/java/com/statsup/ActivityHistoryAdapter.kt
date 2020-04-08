package com.statsup

import android.support.v7.util.DiffUtil
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.statsup.ActivityView.fill


class ActivityHistoryAdapter(private var dataSet: List<Activity>) : RecyclerView.Adapter<ActivityHistoryAdapter.Holder>() {
    class Holder(val layout: CardView) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_history_list_item, parent, false) as CardView

        return Holder(item)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        fill(holder.layout, dataSet[position])
    }

    fun update(newItems: List<Activity>) {
        val orderedNewItems = newItems.sortedByDescending { it.dateInMillis }

        val diff = DiffCallback(orderedNewItems, dataSet)
        val diffResult = DiffUtil.calculateDiff(diff)

        diffResult.dispatchUpdatesTo(this)
        dataSet = orderedNewItems
    }

    override fun getItemCount() = dataSet.size
}

