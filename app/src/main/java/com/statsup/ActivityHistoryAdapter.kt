package com.statsup

import android.support.v7.util.DiffUtil
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_history_list_item.view.*
import org.joda.time.format.DateTimeFormat


class ActivityHistoryAdapter(private var dataSet: List<Activity>) : RecyclerView.Adapter<ActivityHistoryAdapter.Holder>() {
    class Holder(val layout: CardView) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_history_list_item, parent, false) as CardView

        return Holder(item)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val activity = dataSet[position]

        holder.layout.history_list_item_title_text.text = activity.title
        holder.layout.history_list_item_icon.setImageResource(activity.sport.icon)
        holder.layout.history_list_item_date_text.text = activity.date().toString(DateTimeFormat.forPattern("dd/MM/yyyy\nHH:mm"))
        holder.layout.history_list_item_time_text.text = Measure.timeFragments(activity.durationInSeconds)
        holder.layout.history_list_item_distance_text.text = Measure.of(activity.distanceInKilometers(), "Km", "")
        holder.layout.history_list_item_pace_text.text = ""
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

