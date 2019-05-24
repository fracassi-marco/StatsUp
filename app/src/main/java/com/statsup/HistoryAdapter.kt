package com.statsup

import android.support.v7.util.DiffUtil
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.history_list_item.view.*
import org.joda.time.format.DateTimeFormat


class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.Holder>() {
    class Holder(val layout: CardView) : RecyclerView.ViewHolder(layout)

    private var dataSet: List<Activity> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_list_item, parent, false) as CardView

        return Holder(item)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val activity = dataSet[position]

        holder.layout.history_list_item_title_text.text = "Attività"
        holder.layout.history_list_item_icon.setImageResource(activity.sport.icon)
        holder.layout.history_list_item_date_text.text = activity.date().toString(DateTimeFormat.forPattern("dd/MM/yyyy\nHH:mm:ss"))
        val hours = activity.durationInSeconds / 3600;
        val minutes = (activity.durationInSeconds % 3600) / 60;
        val seconds = activity.durationInSeconds % 60;
        holder.layout.history_list_item_time_text.text = "${hours}h ${minutes}m ${seconds}s"
        val div = activity.distanceInMeters.div(1000f)
        holder.layout.history_list_item_distance_text.text = String.format("%.2f", div) + "km"
        holder.layout.history_list_item_pace_text.text = ""
    }

    override fun getItemCount() = dataSet.size
    fun update(users: List<Activity>) {

        val diff = DiffCallback(users, dataSet)
        val diffResult = DiffUtil.calculateDiff(diff)

        diffResult.dispatchUpdatesTo(this)
        dataSet = users
    }
}

