package com.statsup

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.joda.time.format.DateTimeFormat

class ActivityDetailsActivity : AppCompatActivity() {

    private val job = Job()
    val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val activity = ActivityRepository.byId(intent.getLongExtra("id", -1))

        scope.launch {
            val map = MapRepository.ofActivity(applicationContext, activity)

            if(map != null) {
                activity_map.visibility = VISIBLE
                activity_map.setImageBitmap(map)
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
