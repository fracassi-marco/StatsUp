package com.statsup.domain

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.PolyUtil

class Trip(private val map: String) {

    // Cache della lista decodificata - calcolato una sola volta
    private val _list: List<LatLng> by lazy {
        try {
            PolyUtil.decode(map)
        } catch (t: Throwable) {
            Log.e("StatsUp", "Error decoding polyline: ${t.message}", t)
            emptyList()
        }
    }

    val list: List<LatLng> get() = _list

    fun begin() = _list.firstOrNull() ?: LatLng(0.0, 0.0)

    fun end() = _list.lastOrNull() ?: LatLng(0.0, 0.0)

    fun steps() = _list

    /**
     * Restituisce una versione semplificata del percorso per risparmiare memoria.
     * Usa l'algoritmo Douglas-Peucker per ridurre i punti mantenendo la forma.
     * @param tolerance Tolleranza in metri (default 10m)
     * @return Lista semplificata di LatLng
     */
    fun simplifiedSteps(tolerance: Double = 10.0): List<LatLng> {
        if (_list.size <= 2) return _list
        return PolyUtil.simplify(_list, tolerance)
    }

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

    /**
     * Restituisce boundaries con padding percentuale per garantire che
     * il percorso sia sempre completamente visibile con margine uniforme.
     * Google Maps calcolerà automaticamente lo zoom ottimale.
     *
     * @param paddingPercent Percentuale di padding (0.15 = 15% di margine su ogni lato)
     * @return LatLngBounds espanso con padding
     */
    fun getBoundariesWithPadding(paddingPercent: Double = 0.15): LatLngBounds {
        try {
            if (_list.isEmpty()) {
                return LatLngBounds(LatLng(0.0, 0.0), LatLng(0.0, 0.0))
            }

            val latDiff = boundaries.northeast.latitude - boundaries.southwest.latitude
            val lngDiff = boundaries.northeast.longitude - boundaries.southwest.longitude

            val latPadding = latDiff * paddingPercent
            val lngPadding = lngDiff * paddingPercent

            val paddedNorthEast = LatLng(
                boundaries.northeast.latitude + latPadding,
                boundaries.northeast.longitude + lngPadding
            )
            val paddedSouthWest = LatLng(
                boundaries.southwest.latitude - latPadding,
                boundaries.southwest.longitude - lngPadding
            )

            return LatLngBounds(paddedSouthWest, paddedNorthEast)
        } catch (t: Throwable) {
            Log.e("StatsUp", "Error calculating padded boundaries: ${t.message}", t)
            return boundaries
        }
    }
}