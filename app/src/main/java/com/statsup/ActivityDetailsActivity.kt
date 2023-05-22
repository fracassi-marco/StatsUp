package com.statsup

import android.content.Intent
import android.graphics.Color.*
import android.os.Bundle
import android.widget.GridLayout.GONE
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.statsup.databinding.ActivityDetailsBinding
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

        if(activity.map.isNullOrBlank()) {
            binding.map.visibility = GONE
        } else {
            binding.bg.visibility = GONE
            (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)
        }

        binding.activityTitle.text = activity.title
        //binding.sportName.setText(activity.sport.title)
        binding.activityDate.text =
            activity.date().toString(DateTimeFormat.forPattern("dd/MM/yyyy HH:mm"))

        if (activity.durationInSeconds > 0) {
            binding.durationValue.text = Measure.hms(activity.durationInSeconds)
        } else {
            binding.durationValue.visibility = GONE
            binding.label1.visibility = GONE
        }
        if (activity.distanceInMeters > 0) {
            binding.distanceValue.text = Measure.of(activity.distanceInKilometers(), "Km", "", "- ")
        } else {
            binding.distanceValue.visibility = GONE
            binding.label2.visibility = GONE
        }
        if (activity.paceInSecondsPerKilometer() != Int.MAX_VALUE) {
            binding.paceValue.text = Measure.minutesAndSeconds(activity.paceInSecondsPerKilometer(), "/Km")
        } else {
            binding.paceValue.visibility = GONE
            binding.label3.visibility = GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
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
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(trip.boundaries(), 400))
        }
    }
}
