package com.statsup.domain

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

class PeakResolutionTest {

    private val summitRoute = listOf(LatLng(45.0, 7.0), LatLng(45.9, 7.6), LatLng(46.0, 8.0))
    private val summitPolyline = com.google.maps.android.PolyUtil.encode(summitRoute)
    private val elevPoints = listOf(2000.0, 3500.0, 2500.0)

    private fun makeTraining(elevHigh: Double, peakName: String? = null, map: Route? = null) = Training(
        id = "1",
        name = "Morning Run",
        distance = 10000.0,
        movingTime = 3600,
        elapsedTime = 3700,
        totalElevationGain = 50.0,
        startDate = "2024-01-15T08:00:00Z",
        maxSpeed = 4.0,
        averageCadence = 0.0,
        averageWatts = 0.0,
        weightedAverageWatts = 0,
        kilojoules = 0.0,
        deviceWatts = false,
        maxHeartrate = 0.0,
        elevHigh = elevHigh,
        elevLow = 0.0,
        uploadId = 0L,
        sufferScore = null,
        peakName = peakName,
        map = map
    )

    @Test
    fun `returns training unchanged when no repository is configured`() = runTest {
        val training = makeTraining(elevHigh = 3500.0)

        val result = resolvePeak(training, elevPoints, null)

        assertEquals(training, result)
    }

    @Test
    fun `returns training unchanged when already resolved`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val training = makeTraining(elevHigh = 3500.0, peakName = "Monte Rosa")

        val result = resolvePeak(training, elevPoints, peakLookupRepository)

        assertEquals("Monte Rosa", result.peakName)
        verify(peakLookupRepository, never()).findNearestPeak(any(), any())
    }

    @Test
    fun `marks elevation below threshold as a confirmed negative`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val training = makeTraining(elevHigh = 500.0)

        val result = resolvePeak(training, elevPoints, peakLookupRepository)

        assertEquals("", result.peakName)
        assertNull(result.peakElevation)
        verify(peakLookupRepository, never()).findNearestPeak(any(), any())
    }

    @Test
    fun `stays unresolved when elevation points are missing and there is no existing fallback`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val training = makeTraining(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))

        val result = resolvePeak(training, null, peakLookupRepository)

        assertNull(result.peakName)
    }

    @Test
    fun `falls back to the existing peak when elevation points are missing`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val training = makeTraining(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        val existing = makeTraining(elevHigh = 3500.0, peakName = "Monte Rosa").copy(peakElevation = 4634.0)

        val result = resolvePeak(training, null, peakLookupRepository, existing)

        assertEquals("Monte Rosa", result.peakName)
        assertEquals(4634.0, result.peakElevation)
    }

    @Test
    fun `stays unresolved when the lookup throws and there is no existing fallback`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val training = makeTraining(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        whenever(peakLookupRepository.findNearestPeak(any(), any()))
            .thenThrow(PeakLookupException("Overpass unavailable"))

        val result = resolvePeak(training, elevPoints, peakLookupRepository)

        assertNull(result.peakName)
    }

    @Test
    fun `falls back to the existing peak when the lookup throws`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val training = makeTraining(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        val existing = makeTraining(elevHigh = 3500.0, peakName = "Monte Rosa").copy(peakElevation = 4634.0)
        whenever(peakLookupRepository.findNearestPeak(any(), any()))
            .thenThrow(PeakLookupException("Overpass unavailable"))

        val result = resolvePeak(training, elevPoints, peakLookupRepository, existing)

        assertEquals("Monte Rosa", result.peakName)
        assertEquals(4634.0, result.peakElevation)
    }

    @Test
    fun `persists a blank sentinel when the lookup succeeds but finds nothing nearby`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val training = makeTraining(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        whenever(peakLookupRepository.findNearestPeak(any(), any())).thenReturn(null)

        val result = resolvePeak(training, elevPoints, peakLookupRepository)

        assertEquals("", result.peakName)
        assertNull(result.peakElevation)
    }

    @Test
    fun `resolves the peak when the lookup finds one nearby`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val training = makeTraining(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        val peak = Peak(name = "Monte Rosa", latLng = LatLng(45.9, 7.6), elevation = 4634.0)
        whenever(peakLookupRepository.findNearestPeak(any(), any())).thenReturn(peak)

        val result = resolvePeak(training, elevPoints, peakLookupRepository)

        assertEquals("Monte Rosa", result.peakName)
        assertEquals(4634.0, result.peakElevation)
    }
}
