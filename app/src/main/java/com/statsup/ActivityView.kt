package com.statsup

import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_history_list_item.view.*
import org.joda.time.format.DateTimeFormat

object ActivityView {
    fun fill(view: View, activity: Activity) {
        view.history_list_item_title_text.text = view.resources.getString(activity.sport.title) + " - " + activity.title
        view.history_list_item_icon.setImageResource(activity.sport.icon)
        view.history_list_item_date_text.text = activity.date().toString(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm"))
        view.history_list_item_time_text.text = Measure.timeFragments(activity.durationInSeconds)
        view.history_list_item_distance_text.text = Measure.of(activity.distanceInKilometers(), "Km", "", "- ")
        view.history_list_item_pace_text.text = Measure.minutesAndSeconds(activity.paceInSecondsPerKilometer(), "/Km")
        view.history_list_item_elevation_text.text = Measure.of(activity.elevationInMeters, "m", "", "- ")

        view.setOnClickListener {
            val intent = Intent(view.context, ActivityDetailsActivity::class.java)
            intent.putExtra("id", activity.id)
            val bundle = Bundle().apply { putLong("id", activity.id) }
            view.context.startActivity(intent, bundle)
        }
    }
}
