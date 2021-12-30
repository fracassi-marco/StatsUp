package com.statsup

import android.graphics.Color.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.PolylineOptions


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_activity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        activity = ActivityRepository.byId(intent.getLongExtra("id", -1))

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val trip = Trip(activity.map!!)
        googleMap.addCircle(CircleOptions().center(trip.begin()).fillColor(GREEN).strokeColor(GREEN).radius(20.0))
        googleMap.addCircle(CircleOptions().center(trip.end()).fillColor(RED).strokeColor(RED).radius(20.0))
        googleMap.addPolyline(PolylineOptions().width(7f).color(BLUE).geodesic(true).addAll(trip.steps()))

        googleMap.setOnMapLoadedCallback {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(trip.boundaries(), 30))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}