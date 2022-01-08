package com.statsup

import android.graphics.Color.argb
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import kotlin.random.Random.Default.nextInt


class AllTimesMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var activities: List<Activity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_activity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        activities = ActivityRepository.all()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val boundBuilder: LatLngBounds.Builder = LatLngBounds.Builder()
        activities
            .filter { activity -> activity.map != null }
            .forEach { activity ->
                val trip = Trip(activity.map!!)
                googleMap.addPolyline(PolylineOptions().width(7f).color(randomColor()).geodesic(true).addAll(trip.steps()))
                trip.steps().forEach {
                    boundBuilder.include(it)
                }
        }

        googleMap.setOnMapLoadedCallback {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 30))
        }
    }

    private fun randomColor() = argb(255, nextInt(256), nextInt(256), nextInt(256))

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}