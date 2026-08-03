package com.statsup.domain.repository

import com.google.android.gms.maps.model.LatLng

data class Peak(val name: String, val latLng: LatLng, val elevation: Double?)

interface PeakLookupRepository {
    suspend fun findNearestPeak(latLng: LatLng, elevationHint: Double): Peak?
}
