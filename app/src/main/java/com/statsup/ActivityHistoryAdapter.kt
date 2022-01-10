package com.statsup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.statsup.ActivityView.fill
import com.statsup.databinding.ActivityHistoryListItemBinding


class ActivityHistoryAdapter(private var dataSet: List<Activity>) : RecyclerView.Adapter<ActivityHistoryAdapter.Holder>() {
    class Holder(val binding: ActivityHistoryListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val item = ActivityHistoryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(item)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val activity = dataSet[position]
        fill(holder.binding, activity)
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

