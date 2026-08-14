package com.statsup.infrastructure.repository

import com.google.android.gms.maps.model.LatLng
import com.statsup.domain.repository.Peak
import kotlin.math.abs
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

// Meters of "equivalent distance" penalty per meter of elevation mismatch. The GPS-estimated
// summit position is a rough interpolation between two unaligned streams (see
// estimateSummitLatLng) and can easily be off by a few hundred meters, while the elevation
// stream's max altitude is comparatively reliable. Weighting elevation this heavily means a
// candidate a bit further away but at the right altitude beats a closer one at the wrong
// altitude (e.g. a false summit or a shoulder of the real peak).
private const val ELEVATION_MISMATCH_WEIGHT = 5.0

/**
 * Picks the best-matching peak among [candidates] for an estimated summit at [latLng] with an
 * observed [elevationHint] (the training's max recorded altitude), combining proximity and
 * elevation agreement instead of distance alone. Candidates with no known elevation are only
 * scored on distance.
 */
internal fun choosePeak(candidates: List<Peak>, latLng: LatLng, elevationHint: Double): Peak? =
    candidates.minByOrNull { peakScore(it, latLng, elevationHint) }

private fun peakScore(peak: Peak, latLng: LatLng, elevationHint: Double): Double {
    val distancePenalty = haversineMeters(latLng, peak.latLng)
    val elevationPenalty = peak.elevation?.let { abs(it - elevationHint) * ELEVATION_MISMATCH_WEIGHT } ?: 0.0
    return distancePenalty + elevationPenalty
}
