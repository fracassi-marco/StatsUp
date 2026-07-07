package com.statsup.domain

import com.statsup.domain.repository.AthleteRepository
import com.statsup.domain.repository.TrainingRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
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
