package com.statsup

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_history_list_item.view.*
//import kotlinx.android.synthetic.main.activity_records_fragment.view.*
//import kotlinx.android.synthetic.main.record_item.view.*
import kotlinx.android.synthetic.main.no_activities_layout.view.*
import org.joda.time.format.DateTimeFormat

class ActivityRecordsFragment : ActivityFragment() {

    private var averageSpeed: ConstraintLayout?  = null
    private var speed: ConstraintLayout? = null
    private var duration: ConstraintLayout? = null
    private var distance: ConstraintLayout? = null
    private var elevation: ConstraintLayout? = null

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.activity_records_fragment, container, false)
        averageSpeed = view.findViewById<ConstraintLayout>(R.id.average_speed).apply {
            this.findViewById<ImageView>(R.id.image).contentDescription = resources.getString(R.string.records_average_speed)
            this.findViewById<ImageView>(R.id.image).setImageResource(R.drawable.average_speed)
            this.findViewById<TextView>(R.id.label).text = resources.getString(R.string.records_average_speed)
        }
        speed = view.findViewById<ConstraintLayout>(R.id.speed).apply {
            this.findViewById<ImageView>(R.id.image).contentDescription = resources.getString(R.string.records_speed)
            this.findViewById<ImageView>(R.id.image).setImageResource(R.drawable.speed)
            this.findViewById<TextView>(R.id.label).text = resources.getString(R.string.records_speed)
        }
        duration = view.findViewById<ConstraintLayout>(R.id.duration).apply {
            this.findViewById<ImageView>(R.id.image).contentDescription = resources.getString(R.string.records_duration)
            this.findViewById<ImageView>(R.id.image).setImageResource(R.drawable.duration)
            this.findViewById<TextView>(R.id.label).text = resources.getString(R.string.records_duration)
        }
        distance = view.findViewById<ConstraintLayout>(R.id.distance).apply {
            this.findViewById<ImageView>(R.id.image).contentDescription = resources.getString(R.string.records_distance)
            this.findViewById<ImageView>(R.id.image).setImageResource(R.drawable.distance)
            this.findViewById<TextView>(R.id.label).text = resources.getString(R.string.records_distance)
        }
        elevation = view.findViewById<ConstraintLayout>(R.id.total_elevation).apply {
            this.findViewById<ImageView>(R.id.image).contentDescription = resources.getString(R.string.records_elevation)
            this.findViewById<ImageView>(R.id.image).setImageResource(R.drawable.elevation)
            this.findViewById<TextView>(R.id.label).text = resources.getString(R.string.records_elevation)
        }

        onActivityUpdate(ActivityRepository.all())

        showActivitiesOrEmptyPage(view.no_activities_layout, view.findViewById(R.id.records))

        return view
    }

    override fun onActivityUpdate(activities: List<Activity>) {
        averageSpeed!!.visibility = VISIBLE
        speed!!.visibility = VISIBLE
        duration!!.visibility = VISIBLE
        distance!!.visibility = VISIBLE
        elevation!!.visibility = VISIBLE
        if (activities.isNotEmpty()) {
            val averageSpeedActivity = activities.maxBy { it.averageSpeedInKilometersPerHours() }!!
            val averageSpeedValue = averageSpeedActivity.averageSpeedInKilometersPerHours()
            update(averageSpeed!!, averageSpeedValue, averageSpeedActivity, " Km/h")

            val speedActivity = activities.maxBy { it.maxSpeedInMetersPerSecond }!!
            val speedValue = speedActivity.maxSpeedInKilometersPerHours()
            update(speed!!, speedValue, speedActivity, " Km/h")

            val durationActivity = activities.maxBy { it.durationInSeconds }!!
            val durationValue = durationActivity.durationInSeconds
            duration!!.findViewById<TextView>(R.id.value).text = Measure.timeFragments(durationValue)
            update(duration!!.findViewById<CardView>(R.id.activity), durationActivity)

            val distanceActivity = activities.maxBy { it.distanceInMeters }!!
            val distanceValue = distanceActivity.distanceInKilometers()
            update(distance!!, distanceValue, distanceActivity, " Km")

            val elevationActivity = activities.maxBy { it.elevationInMeters }!!
            val elevationValue = elevationActivity.elevationInMeters
            update(elevation!!, elevationValue, elevationActivity, " m")
        }
    }

    private fun update(
        record: View,
        recordValue: Double,
        activity: Activity,
        unit: String
    ) {
        if (recordValue == 0.0) {
            record.visibility = GONE
        } else {
            record.findViewById<TextView>(R.id.value).text = Measure.of(recordValue, unit, "")
            update(record.findViewById<CardView>(R.id.activity), activity)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()

        averageSpeed = null
        speed = null
        duration = null
        distance = null
        elevation = null
    }
}