package com.statsup

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_details.*
import org.joda.time.format.DateTimeFormat

class ActivityDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val activity = ActivityRepository.byId(intent.getLongExtra("id", -1))

        activity_title.text = activity.title
        sport_icon.setImageResource(activity.sport.icon)
        sport_name.setText(activity.sport.title)
        activity_date.text = activity.date().toString(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm"))
        duration_value.text = Measure.timeFragments(activity.durationInSeconds)
        distance_value.text = Measure.of(activity.distanceInKilometers(), "Km", "", "- ")
        pace_value.text = Measure.minutesAndSeconds(activity.paceInSecondsPerKilometer(), "/Km")
        elevation_value.text = Measure.of(activity.elevationInMeters, "m", "", "- ")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
