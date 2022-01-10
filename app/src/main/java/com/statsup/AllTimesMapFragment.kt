package com.statsup

import android.graphics.Color.BLUE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.statsup.Content.showActivitiesOrEmptyPage
import com.statsup.databinding.MapActivityBinding


class AllTimesMapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: MapActivityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = MapActivityBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        showActivitiesOrEmptyPage(binding.noActivitiesLayout, binding.mapLayout)

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val boundBuilder: LatLngBounds.Builder = LatLngBounds.Builder()
        ActivityRepository.all()
            .filter { activity -> activity.map != null }
            .forEach { activity ->
                val trip = Trip(activity.map!!)
                googleMap.addPolyline(PolylineOptions().width(7f).color(BLUE).geodesic(true).addAll(trip.steps()))
                trip.steps().forEach {
                    boundBuilder.include(it)
                }
        }

        googleMap.setOnMapLoadedCallback {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 30))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}