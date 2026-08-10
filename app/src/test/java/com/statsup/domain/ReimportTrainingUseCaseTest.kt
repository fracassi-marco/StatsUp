package com.statsup.domain

import com.google.android.gms.maps.model.LatLng
import com.statsup.domain.repository.Peak
import com.statsup.domain.repository.PeakLookupRepository
import com.statsup.domain.repository.TrainingRepository
import kotlinx.coroutines.runBlocking
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

class ReimportTrainingUseCaseTest {

    private lateinit var trainingRepository: TrainingRepository
    private lateinit var trainingApi: TrainingApi
    private lateinit var useCase: ReimportTrainingUseCase

    private val token = "test-token"

    @Before
    fun setUp() {
        trainingRepository = mock()
        trainingApi = mock()
        runBlocking { whenever(trainingApi.laps(any(), any())).thenReturn(emptyList()) }
        useCase = ReimportTrainingUseCase(trainingRepository, trainingApi)
    }

    // --- Happy path ---

    @Test
    fun `fetches the single training by id and saves it to the repository`() = runTest {
        val training = makeTraining(id = "1")
        whenever(trainingApi.fetchActivityById(token, "1")).thenReturn(training)

        val result = useCase(token, "1")

        assertEquals("1", result.id)
        verify(trainingRepository).add(training)
    }

    @Test
    fun `throws an ApiException when the training is not found on the server`() = runTest {
        whenever(trainingApi.fetchActivityById(token, "missing")).thenReturn(null)

        try {
            useCase(token, "missing")
            org.junit.Assert.fail("Expected an ApiException to be thrown")
        } catch (e: ApiException) {
            assertEquals(404, e.statusCode)
        }
        verify(trainingRepository, never()).add(any())
    }

    @Test
    fun `enriches with polyline when the training has no trip`() = runTest {
        val training = makeTraining(id = "1")
        whenever(trainingApi.fetchActivityById(token, "1")).thenReturn(training)
        whenever(trainingApi.fetchPolyline(token, "1")).thenReturn(summitPolyline)

        useCase(token, "1")

        verify(trainingRepository).add(argThat { map?.summaryPolyline == summitPolyline })
    }

    @Test
    fun `enriches with laps when present`() = runTest {
        val training = makeTraining(id = "1")
        val lap = Lap(split = 1, distance = 1000.0, movingTime = 300, elapsedTime = 300)
        whenever(trainingApi.fetchActivityById(token, "1")).thenReturn(training)
        whenever(trainingApi.laps(token, "1")).thenReturn(listOf(lap))

        useCase(token, "1")

        verify(trainingRepository).add(argThat { lapsJson != null && lapsJson!!.contains("1000.0") })
    }

    // --- Peak lookup enrichment ---

    private val summitRoute = listOf(LatLng(45.0, 7.0), LatLng(45.9, 7.6), LatLng(46.0, 8.0))
    private val summitPolyline = com.google.maps.android.PolyUtil.encode(summitRoute)

    private fun LatLng.isCloseTo(lat: Double, lng: Double) =
        Math.abs(latitude - lat) < 1e-4 && Math.abs(longitude - lng) < 1e-4

    @Test
    fun `does not look up a peak when elevHigh is below the threshold`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val lowTraining = makeTraining(id = "1").copy(elevHigh = 500.0)
        whenever(trainingApi.fetchActivityById(token, "1")).thenReturn(lowTraining)
        val useCaseWithPeaks = ReimportTrainingUseCase(trainingRepository, trainingApi, peakLookupRepository = peakLookupRepository)

        useCaseWithPeaks(token, "1")

        verify(peakLookupRepository, never()).findNearestPeak(any(), any())
    }

    @Test
    fun `resolves and persists the peak name when a nearby peak is found`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val highTraining = makeTraining(id = "1").copy(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        val elevPoints = listOf(2000.0, 3500.0, 2500.0)
        val peak = Peak(name = "Monte Rosa", latLng = LatLng(45.9, 7.6), elevation = 4634.0)
        whenever(trainingApi.fetchActivityById(token, "1")).thenReturn(highTraining)
        whenever(trainingApi.fetchElevationStream(token, "1")).thenReturn(elevPoints)
        whenever(peakLookupRepository.findNearestPeak(argThat { isCloseTo(45.9, 7.6) }, eq(3500.0))).thenReturn(peak)
        val useCaseWithPeaks = ReimportTrainingUseCase(trainingRepository, trainingApi, peakLookupRepository = peakLookupRepository)

        useCaseWithPeaks(token, "1")

        verify(trainingRepository).add(argThat { peakName == "Monte Rosa" && peakElevation == 4634.0 })
    }

    @Test
    fun `preserves the existing peak name when the elevation stream is unavailable on reimport`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val highTraining = makeTraining(id = "1").copy(elevHigh = 3500.0)
        val previouslyResolved = makeTraining(id = "1").copy(
            elevHigh = 3500.0, peakName = "Monte Rosa", peakElevation = 4634.0
        )
        whenever(trainingApi.fetchActivityById(token, "1")).thenReturn(highTraining)
        whenever(trainingApi.fetchElevationStream(token, "1")).thenReturn(null)
        whenever(trainingRepository.byId("1")).thenReturn(previouslyResolved)
        val useCaseWithPeaks = ReimportTrainingUseCase(trainingRepository, trainingApi, peakLookupRepository = peakLookupRepository)

        useCaseWithPeaks(token, "1")

        verify(trainingRepository).add(argThat { peakName == "Monte Rosa" && peakElevation == 4634.0 })
    }

    @Test
    fun `preserves the existing peak name when the peak lookup fails on reimport`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val highTraining = makeTraining(id = "1").copy(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        val previouslyResolved = makeTraining(id = "1").copy(
            elevHigh = 3500.0, peakName = "Monte Rosa", peakElevation = 4634.0
        )
        val elevPoints = listOf(2000.0, 3500.0, 2500.0)
        whenever(trainingApi.fetchActivityById(token, "1")).thenReturn(highTraining)
        whenever(trainingApi.fetchElevationStream(token, "1")).thenReturn(elevPoints)
        whenever(peakLookupRepository.findNearestPeak(any(), any())).thenReturn(null)
        whenever(trainingRepository.byId("1")).thenReturn(previouslyResolved)
        val useCaseWithPeaks = ReimportTrainingUseCase(trainingRepository, trainingApi, peakLookupRepository = peakLookupRepository)

        useCaseWithPeaks(token, "1")

        verify(trainingRepository).add(argThat { peakName == "Monte Rosa" && peakElevation == 4634.0 })
    }

    // --- Helper ---

    private fun makeTraining(id: String = "1") = Training(
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
        elevHigh = 0.0,
        elevLow = 0.0,
        uploadId = 0L,
        sufferScore = null
    )
}
