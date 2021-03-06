package com.statsup

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.statsup.ActivityView.fill


class ActivityHistoryAdapter(private var dataSet: List<Activity>) : RecyclerView.Adapter<ActivityHistoryAdapter.Holder>() {
    class Holder(val layout: CardView) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.activity_history_list_item, parent, false) as CardView
        return Holder(item)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val activity = dataSet[position]
        fill(holder.layout, activity)
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

