package com.statsup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.View.VISIBLE
import android.widget.GridLayout
import android.widget.GridLayout.UNDEFINED
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_details_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.joda.time.format.DateTimeFormat

class ActivityDetailsActivity : AppCompatActivity() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val activity = ActivityRepository.byId(intent.getLongExtra("id", -1))

        scope.launch {
            val map = MapRepository.ofActivity(applicationContext, activity)

            if (map != null) {
                activity_map.visibility = VISIBLE
                activity_map.setImageBitmap(map)
            }
        }
        activity_title.text = activity.title
        sport_icon.setImageResource(activity.sport.icon)
        sport_name.setText(activity.sport.title)
        activity_date.text = activity.date().toString(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm"))

        if (activity.durationInSeconds > 0) {
            activity_details_a.addView(item().apply {
                item_icon.setImageResource(R.drawable.outline_timer_24)
                item_label.setText(R.string.activity_duration)
                item_value.text = Measure.timeFragments(activity.durationInSeconds)
            })
        }

        if (activity.movingTimeInSeconds > 0) {
            activity_details_a.addView(item().apply {
                item_icon.setImageResource(R.drawable.moving_time)
                item_label.setText(R.string.activity_moving_time)
                item_value.text = Measure.timeFragments(activity.movingTimeInSeconds)
            })
        }

        if (activity.paceInSecondsPerKilometer() != Int.MAX_VALUE) {
            activity_details_a.addView(item().apply {
                item_icon.setImageResource(R.drawable.outline_restore_24)
                item_label.setText(R.string.activity_pace)
                item_value.text =
                    Measure.minutesAndSeconds(activity.paceInSecondsPerKilometer(), "/Km")
            })
        }

        if (activity.movingPaceInSecondsPerKilometer() != Int.MAX_VALUE) {
            activity_details_a.addView(item().apply {
                item_icon.setImageResource(R.drawable.moving_pace)
                item_label.setText(R.string.activity_moving_pace)
                item_value.text =
                    Measure.minutesAndSeconds(activity.movingPaceInSecondsPerKilometer(), "/Km")
            })
        }

        if (activity.distanceInMeters > 0) {
            activity_details_a.addView(item().apply {
                item_icon.setImageResource(R.drawable.outline_place_24)
                item_label.setText(R.string.activity_distance)
                item_value.text = Measure.of(activity.distanceInKilometers(), "Km", "", "- ")
            })
        }

        if (activity.elevationInMeters > 0) {
            activity_details_a.addView(item().apply {
                item_icon.setImageResource(R.drawable.outline_trending_up_24)
                item_label.setText(R.string.activity_elevation)
                item_value.text = Measure.of(activity.elevationInMeters, "m", "", "- ")
            })
        }

        if (activity.elevHighInMeters > 0) {
            activity_details_a.addView(item().apply {
                item_icon.setImageResource(R.drawable.elevation_high)
                item_label.setText(R.string.activity_elevation_high)
                item_value.text = Measure.of(activity.elevHighInMeters, "m", "", "- ")
            })
        }

        if (activity.elevLowInMeters > 0) {
            activity_details_a.addView(item().apply {
                item_icon.setImageResource(R.drawable.elevation_low)
                item_label.setText(R.string.activity_elevation_low)
                item_value.text = Measure.of(activity.elevLowInMeters, "m", "", "- ")
            })
        }
    }

    private fun item(): View {
        return layoutInflater.inflate(R.layout.activity_details_item, activity_details_a, false)
            .apply {
                layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(UNDEFINED, 1f),
                    GridLayout.spec(UNDEFINED, 1f)
                )
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
