package com.statsup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_records_fragment.view.*
import kotlinx.android.synthetic.main.no_activities_layout.view.*
import kotlinx.android.synthetic.main.activity_history_list_item.view.*
import org.joda.time.format.DateTimeFormat

class ActivityRecordsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_records_fragment, container, false)

        if (ActivityRepository.anyActivities()) {
            val bestAverageSpeed = ActivityRepository.all().maxBy { it.averageSpeedInKmH() }!!
            view.average_speed_value.text = Measure.of(bestAverageSpeed.maxSpeedInKilometersPerHours(), " Km/h", "")
            update(view.item_average_speed, bestAverageSpeed)

            val bestSpeed = ActivityRepository.all().maxBy { it.maxSpeedInMetersPerSecond }!!
            view.speed_value.text = Measure.of(bestSpeed.maxSpeedInKilometersPerHours(), " Km/h", "")
            update(view.item_speed, bestSpeed)

            val bestDuration = ActivityRepository.all().maxBy { it.durationInSeconds }!!
            view.duration_value.text = Measure.timeFragments(bestDuration.durationInSeconds)
            update(view.item_duration, bestDuration)

            val bestDistance = ActivityRepository.all().maxBy { it.distanceInMeters }!!
            view.distance_value.text = Measure.of(bestDistance.distanceInKilometers(), " Km", "")
            update(view.item_distance, bestDistance)
        }

        showActivitiesOrEmptyPage(view.no_activities_layout, view.records)

        return view
    }

    private fun showActivitiesOrEmptyPage(noItemLayout: View, viewPager: View) {
        if (ActivityRepository.anyActivities()) {
            noItemLayout.visibility = GONE
            viewPager.visibility = VISIBLE
        } else {
            noItemLayout.visibility = VISIBLE
            viewPager.visibility = GONE
        }
    }

    fun update(view: View, activity: Activity) {
        view.history_list_item_title_text.text = activity.title
        view.history_list_item_icon.setImageResource(activity.sport.icon)
        view.history_list_item_date_text.text = activity.date().toString(DateTimeFormat.forPattern("dd/MM/yyyy\nHH:mm"))
        view.history_list_item_time_text.text = Measure.timeFragments(activity.durationInSeconds)
        view.history_list_item_distance_text.text = Measure.of(activity.distanceInKilometers(), "Km", "")
        view.history_list_item_pace_text.text = ""
    }
}