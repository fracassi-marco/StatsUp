package com.statsup

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil


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
        val list = PolyUtil.decode(activity.map!!)
        googleMap
            .addPolyline(
                PolylineOptions()
                    .width(7.toFloat())
                    .color(Color.BLUE)
                    .geodesic(true)
                    .addAll(list)
            )

        val builder: LatLngBounds.Builder = LatLngBounds.Builder()
        list.forEach {
            builder.include(it)
        }

        googleMap.setOnMapLoadedCallback {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 20))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}