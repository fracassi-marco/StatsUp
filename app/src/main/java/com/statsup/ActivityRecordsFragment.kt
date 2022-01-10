package com.statsup

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.statsup.ActivityView.fill
import com.statsup.Content.showActivitiesOrEmptyPage
import com.statsup.databinding.ActivityRecordsFragmentBinding
import com.statsup.databinding.RecordItemBinding
import kotlin.Int.Companion.MAX_VALUE

class ActivityRecordsFragment : ActivityFragment() {

    private var averageSpeed: RecordItemBinding?  = null
    private var speed: RecordItemBinding? = null
    private var pace: RecordItemBinding? = null
    private var duration: RecordItemBinding? = null
    private var distance: RecordItemBinding? = null
    private var elevation: RecordItemBinding? = null
    private var elevationHigh: RecordItemBinding? = null
    private var _binding: ActivityRecordsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(inflater: LayoutInflater, container: ViewGroup?): View {
        _binding = ActivityRecordsFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.averageSpeed
        val averageSpeed1: RecordItemBinding = binding.averageSpeed
        averageSpeed = averageSpeed1.apply {
            image.contentDescription = resources.getString(R.string.records_average_speed)
            image.setImageResource(R.drawable.average_speed)
            label.text = resources.getString(R.string.records_average_speed)
        }
        speed = binding.speed.apply {
            image.contentDescription = resources.getString(R.string.records_speed)
            image.setImageResource(R.drawable.speed)
            label.text = resources.getString(R.string.records_speed)
        }
        pace = binding.pace.apply {
            image.contentDescription = resources.getString(R.string.records_pace)
            image.setImageResource(R.drawable.pace)
            label.text = resources.getString(R.string.records_pace)
        }
        duration = binding.duration.apply {
            image.contentDescription = resources.getString(R.string.records_duration)
            image.setImageResource(R.drawable.duration)
            label.text = resources.getString(R.string.records_duration)
        }
        distance = binding.distance.apply {
            image.contentDescription = resources.getString(R.string.records_distance)
            image.setImageResource(R.drawable.distance)
            label.text = resources.getString(R.string.records_distance)
        }
        elevation = binding.totalElevation.apply {
            image.contentDescription = resources.getString(R.string.records_elevation)
            image.setImageResource(R.drawable.elevation)
            label.text = resources.getString(R.string.records_elevation)
        }
        elevationHigh = binding.elevationHigh.apply {
            image.contentDescription = resources.getString(R.string.records_elevation_high)
            image.setImageResource(R.drawable.elevation_high)
            label.text = resources.getString(R.string.records_elevation_high)
        }

        onFilterChange()

        showActivitiesOrEmptyPage(binding.noActivitiesLayout, binding.records)

        return view
    }

    override fun onFilterChange() {
        averageSpeed!!.root.visibility = VISIBLE
        speed!!.root.visibility = VISIBLE
        pace!!.root.visibility = VISIBLE
        duration!!.root.visibility = VISIBLE
        distance!!.root.visibility = VISIBLE
        elevation!!.root.visibility = VISIBLE
        elevationHigh!!.root.visibility = VISIBLE

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
                pace!!.root.visibility = GONE
            }
            else {
                pace!!.value.text = Measure.minutesAndSeconds(paceValue, "/Km")
                fill(pace!!.activity, paceActivity)
            }

            val durationActivity = activities.maxByOrNull { it.durationInSeconds }!!
            val durationValue = durationActivity.durationInSeconds
            if(durationValue == 0) {
                duration!!.root.visibility = GONE
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
        record: RecordItemBinding,
        recordValue: Double,
        activity: Activity,
        unit: String
    ) {
        if (recordValue == 0.0) {
            record.root.visibility = GONE
        } else {
            record.value.text = Measure.of(recordValue, unit, "", "- ")
            fill(record.activity, activity)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        averageSpeed = null
        speed = null
        pace = null
        duration = null
        distance = null
        elevation = null
        elevationHigh = null
    }
}