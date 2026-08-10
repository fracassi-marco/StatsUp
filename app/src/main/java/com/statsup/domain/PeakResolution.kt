package com.statsup.domain

import com.statsup.domain.repository.PeakLookupException
import com.statsup.domain.repository.PeakLookupRepository

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
        return training.copy(peakName = "", peakElevation = null)
    }
    if (elevPoints.isNullOrEmpty()) {
        return training.copy(peakName = existing?.peakName, peakElevation = existing?.peakElevation)
    }
    val summit = estimateSummitLatLng(training.trip, elevPoints)
        ?: return training.copy(peakName = existing?.peakName, peakElevation = existing?.peakElevation)
    return try {
        val peak = peakLookupRepository.findNearestPeak(summit, elevPoints.max())
        training.copy(peakName = peak?.name ?: "", peakElevation = peak?.elevation)
    } catch (e: PeakLookupException) {
        training.copy(peakName = existing?.peakName, peakElevation = existing?.peakElevation)
    }
}
