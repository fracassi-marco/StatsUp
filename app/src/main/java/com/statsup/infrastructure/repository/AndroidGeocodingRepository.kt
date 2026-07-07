package com.statsup.infrastructure.repository

import android.content.Context
import android.location.Geocoder
import com.statsup.domain.repository.GeocodingRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidGeocodingRepository(private val context: Context) : GeocodingRepository {

    override suspend fun reverseGeocode(lat: Double, lng: Double): String? {
        if (!Geocoder.isPresent()) return null
        return try {
            suspendCancellableCoroutine { continuation ->
                Geocoder(context).getFromLocation(lat, lng, 1) { addresses ->
                    val address = addresses.firstOrNull()
                    val label = address?.let {
                        listOfNotNull(
                            it.countryName,
                            it.adminArea,
                            it.subAdminArea,
                            it.locality
                        ).joinToString(" → ")
                    }
                    continuation.resume(label)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
