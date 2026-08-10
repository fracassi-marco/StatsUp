package com.statsup.domain.repository

import com.google.android.gms.maps.model.LatLng

data class Peak(val name: String, val latLng: LatLng, val elevation: Double?)

/**
 * Thrown when a peak lookup could not be completed (network error, rate limiting, etc).
 * Callers must not treat this the same as a successful lookup that found no peak nearby.
 */
class PeakLookupException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

interface PeakLookupRepository {
    /**
     * @return the nearest peak, or null if the lookup succeeded but found none nearby.
     * @throws PeakLookupException if the lookup itself could not be completed.
     */
    suspend fun findNearestPeak(latLng: LatLng, elevationHint: Double): Peak?
}
