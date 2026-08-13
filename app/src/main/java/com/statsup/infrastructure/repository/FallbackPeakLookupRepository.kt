package com.statsup.infrastructure.repository

import com.google.android.gms.maps.model.LatLng
import com.statsup.domain.repository.Peak
import com.statsup.domain.repository.PeakLookupException
import com.statsup.domain.repository.PeakLookupRepository

/**
 * Falls back to [fallback] whenever [primary] can't produce an answer — either because it threw
 * or because it found no peak nearby — so a gap in one source's coverage doesn't sink the whole
 * lookup. [PeakLookupException] only propagates if both sources fail; a confirmed negative from
 * [fallback] after a [primary] failure still counts as "no peak found".
 */
class FallbackPeakLookupRepository(
    private val primary: PeakLookupRepository,
    private val fallback: PeakLookupRepository
) : PeakLookupRepository {

    override suspend fun findNearestPeak(latLng: LatLng, elevationHint: Double): Peak? {
        val primaryResult = try {
            primary.findNearestPeak(latLng, elevationHint)
        } catch (e: PeakLookupException) {
            return try {
                fallback.findNearestPeak(latLng, elevationHint)
            } catch (fallbackError: PeakLookupException) {
                throw PeakLookupException(
                    "Both primary and fallback peak lookups failed: ${e.message} / ${fallbackError.message}",
                    fallbackError
                )
            }
        }
        return primaryResult ?: fallback.findNearestPeak(latLng, elevationHint)
    }
}
