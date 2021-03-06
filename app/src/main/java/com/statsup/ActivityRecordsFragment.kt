package com.statsup

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.statsup.ActivityView.fill
import com.statsup.Content.showActivitiesOrEmptyPage
import kotlinx.android.synthetic.main.activity_records_fragment.view.*
import kotlinx.android.synthetic.main.no_activities_layout.view.*
import kotlinx.android.synthetic.main.record_item.view.*
import kotlin.Int.Companion.MAX_VALUE

class ActivityRecordsFragment : ActivityFragment() {

    private var averageSpeed: View?  = null
    private var speed: View? = null
    private var pace: View? = null
    private var duration: View? = null
    private var distance: View? = null
    private var elevation: View? = null
    private var elevationHigh: View? = null

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.activity_records_fragment, container, false)
        averageSpeed = view.average_speed.apply {
            image.contentDescription = resources.getString(R.string.records_average_speed)
            image.setImageResource(R.drawable.average_speed)
            label.text = resources.getString(R.string.records_average_speed)
        }
        speed = view.speed.apply {
            image.contentDescription = resources.getString(R.string.records_speed)
            image.setImageResource(R.drawable.speed)
            label.text = resources.getString(R.string.records_speed)
        }
        pace = view.pace.apply {
            image.contentDescription = resources.getString(R.string.records_pace)
            image.setImageResource(R.drawable.pace)
            label.text = resources.getString(R.string.records_pace)
        }
        duration = view.duration.apply {
            image.contentDescription = resources.getString(R.string.records_duration)
            image.setImageResource(R.drawable.duration)
            label.text = resources.getString(R.string.records_duration)
        }
        distance = view.distance.apply {
            image.contentDescription = resources.getString(R.string.records_distance)
            image.setImageResource(R.drawable.distance)
            label.text = resources.getString(R.string.records_distance)
        }
        elevation = view.total_elevation.apply {
            image.contentDescription = resources.getString(R.string.records_elevation)
            image.setImageResource(R.drawable.elevation)
            label.text = resources.getString(R.string.records_elevation)
        }
        elevationHigh = view.elevation_high.apply {
            image.contentDescription = resources.getString(R.string.records_elevation_high)
            image.setImageResource(R.drawable.elevation_high)
            label.text = resources.getString(R.string.records_elevation_high)
        }

        onFilterChange()

        showActivitiesOrEmptyPage(view.no_activities_layout, view.records)

        return view
    }

    override fun onFilterChange() {
        averageSpeed!!.visibility = VISIBLE
        speed!!.visibility = VISIBLE
        pace!!.visibility = VISIBLE
        duration!!.visibility = VISIBLE
        distance!!.visibility = VISIBLE
        elevation!!.visibility = VISIBLE
        elevationHigh!!.visibility = VISIBLE

        val activities = ActivityRepository.filterBySelectedSport()
        if (activities.isNotEmpty()) {
            val averageSpeedActivity = activities.maxByOrNull { it.averageSpeedInKilometersPerHours() }!!
            val averageSpeedValue = averageSpeedActivity.averageSpeedInKilometersPerHours()
            update(averageSpeed!!, averageSpeedValue, averageSpeedActivity, " Km/h")

            val speedActivity = activities.maxByOrNull { it.maxSpeedInMetersPerSecond }!!
            val speedValue = speedActivity.maxSpeedInKilometersPerHours()
            update(speed!!, speedValue, speedActivity, " Km/h")

            val paceActivity = activities.minByOrNull { it.paceInSecondsPerKilometer() }!!
            val paceValue = paceActivity.paceInSecondsPerKilometer()
            if(paceValue == MAX_VALUE) {
                pace!!.visibility = GONE
            }
            else {
                pace!!.value.text = Measure.minutesAndSeconds(paceValue, "/Km")
                fill(pace!!.activity, paceActivity)
            }

            val durationActivity = activities.maxByOrNull { it.durationInSeconds }!!
            val durationValue = durationActivity.durationInSeconds
            if(durationValue == 0) {
                duration!!.visibility = GONE
            }
            else {
                duration!!.value.text = Measure.timeFragments(durationValue)
                fill(duration!!.activity, durationActivity)
            }

            val distanceActivity = activities.maxByOrNull { it.distanceInMeters }!!
            val distanceValue = distanceActivity.distanceInKilometers()
            update(distance!!, distanceValue, distanceActivity, " Km")

            val elevationActivity = activities.maxByOrNull { it.elevationInMeters }!!
            val elevationValue = elevationActivity.elevationInMeters
            update(elevation!!, elevationValue, elevationActivity, " m")

            val elevationHighActivity = activities.maxByOrNull { it.elevHighInMeters }!!
            val elevationHighValue = elevationHighActivity.elevHighInMeters
            update(elevationHigh!!, elevationHighValue, elevationHighActivity, " m")
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
            record.value.text = Measure.of(recordValue, unit, "", "- ")
            fill(record.activity, activity)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        averageSpeed = null
        speed = null
        pace = null
        duration = null
        distance = null
        elevation = null
        elevationHigh = null
    }
}