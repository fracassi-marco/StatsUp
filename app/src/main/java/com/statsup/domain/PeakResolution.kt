package com.statsup.domain

import android.util.Log
import com.statsup.domain.repository.PeakLookupException
import com.statsup.domain.repository.PeakLookupRepository

private const val TAG = "PeakResolution"

const val MIN_PEAK_ELEVATION_METERS = 1200.0

/**
 * Resolves [Training.peakName]/[Training.peakElevation].
 *
 * `peakName == null` means "not yet resolved, eligible for a future retry"; `peakName == ""`
 * means "resolved successfully, confirmed no peak nearby" — a final, legitimate negative. A
 * transient lookup failure (network error, rate limiting, missing elevation data) must never
 * collapse into `""`, or the training silently and permanently drops out of [Trainings.topPeaks]
 * with no way to recover except a manual per-activity reimport. On failure, fall back to
 * [existing]'s value (or stay unresolved if there is none) instead.
 */
suspend fun resolvePeak(
    training: Training,
    elevPoints: List<Double>?,
    peakLookupRepository: PeakLookupRepository?,
    existing: Training? = null
): Training {
    if (peakLookupRepository == null || training.peakName != null) return training
    if (training.elevHigh < MIN_PEAK_ELEVATION_METERS) {
        Log.d(TAG, "training=${training.id} skip: elevHigh=${training.elevHigh} < ${MIN_PEAK_ELEVATION_METERS}m threshold")
        return training.copy(peakName = "", peakElevation = null)
    }
    if (elevPoints.isNullOrEmpty()) {
        Log.d(TAG, "training=${training.id} skip: no elevation stream, keeping existing peakName=${existing?.peakName}")
        return training.copy(peakName = existing?.peakName, peakElevation = existing?.peakElevation)
    }
    val summit = estimateSummitLatLng(training.trip, elevPoints)
        ?: run {
            Log.d(TAG, "training=${training.id} skip: could not estimate summit lat/lng (trip=${training.trip != null}, elevPoints=${elevPoints.size}), keeping existing peakName=${existing?.peakName}")
            return training.copy(peakName = existing?.peakName, peakElevation = existing?.peakElevation)
        }
    Log.d(TAG, "training=${training.id} lookup: elevHigh=${training.elevHigh} summit=(${summit.latitude},${summit.longitude})")
    return try {
        val peak = peakLookupRepository.findNearestPeak(summit, elevPoints.max())
        Log.d(TAG, "training=${training.id} result: ${if (peak != null) "peak=${peak.name} ele=${peak.elevation}" else "no peak found nearby"}")
        training.copy(peakName = peak?.name ?: "", peakElevation = peak?.elevation)
    } catch (e: PeakLookupException) {
        Log.w(TAG, "training=${training.id} lookup failed, keeping existing peakName=${existing?.peakName}", e)
        training.copy(peakName = existing?.peakName, peakElevation = existing?.peakElevation)
    }
}
