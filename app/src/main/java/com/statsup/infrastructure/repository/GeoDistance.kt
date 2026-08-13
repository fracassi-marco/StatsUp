package com.statsup.infrastructure.repository

import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal fun haversineMeters(a: LatLng, b: LatLng): Double {
    val earthRadiusMeters = 6371000.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLng = Math.toRadians(b.longitude - a.longitude)
    val sinDLat = sin(dLat / 2)
    val sinDLng = sin(dLng / 2)
    val h = sinDLat * sinDLat +
        cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sinDLng * sinDLng
    return 2 * earthRadiusMeters * atan2(sqrt(h), sqrt(1 - h))
}
