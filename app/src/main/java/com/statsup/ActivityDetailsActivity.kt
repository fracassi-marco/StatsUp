package com.statsup

import android.content.Intent
import android.graphics.Color.*
import android.os.Bundle
import android.widget.GridLayout
import android.widget.GridLayout.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)

        binding.activityTitle.text = activity.title
        binding.sportIcon.setImageResource(activity.sport.icon)
        binding.sportName.setText(activity.sport.title)
        binding.activityDate.text =
            activity.date().toString(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm"))

        if (activity.durationInSeconds > 0) {
            item().apply {
                itemIcon.setImageResource(R.drawable.outline_timer_24)
                itemLabel.setText(R.string.activity_duration)
                itemValue.text = Measure.hms(activity.durationInSeconds)
            }
        }

        if (activity.movingTimeInSeconds > 0) {
            item().apply {
                itemIcon.setImageResource(R.drawable.moving_time)
                itemLabel.setText(R.string.activity_moving_time)
                itemValue.text = Measure.hms(activity.movingTimeInSeconds)
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
                layoutParams = LayoutParams(
                    spec(UNDEFINED, 1f),
                    spec(UNDEFINED, 1f)
                )
            }
        return inflate
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (activity.map.isNullOrBlank()) {
            binding.map.visibility = GONE
            return
        }

        val trip = Trip(activity.map!!)
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = false
        googleMap.uiSettings.isZoomControlsEnabled = false
        googleMap.uiSettings.isZoomGesturesEnabled = false
        googleMap.uiSettings.isRotateGesturesEnabled = false
        googleMap.addPolyline(PolylineOptions().width(8f).color(BLUE).geodesic(true).addAll(trip.steps()))
        googleMap.addCircle(CircleOptions().center(trip.begin()).fillColor(GREEN).strokeColor(GREEN).radius(12.0))
        googleMap.addCircle(CircleOptions().center(trip.end()).fillColor(RED).strokeColor(RED).radius(12.0))
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
