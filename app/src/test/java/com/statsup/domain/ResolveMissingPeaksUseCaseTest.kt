package com.statsup.domain

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
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

class ResolveMissingPeaksUseCaseTest {

    private lateinit var trainingRepository: TrainingRepository
    private lateinit var trainingApi: TrainingApi
    private lateinit var peakLookupRepository: PeakLookupRepository
    private lateinit var useCase: ResolveMissingPeaksUseCase

    private val token = "test-token"
    private val jsonMapper = jsonMapper { addModule(kotlinModule()) }
    private val summitRoute = listOf(LatLng(45.0, 7.0), LatLng(45.9, 7.6), LatLng(46.0, 8.0))
    private val summitPolyline = com.google.maps.android.PolyUtil.encode(summitRoute)
    private val elevPoints = listOf(2000.0, 3500.0, 2500.0)

    @Before
    fun setUp() {
        trainingRepository = mock()
        trainingApi = mock()
        peakLookupRepository = mock()
        useCase = ResolveMissingPeaksUseCase(trainingRepository, trainingApi, peakLookupRepository)
    }

    private fun makeTraining(
        id: String = "1",
        elevHigh: Double = 3500.0,
        peakName: String? = null,
        map: Route? = Route(summaryPolyline = summitPolyline),
        elevationPointsJson: String? = null
    ) = Training(
        id = id,
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
        map = map,
        elevationPointsJson = elevationPointsJson
    )

    @Test
    fun `ignores trainings that are already resolved`() = runTest {
        whenever(trainingRepository.getAllTrainings()).thenReturn(listOf(makeTraining(peakName = "Monte Rosa")))

        val count = useCase(token)

        assertEquals(0, count)
        verify(trainingRepository, never()).add(any())
    }

    @Test
    fun `ignores trainings below the peak elevation threshold`() = runTest {
        whenever(trainingRepository.getAllTrainings()).thenReturn(listOf(makeTraining(elevHigh = 500.0)))

        val count = useCase(token)

        assertEquals(0, count)
        verify(trainingRepository, never()).add(any())
    }

    @Test
    fun `reuses the stored elevation stream instead of refetching it`() = runTest {
        val training = makeTraining(elevationPointsJson = jsonMapper.writeValueAsString(elevPoints))
        val peak = Peak(name = "Monte Rosa", latLng = LatLng(45.9, 7.6), elevation = 4634.0)
        whenever(trainingRepository.getAllTrainings()).thenReturn(listOf(training))
        whenever(peakLookupRepository.findNearestPeak(any(), eq(3500.0))).thenReturn(peak)

        val count = useCase(token)

        assertEquals(1, count)
        verify(trainingApi, never()).fetchElevationStream(any(), any())
        verify(trainingRepository).add(argThat { peakName == "Monte Rosa" && peakElevation == 4634.0 })
    }

    @Test
    fun `fetches and persists the elevation stream when missing locally`() = runTest {
        val training = makeTraining()
        val peak = Peak(name = "Monte Rosa", latLng = LatLng(45.9, 7.6), elevation = 4634.0)
        whenever(trainingRepository.getAllTrainings()).thenReturn(listOf(training))
        whenever(trainingApi.fetchElevationStream(token, "1")).thenReturn(elevPoints)
        whenever(peakLookupRepository.findNearestPeak(any(), eq(3500.0))).thenReturn(peak)

        val count = useCase(token)

        assertEquals(1, count)
        verify(trainingRepository).add(argThat {
            peakName == "Monte Rosa" && elevationPointsJson == jsonMapper.writeValueAsString(elevPoints)
        })
    }

    @Test
    fun `leaves the training unresolved when the lookup fails, so a later run can retry it`() = runTest {
        val training = makeTraining(elevationPointsJson = jsonMapper.writeValueAsString(elevPoints))
        whenever(trainingRepository.getAllTrainings()).thenReturn(listOf(training))
        whenever(peakLookupRepository.findNearestPeak(any(), any()))
            .thenThrow(PeakLookupException("Overpass unavailable"))

        val count = useCase(token)

        assertEquals(0, count)
        verify(trainingRepository).add(argThat { peakName == null })
    }

    @Test
    fun `does not count a confirmed no-peak-nearby result, but still settles it so it is not retried again`() = runTest {
        val training = makeTraining(elevationPointsJson = jsonMapper.writeValueAsString(elevPoints))
        whenever(trainingRepository.getAllTrainings()).thenReturn(listOf(training))
        whenever(peakLookupRepository.findNearestPeak(any(), any())).thenReturn(null)

        val count = useCase(token)

        assertEquals(0, count)
        verify(trainingRepository).add(argThat { peakName == "" })
    }

    @Test
    fun `reports progress across all candidates`() = runTest {
        val trainings = listOf(
            makeTraining(id = "1", elevationPointsJson = jsonMapper.writeValueAsString(elevPoints)),
            makeTraining(id = "2", elevationPointsJson = jsonMapper.writeValueAsString(elevPoints))
        )
        whenever(trainingRepository.getAllTrainings()).thenReturn(trainings)
        whenever(peakLookupRepository.findNearestPeak(any(), any())).thenReturn(null)
        val progressUpdates = mutableListOf<Pair<Int, Int>>()

        useCase(token) { current, total -> progressUpdates.add(current to total) }

        assertEquals(listOf(1 to 2, 2 to 2), progressUpdates)
    }
}
