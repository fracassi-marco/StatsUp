package com.statsup.domain

import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt

/**
 * Estimates the lat/lng of the highest point of a training.
 *
 * The route polyline and the elevation stream come from two independent, unaligned
 * intervals.icu API calls with their own sampling (intervals.icu's combined streams
 * endpoint does not expose lat/lng pairs alongside altitude). Both streams span the
 * same activity duration, so the elevation peak's fractional position is mapped onto
 * the corresponding fractional position along the route.
 */
fun estimateSummitLatLng(trip: Trip?, altitudes: List<Double>): LatLng? {
    val points = trip?.list ?: return null
    if (points.isEmpty() || altitudes.isEmpty()) return null
    val maxIndex = altitudes.indices.maxByOrNull { altitudes[it] } ?: return null
    val fraction = maxIndex.toDouble() / (altitudes.size - 1).coerceAtLeast(1)
    val pointIndex = (fraction * (points.size - 1)).roundToInt().coerceIn(0, points.size - 1)
    return points[pointIndex]
}
