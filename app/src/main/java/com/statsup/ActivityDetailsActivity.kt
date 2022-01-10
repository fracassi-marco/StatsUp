package com.statsup

import android.content.Intent
import android.graphics.Color.*
import android.os.Bundle
import android.widget.GridLayout
import android.widget.GridLayout.GONE
import android.widget.GridLayout.UNDEFINED
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.statsup.databinding.ActivityDetailsBinding
import com.statsup.databinding.ActivityDetailsItemBinding
import org.joda.time.format.DateTimeFormat


class ActivityDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var activity: Activity
    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        activity = ActivityRepository.byId(intent.getLongExtra("id", -1))

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.activityTitle.text = activity.title
        binding.sportIcon.setImageResource(activity.sport.icon)
        binding.sportName.setText(activity.sport.title)
        binding.activityDate.text = activity.date().toString(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm"))

        if (activity.durationInSeconds > 0) {
            item().apply {
                itemIcon.setImageResource(R.drawable.outline_timer_24)
                itemLabel.setText(R.string.activity_duration)
                itemValue.text = Measure.timeFragments(activity.durationInSeconds)
            }
        }

        if (activity.movingTimeInSeconds > 0) {
            item().apply {
                itemIcon.setImageResource(R.drawable.moving_time)
                itemLabel.setText(R.string.activity_moving_time)
                itemValue.text = Measure.timeFragments(activity.movingTimeInSeconds)
            }
        }

        if (activity.paceInSecondsPerKilometer() != Int.MAX_VALUE) {
            item().apply {
                itemIcon.setImageResource(R.drawable.outline_restore_24)
                itemLabel.setText(R.string.activity_pace)
                itemValue.text =
                    Measure.minutesAndSeconds(activity.paceInSecondsPerKilometer(), "/Km")
            }
        }

        if (activity.movingPaceInSecondsPerKilometer() != Int.MAX_VALUE) {
            item().apply {
                itemIcon.setImageResource(R.drawable.moving_pace)
                itemLabel.setText(R.string.activity_moving_pace)
                itemValue.text =
                    Measure.minutesAndSeconds(activity.movingPaceInSecondsPerKilometer(), "/Km")
            }
        }

        if (activity.distanceInMeters > 0) {
            item().apply {
                itemIcon.setImageResource(R.drawable.outline_place_24)
                itemLabel.setText(R.string.activity_distance)
                itemValue.text = Measure.of(activity.distanceInKilometers(), "Km", "", "- ")
            }
        }

        if (activity.elevationInMeters > 0) {
            item().apply {
                itemIcon.setImageResource(R.drawable.outline_trending_up_24)
                itemLabel.setText(R.string.activity_elevation)
                itemValue.text = Measure.of(activity.elevationInMeters, "m", "", "- ")
            }
        }

        if (activity.elevHighInMeters > 0) {
            item().apply {
                itemIcon.setImageResource(R.drawable.elevation_high)
                itemLabel.setText(R.string.activity_elevation_high)
                itemValue.text = Measure.of(activity.elevHighInMeters, "m", "", "- ")
            }
        }

        if (activity.elevLowInMeters > 0) {
            item().apply {
                itemIcon.setImageResource(R.drawable.elevation_low)
                itemLabel.setText(R.string.activity_elevation_low)
                itemValue.text = Measure.of(activity.elevLowInMeters, "m", "", "- ")
            }
        }
    }

    private fun item(): ActivityDetailsItemBinding {
        val inflate = ActivityDetailsItemBinding.inflate(layoutInflater, binding.activityDetailsA, true)
        inflate.root.apply {
                layoutParams = GridLayout.LayoutParams(
                    GridLayout.spec(UNDEFINED, 1f),
                    GridLayout.spec(UNDEFINED, 1f)
                )
            }
        return inflate
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (activity.map == null) {
            val fragment: SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            fragment.view!!.visibility = GONE
            return
        }

        val trip = Trip(activity.map!!)
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.addCircle(CircleOptions().center(trip.begin()).fillColor(GREEN).strokeColor(GREEN).radius(20.0))
        googleMap.addCircle(CircleOptions().center(trip.end()).fillColor(RED).strokeColor(RED).radius(20.0))
        googleMap.addPolyline(PolylineOptions().width(5f).color(BLUE).geodesic(true).addAll(trip.steps()))
        googleMap.setOnMapClickListener {
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("id", activity.id)
            }
            startActivity(intent)
        }

        googleMap.setOnMapLoadedCallback {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(trip.boundaries(), 30))
        }
    }
}
