package com.statsup

import android.app.AlertDialog
import android.support.v7.util.DiffUtil
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.weight_history_list_item.view.*
import org.joda.time.format.DateTimeFormat


class WeightHistoryAdapter : RecyclerView.Adapter<WeightHistoryAdapter.Holder>() {
    inner class Holder(val layout: CardView) : RecyclerView.ViewHolder(layout), View.OnClickListener {

        init {
            layout.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            AlertDialog
                .Builder(layout.context)
                .setMessage("Vuoi cancellare questa pesata?")
                .setNegativeButton("No") { _, _ -> }
                .setPositiveButton("Si") { _, _ -> WeightRepository.delete(dataSet[adapterPosition]) }
                .show()
        }
    }

    private var dataSet: List<Weight> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.weight_history_list_item, parent, false) as CardView

        return Holder(item)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = dataSet[position]
        holder.layout.weight_text.text = String.format("%.1f", item.kilograms) + "Kg"
        holder.layout.date_text.text = item.date().toString(DateTimeFormat.forPattern("dd/MM/yyyy\nHH:mm"))
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun update(newItems: List<Weight>) {
        val diff = DiffCallback(newItems, dataSet)
        val diffResult = DiffUtil.calculateDiff(diff)

        diffResult.dispatchUpdatesTo(this)
        dataSet = newItems
    }
}
