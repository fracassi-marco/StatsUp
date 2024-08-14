package com.statsup.domain

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil

class Trip(private val map: String) {

    val list: List<LatLng> by lazy {
        PolyUtil.decode(map)
    }

    fun begin() = list.first()

    fun end() = list.last()

    fun steps() = list

    val zoomForBoundaries: Float by lazy {
        val distance = distance(
            boundaries.northeast.latitude,
            boundaries.northeast.longitude,
            boundaries.southwest.latitude,
            boundaries.southwest.longitude
        )
        //Log.i("RANGE", "distance -> $distance, b: $boundaries")
        when(distance) {
            in 0.0..<0.3 -> 17f
            in 0.3..<1.0 -> 16f
            in 1.0..1.5 -> 15f
            in 1.5..1.6 -> 14f
            in 1.6..2.0 -> 13f
            else -> 12f
        }
    }

    val boundaries: LatLngBounds by lazy {
        try {
            val builder = LatLngBounds.Builder()
            list.forEach {
                builder.include(it)
            }
            builder.build()
        }catch (t: Throwable) {
            Log.e("StatsUp", t.message!!, t)
            LatLngBounds.Builder().build()
        }
    }
}