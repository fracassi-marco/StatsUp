package com.statsup

import android.content.Intent
import android.os.Bundle
import com.statsup.databinding.ActivityHistoryListItemBinding
import org.joda.time.format.DateTimeFormat

object ActivityView {
    fun fill(binding: ActivityHistoryListItemBinding, activity: Activity) {
        binding.historyListItemTitleText.text = binding.root.resources.getString(activity.sport.title) + " - " + activity.title
        binding.historyListItemIcon.setImageResource(activity.sport.icon)
        binding.historyListItemDateText.text = activity.date().toString(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm"))
        binding.historyListItemTimeText.text = Measure.timeFragments(activity.durationInSeconds)
        binding.historyListItemDistanceText.text = Measure.of(activity.distanceInKilometers(), "Km", "", "- ")
        binding.historyListItemPaceText.text = Measure.minutesAndSeconds(activity.paceInSecondsPerKilometer(), "/Km")
        binding.historyListItemElevationText.text = Measure.of(activity.elevationInMeters, "m", "", "- ")

        binding.root.setOnClickListener {
            val intent = Intent(binding.root.context, ActivityDetailsActivity::class.java)
            intent.putExtra("id", activity.id)
            val bundle = Bundle().apply { putLong("id", activity.id) }
            binding.root.context.startActivity(intent, bundle)
        }
    }
}
