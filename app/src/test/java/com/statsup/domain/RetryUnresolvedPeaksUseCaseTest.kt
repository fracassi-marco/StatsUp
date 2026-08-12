package com.statsup.domain

import com.google.android.gms.maps.model.LatLng
import com.statsup.domain.repository.Peak
import com.statsup.domain.repository.PeakLookupException
import com.statsup.domain.repository.PeakLookupRepository
import com.statsup.domain.repository.TrainingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RetryUnresolvedPeaksUseCaseTest {

    private lateinit var trainingRepository: TrainingRepository
    private lateinit var peakLookupRepository: PeakLookupRepository

    private val summitRoute = listOf(LatLng(45.0, 7.0), LatLng(45.9, 7.6), LatLng(46.0, 8.0))
    private val summitPolyline = com.google.maps.android.PolyUtil.encode(summitRoute)
    private val elevPointsJson = "[2000.0,3500.0,2500.0]"

    private fun LatLng.isCloseTo(lat: Double, lng: Double) =
        Math.abs(latitude - lat) < 1e-4 && Math.abs(longitude - lng) < 1e-4

    @Before
    fun setUp() {
        trainingRepository = mock()
        peakLookupRepository = mock()
    }

    @Test
    fun `returns zero and never touches the repository when no peak lookup repository is configured`() = runTest {
        val useCase = RetryUnresolvedPeaksUseCase(trainingRepository, peakLookupRepository = null)

        val result = useCase()

        assertEquals(0, result)
        verify(trainingRepository, never()).unresolvedPeakCandidates()
    }

    @Test
    fun `returns zero when there are no unresolved candidates`() = runTest {
        whenever(trainingRepository.unresolvedPeakCandidates()).thenReturn(emptyList())
        val useCase = RetryUnresolvedPeaksUseCase(trainingRepository, peakLookupRepository)

        val result = useCase()

        assertEquals(0, result)
    }

    @Test
    fun `resolves and persists a previously unresolved peak`() = runTest {
        val candidate = makeUnresolvedTraining(id = "1")
        val peak = Peak(name = "Monte Rosa", latLng = LatLng(45.9, 7.6), elevation = 4634.0)
        whenever(trainingRepository.unresolvedPeakCandidates()).thenReturn(listOf(candidate))
        whenever(peakLookupRepository.findNearestPeak(argThat { isCloseTo(45.9, 7.6) }, eq(3500.0))).thenReturn(peak)
        val useCase = RetryUnresolvedPeaksUseCase(trainingRepository, peakLookupRepository)

        val result = useCase()

        assertEquals(1, result)
        verify(trainingRepository).add(argThat { peakName == "Monte Rosa" && peakElevation == 4634.0 })
    }

    @Test
    fun `persists the confirmed no-peak-nearby sentinel when the retry finds nothing`() = runTest {
        val candidate = makeUnresolvedTraining(id = "1")
        whenever(trainingRepository.unresolvedPeakCandidates()).thenReturn(listOf(candidate))
        whenever(peakLookupRepository.findNearestPeak(any(), any())).thenReturn(null)
        val useCase = RetryUnresolvedPeaksUseCase(trainingRepository, peakLookupRepository)

        val result = useCase()

        assertEquals(1, result)
        verify(trainingRepository).add(argThat { peakName == "" && peakElevation == null })
    }

    @Test
    fun `leaves the candidate unresolved and does not write when the retry fails again`() = runTest {
        val candidate = makeUnresolvedTraining(id = "1")
        whenever(trainingRepository.unresolvedPeakCandidates()).thenReturn(listOf(candidate))
        whenever(peakLookupRepository.findNearestPeak(any(), any()))
            .thenThrow(PeakLookupException("Overpass unavailable"))
        val useCase = RetryUnresolvedPeaksUseCase(trainingRepository, peakLookupRepository)

        val result = useCase()

        assertEquals(0, result)
        verify(trainingRepository, never()).add(any())
    }

    @Test
    fun `leaves the candidate unresolved when its elevation stream was never stored`() = runTest {
        val candidate = makeUnresolvedTraining(id = "1", elevationPointsJson = null)
        whenever(trainingRepository.unresolvedPeakCandidates()).thenReturn(listOf(candidate))
        val useCase = RetryUnresolvedPeaksUseCase(trainingRepository, peakLookupRepository)

        val result = useCase()

        assertEquals(0, result)
        verify(peakLookupRepository, never()).findNearestPeak(any(), any())
        verify(trainingRepository, never()).add(any())
    }

    @Test
    fun `retries every candidate returned by the repository`() = runTest {
        val candidates = listOf(makeUnresolvedTraining(id = "1"), makeUnresolvedTraining(id = "2"))
        whenever(trainingRepository.unresolvedPeakCandidates()).thenReturn(candidates)
        whenever(peakLookupRepository.findNearestPeak(any(), any())).thenReturn(null)
        val useCase = RetryUnresolvedPeaksUseCase(trainingRepository, peakLookupRepository)

        val result = useCase()

        assertEquals(2, result)
        verify(trainingRepository).add(argThat { id == "1" })
        verify(trainingRepository).add(argThat { id == "2" })
    }

    private fun makeUnresolvedTraining(id: String, elevationPointsJson: String? = elevPointsJson) = Training(
        id = id,
        name = "Morning Hike",
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
        elevHigh = 3500.0,
        elevLow = 0.0,
        uploadId = 0L,
        sufferScore = null,
        map = Route(summaryPolyline = summitPolyline),
        elevationPointsJson = elevationPointsJson,
        peakName = null
    )
}
