package com.statsup.domain

import com.google.android.gms.maps.model.LatLng
import com.statsup.domain.repository.AthleteRepository
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UpdateTrainingsUseCaseTest {

    private lateinit var trainingRepository: TrainingRepository
    private lateinit var athleteRepository: AthleteRepository
    private lateinit var trainingApi: TrainingApi
    private lateinit var useCase: UpdateTrainingsUseCase

    private val token = "test-token"
    private val athlete = Athlete(id = 42L, username = "marco")

    @Before
    fun setUp() {
        trainingRepository = mock()
        athleteRepository = mock()
        trainingApi = mock()
        runBlocking { whenever(trainingApi.laps(any(), any())).thenReturn(emptyList()) }
        useCase = UpdateTrainingsUseCase(trainingRepository, athleteRepository, trainingApi)
    }

    // --- Happy path ---

    @Test
    fun `returns count of downloaded trainings`() = runTest {
        val trainings = listOf(makeTraining(id = "1"), makeTraining(id = "2"))
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(trainings)
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        val result = useCase(token)

        assertEquals(2, result)
    }

    @Test
    fun `saves each new training to repository`() = runTest {
        val trainings = listOf(makeTraining(id = "10"), makeTraining(id = "20"))
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(trainings)
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        verify(trainingRepository).add(trainings[0])
        verify(trainingRepository).add(trainings[1])
        verify(trainingRepository, times(2)).add(any())
    }

    @Test
    fun `updates athlete after downloading trainings`() = runTest {
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(emptyList())
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        verify(athleteRepository).update(athlete)
    }

    @Test
    fun `still returns successfully and keeps the persisted trainings when the trailing athlete fetch fails`() = runTest {
        // New trainings are already persisted (add, one at a time) by the time athlete() runs;
        // a failure refreshing the profile must not be reported as a failed import.
        val trainings = listOf(makeTraining(id = "1"))
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(trainings)
        whenever(trainingApi.athlete(token)).thenAnswer { throw ApiException(500) }

        val result = useCase(token)

        assertEquals(1, result)
        verify(trainingRepository).add(trainings[0])
        verify(athleteRepository, never()).update(any())
    }

    // --- Latest training passed to API ---

    @Test
    fun `passes null to API when repository has no trainings`() = runTest {
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(emptyList())
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        verify(trainingApi).download(token, null)
    }

    @Test
    fun `passes latest training to API so only newer ones are fetched`() = runTest {
        val latest = makeTraining(id = "5")
        val newTrainings = listOf(makeTraining(id = "6"))
        whenever(trainingRepository.latest()).thenReturn(latest)
        whenever(trainingApi.download(token, latest)).thenReturn(newTrainings)
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        val result = useCase(token)

        verify(trainingApi).download(token, latest)
        assertEquals(1, result)
    }

    // --- Edge cases ---

    @Test
    fun `when API returns empty list nothing is added to repository`() = runTest {
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(emptyList())
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        val result = useCase(token)

        assertEquals(0, result)
        verify(trainingRepository, never()).add(any())
    }

    @Test
    fun `does NOT call deleteAll unlike full import`() = runTest {
        // UpdateTrainingsUseCase is incremental: it must never wipe existing data
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(emptyList())
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        verify(trainingRepository, never()).deleteAll()
    }

    @Test
    fun `returns zero when there are no new trainings since latest`() = runTest {
        val latest = makeTraining(id = "99")
        whenever(trainingRepository.latest()).thenReturn(latest)
        whenever(trainingApi.download(token, latest)).thenReturn(emptyList())
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        val result = useCase(token)

        assertEquals(0, result)
    }

    @Test
    fun `athlete is always updated even when no new trainings are found`() = runTest {
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(emptyList())
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        // Athlete profile should always be refreshed regardless of training count
        verify(athleteRepository).update(athlete)
    }

    @Test
    fun `multiple new trainings are all persisted`() = runTest {
        val newTrainings = (1..5).map { makeTraining(id = it.toString()) }
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(newTrainings)
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        verify(trainingRepository, times(5)).add(any())
        newTrainings.forEach { verify(trainingRepository).add(it) }
    }

    // --- Peak lookup enrichment ---

    private val summitRoute = listOf(LatLng(45.0, 7.0), LatLng(45.9, 7.6), LatLng(46.0, 8.0))
    private val summitPolyline = com.google.maps.android.PolyUtil.encode(summitRoute)

    // PolyUtil round-trips through a fixed-precision encoding, so match with a small tolerance.
    private fun LatLng.isCloseTo(lat: Double, lng: Double) =
        Math.abs(latitude - lat) < 1e-4 && Math.abs(longitude - lng) < 1e-4

    @Test
    fun `does not look up a peak when elevHigh is below the threshold`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val lowTraining = makeTraining(id = "1").copy(elevHigh = 500.0)
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(listOf(lowTraining))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)
        val useCaseWithPeaks = UpdateTrainingsUseCase(trainingRepository, athleteRepository, trainingApi, peakLookupRepository = peakLookupRepository)

        useCaseWithPeaks(token)

        verify(peakLookupRepository, never()).findNearestPeak(any(), any())
    }

    @Test
    fun `resolves and persists the peak name when a nearby peak is found`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val highTraining = makeTraining(id = "1").copy(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        val elevPoints = listOf(2000.0, 3500.0, 2500.0)
        val peak = Peak(name = "Monte Rosa", latLng = LatLng(45.9, 7.6), elevation = 4634.0)
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(listOf(highTraining))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)
        whenever(trainingApi.fetchElevationStream(token, "1")).thenReturn(elevPoints)
        whenever(peakLookupRepository.findNearestPeak(argThat { isCloseTo(45.9, 7.6) }, org.mockito.kotlin.eq(3500.0))).thenReturn(peak)
        val useCaseWithPeaks = UpdateTrainingsUseCase(trainingRepository, athleteRepository, trainingApi, peakLookupRepository = peakLookupRepository)

        useCaseWithPeaks(token)

        verify(trainingRepository).add(argThat { peakName == "Monte Rosa" && peakElevation == 4634.0 })
    }

    @Test
    fun `persists an empty peak name sentinel when no peak is nearby`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val highTraining = makeTraining(id = "1").copy(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        val elevPoints = listOf(2000.0, 3500.0, 2500.0)
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(listOf(highTraining))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)
        whenever(trainingApi.fetchElevationStream(token, "1")).thenReturn(elevPoints)
        whenever(peakLookupRepository.findNearestPeak(any(), any())).thenReturn(null)
        val useCaseWithPeaks = UpdateTrainingsUseCase(trainingRepository, athleteRepository, trainingApi, peakLookupRepository = peakLookupRepository)

        useCaseWithPeaks(token)

        verify(trainingRepository).add(argThat { peakName == "" && peakElevation == null })
    }

    @Test
    fun `leaves the peak unresolved instead of blank when the lookup fails`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val highTraining = makeTraining(id = "1").copy(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        val elevPoints = listOf(2000.0, 3500.0, 2500.0)
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(listOf(highTraining))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)
        whenever(trainingApi.fetchElevationStream(token, "1")).thenReturn(elevPoints)
        whenever(peakLookupRepository.findNearestPeak(any(), any()))
            .thenThrow(com.statsup.domain.repository.PeakLookupException("Overpass unavailable"))
        val useCaseWithPeaks = UpdateTrainingsUseCase(trainingRepository, athleteRepository, trainingApi, peakLookupRepository = peakLookupRepository)

        useCaseWithPeaks(token)

        verify(trainingRepository).add(argThat { peakName == null && peakElevation == null })
    }

    @Test
    fun `skips peak lookup entirely when no repository is configured`() = runTest {
        val highTraining = makeTraining(id = "1").copy(elevHigh = 3500.0)
        whenever(trainingRepository.latest()).thenReturn(null)
        whenever(trainingApi.download(token, null)).thenReturn(listOf(highTraining))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        verify(trainingRepository).add(argThat { peakName == null })
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
