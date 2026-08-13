package com.statsup.infrastructure.repository

import com.google.android.gms.maps.model.LatLng
import com.statsup.domain.repository.Peak
import com.statsup.domain.repository.PeakLookupException
import com.statsup.domain.repository.PeakLookupRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FallbackPeakLookupRepositoryTest {

    private val latLng = LatLng(45.9, 7.6)
    private val elevationHint = 3500.0
    private val peak = Peak(name = "Monte Rosa", latLng = latLng, elevation = 4634.0)

    @Test
    fun `returns primary's result without touching fallback when primary finds a peak`() = runTest {
        val primary: PeakLookupRepository = mock()
        val fallback: PeakLookupRepository = mock()
        whenever(primary.findNearestPeak(any(), any())).thenReturn(peak)

        val result = FallbackPeakLookupRepository(primary, fallback).findNearestPeak(latLng, elevationHint)

        assertEquals(peak, result)
        verify(fallback, never()).findNearestPeak(any(), any())
    }

    @Test
    fun `falls back when primary finds nothing nearby`() = runTest {
        val primary: PeakLookupRepository = mock()
        val fallback: PeakLookupRepository = mock()
        whenever(primary.findNearestPeak(any(), any())).thenReturn(null)
        whenever(fallback.findNearestPeak(any(), any())).thenReturn(peak)

        val result = FallbackPeakLookupRepository(primary, fallback).findNearestPeak(latLng, elevationHint)

        assertEquals(peak, result)
    }

    @Test
    fun `falls back when primary throws`() = runTest {
        val primary: PeakLookupRepository = mock()
        val fallback: PeakLookupRepository = mock()
        whenever(primary.findNearestPeak(any(), any())).thenThrow(PeakLookupException("Overpass down"))
        whenever(fallback.findNearestPeak(any(), any())).thenReturn(peak)

        val result = FallbackPeakLookupRepository(primary, fallback).findNearestPeak(latLng, elevationHint)

        assertEquals(peak, result)
    }

    @Test
    fun `returns null when primary finds nothing and fallback also finds nothing`() = runTest {
        val primary: PeakLookupRepository = mock()
        val fallback: PeakLookupRepository = mock()
        whenever(primary.findNearestPeak(any(), any())).thenReturn(null)
        whenever(fallback.findNearestPeak(any(), any())).thenReturn(null)

        val result = FallbackPeakLookupRepository(primary, fallback).findNearestPeak(latLng, elevationHint)

        assertNull(result)
    }

    @Test(expected = PeakLookupException::class)
    fun `propagates an exception when both primary and fallback fail`() = runTest {
        val primary: PeakLookupRepository = mock()
        val fallback: PeakLookupRepository = mock()
        whenever(primary.findNearestPeak(any(), any())).thenThrow(PeakLookupException("Overpass down"))
        whenever(fallback.findNearestPeak(any(), any())).thenThrow(PeakLookupException("GeoNames down"))

        FallbackPeakLookupRepository(primary, fallback).findNearestPeak(latLng, elevationHint)
    }

    @Test(expected = PeakLookupException::class)
    fun `propagates the fallback's exception when primary found nothing but fallback fails`() = runTest {
        val primary: PeakLookupRepository = mock()
        val fallback: PeakLookupRepository = mock()
        whenever(primary.findNearestPeak(any(), any())).thenReturn(null)
        whenever(fallback.findNearestPeak(any(), any())).thenThrow(PeakLookupException("GeoNames down"))

        FallbackPeakLookupRepository(primary, fallback).findNearestPeak(latLng, elevationHint)
    }
}
