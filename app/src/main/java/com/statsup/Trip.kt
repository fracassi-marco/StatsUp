package com.statsup

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

    fun boundaries(): LatLngBounds {
        val builder: LatLngBounds.Builder = LatLngBounds.Builder()
        list.forEach {
            builder.include(it)
        }
        return builder.build()
    }
}
