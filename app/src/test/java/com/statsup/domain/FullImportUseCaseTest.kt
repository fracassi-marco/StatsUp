package com.statsup.domain

import com.statsup.domain.repository.AthleteRepository
import com.statsup.domain.repository.TrainingRepository
import com.statsup.infrastructure.repository.DbBookmarkedTrainingRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FullImportUseCaseTest {

    private lateinit var trainingRepository: TrainingRepository
    private lateinit var athleteRepository: AthleteRepository
    private lateinit var bookmarkedTrainingRepository: DbBookmarkedTrainingRepository
    private lateinit var trainingApi: TrainingApi
    private lateinit var useCase: FullImportUseCase

    private val token = "test-token"
    private val athlete = Athlete(id = 42L, username = "marco")

    @Before
    fun setUp() {
        trainingRepository = mock()
        athleteRepository = mock()
        bookmarkedTrainingRepository = mock()
        trainingApi = mock()
        runBlocking { whenever(trainingApi.laps(any(), any())).thenReturn(emptyList()) }
        useCase = FullImportUseCase(
            trainingRepository,
            athleteRepository,
            bookmarkedTrainingRepository,
            trainingApi
        )
    }

    // --- Happy path ---

    @Test
    fun `returns count of downloaded trainings`() = runTest {
        val trainings = listOf(makeTraining(id = "1"), makeTraining(id = "2"))
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenReturn(trainings)
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        val result = useCase(token)

        assertEquals(2, result)
    }

    @Test
    fun `deletes all trainings before downloading`() = runTest {
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenReturn(emptyList())
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        // deleteAll() must be called exactly once
        verify(trainingRepository).deleteAll()
    }

    @Test
    fun `saves each downloaded training to repository`() = runTest {
        val trainings = listOf(makeTraining(id = "10"), makeTraining(id = "20"))
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenReturn(trainings)
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        verify(trainingRepository).add(trainings[0])
        verify(trainingRepository).add(trainings[1])
        verify(trainingRepository, times(2)).add(any())
    }

    @Test
    fun `updates athlete after import`() = runTest {
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenReturn(emptyList())
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        verify(athleteRepository).update(athlete)
    }

    @Test
    fun `passes null as latest to API during full import`() = runTest {
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenReturn(emptyList())
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        verify(trainingApi).download(token, null)
    }

    // --- Bookmark preservation ---

    @Test
    fun `restores bookmarks whose trainingId is still present after import`() = runTest {
        val training = makeTraining(id = "100")
        val bookmark = BookmarkedTraining(id = 5L, trainingId = "100", note = "great run")
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(listOf(bookmark))
        whenever(trainingApi.download(token, null)).thenReturn(listOf(training))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        // bookmark must be re-inserted with id=0 so Room can auto-generate a new PK
        val captor = argumentCaptor<BookmarkedTraining>()
        verify(bookmarkedTrainingRepository).addBookmark(captor.capture())
        assertEquals(0L, captor.firstValue.id)
        assertEquals("100", captor.firstValue.trainingId)
        assertEquals("great run", captor.firstValue.note)
    }

    @Test
    fun `does NOT restore bookmarks whose trainingId is no longer in the import`() = runTest {
        val training = makeTraining(id = "100")
        val orphanBookmark = BookmarkedTraining(id = 3L, trainingId = "999")
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(listOf(orphanBookmark))
        whenever(trainingApi.download(token, null)).thenReturn(listOf(training))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        // The orphan bookmark (trainingId=999 not in import) must not be re-added
        verify(bookmarkedTrainingRepository, never()).addBookmark(any())
    }

    @Test
    fun `restores only matching bookmarks when list is mixed`() = runTest {
        val training1 = makeTraining(id = "1")
        val training2 = makeTraining(id = "2")
        val bookmarkKeep = BookmarkedTraining(id = 1L, trainingId = "1")
        val bookmarkDrop = BookmarkedTraining(id = 2L, trainingId = "99") // not in import
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(
            listOf(bookmarkKeep, bookmarkDrop)
        )
        whenever(trainingApi.download(token, null)).thenReturn(listOf(training1, training2))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        // Only bookmarkKeep should be re-added
        val captor = argumentCaptor<BookmarkedTraining>()
        verify(bookmarkedTrainingRepository, times(1)).addBookmark(captor.capture())
        assertEquals("1", captor.firstValue.trainingId)
    }

    // --- Edge cases ---

    @Test
    fun `reads saved bookmarks BEFORE deleting trainings`() = runTest {
        // This is crucial: if bookmarks were read after deleteAll(), cascade FK delete
        // would have already wiped them from DB.
        // We verify getAllBookmarksList() is called (the fact that the mock returns data
        // proves it was called before any delete that might cascade).
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenReturn(emptyList())
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        val orderVerifier = org.mockito.kotlin.inOrder(
            bookmarkedTrainingRepository,
            trainingRepository
        )
        orderVerifier.verify(bookmarkedTrainingRepository).getAllBookmarksList()
        orderVerifier.verify(trainingRepository).deleteAll()
    }

    @Test
    fun `when API returns empty list returns zero and no bookmarks are restored`() = runTest {
        // Potential data-loss scenario: if the API returns [] (network glitch?),
        // all trainings are deleted and no bookmarks survive. Test documents this behaviour.
        val bookmark = BookmarkedTraining(id = 1L, trainingId = "42")
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(listOf(bookmark))
        whenever(trainingApi.download(token, null)).thenReturn(emptyList())
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        val result = useCase(token)

        assertEquals(0, result)
        // trainingId=42 is not in importedIds (empty), so bookmark is dropped silently
        verify(bookmarkedTrainingRepository, never()).addBookmark(any())
    }

    @Test
    fun `handles no pre-existing bookmarks gracefully`() = runTest {
        val trainings = listOf(makeTraining(id = "1"))
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenReturn(trainings)
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        val result = useCase(token)

        assertEquals(1, result)
        verify(bookmarkedTrainingRepository, never()).addBookmark(any())
    }

    @Test
    fun `bookmark id is always reset to 0 when restoring`() = runTest {
        // If id is NOT reset to 0, Room's autoGenerate will use the old id as explicit PK,
        // potentially conflicting with an existing row.
        val training = makeTraining(id = "7")
        val bookmark = BookmarkedTraining(id = 999L, trainingId = "7") // large existing id
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(listOf(bookmark))
        whenever(trainingApi.download(token, null)).thenReturn(listOf(training))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        val captor = argumentCaptor<BookmarkedTraining>()
        verify(bookmarkedTrainingRepository).addBookmark(captor.capture())
        assertEquals(0L, captor.firstValue.id)
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
