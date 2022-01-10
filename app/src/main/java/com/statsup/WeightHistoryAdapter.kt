package com.statsup

import android.app.AlertDialog
import android.graphics.Color.GREEN
import android.graphics.Color.RED
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.statsup.databinding.WeightHistoryListItemBinding
import org.joda.time.format.DateTimeFormat


class WeightHistoryAdapter : RecyclerView.Adapter<WeightHistoryAdapter.Holder>() {
    inner class Holder(val binding: WeightHistoryListItemBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            AlertDialog
                .Builder(binding.root.context)
                .setMessage("Vuoi cancellare questa pesata?")
                .setNegativeButton("No") { _, _ -> }
                .setPositiveButton("Si") { _, _ ->
                    WeightRepository.delete(
                        binding.root.context,
                        dataSet[adapterPosition]
                    )
                    update(WeightRepository.all())
                }
                .show()
        }
    }

    private var dataSet: List<Weight> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = WeightHistoryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = dataSet[position]
        holder.binding.weightText.text = String.format("%.1f", item.kilograms) + "Kg"
        holder.binding.dateText.text =
            item.date().toString(DateTimeFormat.forPattern("dd/MM/yyyy\nHH:mm"))
        holder.binding.delta.setTextColor(GREEN)
        if (position != dataSet.size -1) {
            val initialValue = dataSet[position + 1].kilograms
            val finalValue = item.kilograms
            val percentVariation = ((finalValue / initialValue) * 100.0) - 100.0
            holder.binding.delta.text = Measure.of(percentVariation, "%")
            if(percentVariation > 0) holder.binding.delta.setTextColor(RED)
        } else {
            holder.binding.delta.text = Measure.of(0.0, "%")
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun update(newItems: List<Weight>) {

        val orderedNewItems = newItems.sortedByDescending { it.dateInMillis }

        val diff = DiffCallback(orderedNewItems, dataSet)
        val diffResult = DiffUtil.calculateDiff(diff)

        diffResult.dispatchUpdatesTo(this)
        dataSet = orderedNewItems
    }
}

