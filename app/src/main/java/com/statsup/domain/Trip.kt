package com.statsup.domain

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil

class Trip(private val map: String) {

    // Cache della lista decodificata - calcolato una sola volta
    private val _list: List<LatLng> by lazy {
        PolyUtil.decode(map)
    }

    val list: List<LatLng> get() = _list

    fun begin() = _list.firstOrNull() ?: LatLng(0.0, 0.0)

    fun end() = _list.lastOrNull() ?: LatLng(0.0, 0.0)

    fun steps() = _list

    // Cache delle boundaries - calcolato prima dello zoom
    val boundaries: LatLngBounds by lazy {
        try {
            if (_list.isEmpty()) {
                LatLngBounds(LatLng(0.0, 0.0), LatLng(0.0, 0.0))
            } else {
                val builder = LatLngBounds.Builder()
                _list.forEach { builder.include(it) }
                builder.build()
            }
        } catch (t: Throwable) {
            Log.e("StatsUp", "Error calculating boundaries: ${t.message}", t)
            LatLngBounds(LatLng(0.0, 0.0), LatLng(0.0, 0.0))
        }
    }

    val zoomForBoundaries: Float by lazy {
        try {
            val distance = distance(
                boundaries.northeast.latitude,
                boundaries.northeast.longitude,
                boundaries.southwest.latitude,
                boundaries.southwest.longitude
            )
            when (distance) {
                in 0.0..<0.3 -> 17f
                in 0.3..<1.0 -> 16f
                in 1.0..1.5 -> 15f
                in 1.5..1.6 -> 14f
                in 1.6..2.0 -> 13f
                else -> 12f
            }
        } catch (t: Throwable) {
            Log.e("StatsUp", "Error calculating zoom: ${t.message}", t)
            12f
        }
    }
}