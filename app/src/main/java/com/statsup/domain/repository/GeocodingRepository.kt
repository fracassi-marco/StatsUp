package com.statsup.domain.repository

interface GeocodingRepository {
    suspend fun reverseGeocode(lat: Double, lng: Double): String?
}
