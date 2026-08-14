package com.statsup.domain

import com.google.android.gms.maps.model.LatLng
import com.statsup.domain.repository.AthleteRepository
import com.statsup.domain.repository.Peak
import com.statsup.domain.repository.PeakLookupRepository
import com.statsup.domain.repository.TrainingRepository
import com.statsup.infrastructure.repository.DbBookmarkedTrainingRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
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
        runBlocking {
            whenever(trainingApi.laps(any(), any())).thenReturn(emptyList())
            whenever(trainingRepository.getAllTrainings()).thenReturn(emptyList())
        }
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
    fun `replaces the whole training history atomically after all data is fetched`() = runTest {
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenReturn(emptyList())
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        // replaceAll() must be called exactly once — deleteAll() is never called directly by
        // the use case, so a fetch failure never wipes existing data (see safety tests below).
        verify(trainingRepository).replaceAll(emptyList())
        verify(trainingRepository, never()).deleteAll()
        verify(trainingRepository, never()).add(any())
    }

    @Test
    fun `saves each downloaded training in a single atomic replaceAll call`() = runTest {
        val trainings = listOf(makeTraining(id = "10"), makeTraining(id = "20"))
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenReturn(trainings)
        whenever(trainingApi.athlete(token)).thenReturn(athlete)

        useCase(token)

        verify(trainingRepository).replaceAll(argThat { size == 2 && containsAll(trainings) })
    }

    @Test
    fun `never touches the repository at all if fetching from the API fails`() = runTest {
        // Safety guarantee: a network failure while fetching must never delete existing data.
        // The old data stays intact and the user can just retry the full import.
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenAnswer { throw ApiException(500) }

        try {
            useCase(token)
            org.junit.Assert.fail("Expected an ApiException to be thrown")
        } catch (e: ApiException) {
            // expected
        }

        verify(trainingRepository, never()).deleteAll()
        verify(trainingRepository, never()).replaceAll(any())
        verify(trainingRepository, never()).add(any())
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
    fun `still returns successfully and keeps the replaced trainings when the trailing athlete fetch fails`() = runTest {
        // The training history is already persisted (replaceAll) by the time athlete() runs;
        // a failure refreshing the profile must not be reported as a failed import.
        val trainings = listOf(makeTraining(id = "1"))
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenReturn(trainings)
        whenever(trainingApi.athlete(token)).thenAnswer { throw ApiException(500) }

        val result = useCase(token)

        assertEquals(1, result)
        verify(trainingRepository).replaceAll(trainings)
        verify(athleteRepository, never()).update(any())
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
    fun `reads saved bookmarks BEFORE replacing trainings`() = runTest {
        // This is crucial: if bookmarks were read after replaceAll() (which deletes then
        // re-inserts), cascade FK delete would have already wiped them from DB.
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
        orderVerifier.verify(trainingRepository).replaceAll(any())
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

    // --- Peak lookup enrichment ---

    private val summitRoute = listOf(LatLng(45.0, 7.0), LatLng(45.9, 7.6), LatLng(46.0, 8.0))
    private val summitPolyline = com.google.maps.android.PolyUtil.encode(summitRoute)

    // PolyUtil round-trips through a fixed-precision encoding, so match with a small tolerance.
    private fun LatLng.isCloseTo(lat: Double, lng: Double) =
        Math.abs(latitude - lat) < 1e-4 && Math.abs(longitude - lng) < 1e-4

    @Test
    fun `reuses an already-resolved peak from the existing training instead of re-querying the network`() = runTest {
        // A full re-import must not throw away peak names already resolved by a previous
        // incremental sync, nor waste an Overpass round-trip re-confirming them.
        val peakLookupRepository: PeakLookupRepository = mock()
        val downloadedTraining = makeTraining(id = "1").copy(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        val existingTraining = downloadedTraining.copy(peakName = "Monte Rosa", peakElevation = 4634.0)
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingRepository.getAllTrainings()).thenReturn(listOf(existingTraining))
        whenever(trainingApi.download(token, null)).thenReturn(listOf(downloadedTraining))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)
        whenever(trainingApi.fetchElevationStream(token, "1")).thenReturn(listOf(2000.0, 3500.0, 2500.0))
        val useCaseWithPeaks = FullImportUseCase(
            trainingRepository, athleteRepository, bookmarkedTrainingRepository, trainingApi,
            peakLookupRepository = peakLookupRepository
        )

        useCaseWithPeaks(token)

        verify(peakLookupRepository, never()).findNearestPeak(any(), any())
        verify(trainingRepository).replaceAll(argThat { any { it.peakName == "Monte Rosa" && it.peakElevation == 4634.0 } })
    }

    @Test
    fun `reuses an existing confirmed negative instead of re-querying the network`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val downloadedTraining = makeTraining(id = "1").copy(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        val existingTraining = downloadedTraining.copy(peakName = "", peakElevation = null)
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingRepository.getAllTrainings()).thenReturn(listOf(existingTraining))
        whenever(trainingApi.download(token, null)).thenReturn(listOf(downloadedTraining))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)
        whenever(trainingApi.fetchElevationStream(token, "1")).thenReturn(listOf(2000.0, 3500.0, 2500.0))
        val useCaseWithPeaks = FullImportUseCase(
            trainingRepository, athleteRepository, bookmarkedTrainingRepository, trainingApi,
            peakLookupRepository = peakLookupRepository
        )

        useCaseWithPeaks(token)

        verify(peakLookupRepository, never()).findNearestPeak(any(), any())
        verify(trainingRepository).replaceAll(argThat { any { it.peakName == "" } })
    }

    @Test
    fun `still queries the network for a training left unresolved by a previous import`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val downloadedTraining = makeTraining(id = "1").copy(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        val existingTraining = downloadedTraining.copy(peakName = null, peakElevation = null)
        val peak = Peak(name = "Monte Rosa", latLng = LatLng(45.9, 7.6), elevation = 4634.0)
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingRepository.getAllTrainings()).thenReturn(listOf(existingTraining))
        whenever(trainingApi.download(token, null)).thenReturn(listOf(downloadedTraining))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)
        whenever(trainingApi.fetchElevationStream(token, "1")).thenReturn(listOf(2000.0, 3500.0, 2500.0))
        whenever(peakLookupRepository.findNearestPeak(argThat { isCloseTo(45.9, 7.6) }, org.mockito.kotlin.eq(3500.0))).thenReturn(peak)
        val useCaseWithPeaks = FullImportUseCase(
            trainingRepository, athleteRepository, bookmarkedTrainingRepository, trainingApi,
            peakLookupRepository = peakLookupRepository
        )

        useCaseWithPeaks(token)

        verify(peakLookupRepository).findNearestPeak(any(), any())
        verify(trainingRepository).replaceAll(argThat { any { it.peakName == "Monte Rosa" && it.peakElevation == 4634.0 } })
    }

    @Test
    fun `does not look up a peak when elevHigh is below the threshold`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val lowTraining = makeTraining(id = "1").copy(elevHigh = 500.0)
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenReturn(listOf(lowTraining))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)
        val useCaseWithPeaks = FullImportUseCase(
            trainingRepository, athleteRepository, bookmarkedTrainingRepository, trainingApi,
            peakLookupRepository = peakLookupRepository
        )

        useCaseWithPeaks(token)

        verify(peakLookupRepository, never()).findNearestPeak(any(), any())
    }

    @Test
    fun `resolves and persists the peak name when a nearby peak is found`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val highTraining = makeTraining(id = "1").copy(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        val elevPoints = listOf(2000.0, 3500.0, 2500.0)
        val peak = Peak(name = "Monte Rosa", latLng = LatLng(45.9, 7.6), elevation = 4634.0)
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenReturn(listOf(highTraining))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)
        whenever(trainingApi.fetchElevationStream(token, "1")).thenReturn(elevPoints)
        whenever(peakLookupRepository.findNearestPeak(argThat { isCloseTo(45.9, 7.6) }, org.mockito.kotlin.eq(3500.0))).thenReturn(peak)
        val useCaseWithPeaks = FullImportUseCase(
            trainingRepository, athleteRepository, bookmarkedTrainingRepository, trainingApi,
            peakLookupRepository = peakLookupRepository
        )

        useCaseWithPeaks(token)

        verify(trainingRepository).replaceAll(argThat { any { it.peakName == "Monte Rosa" && it.peakElevation == 4634.0 } })
    }

    @Test
    fun `leaves the peak unresolved instead of blank when the lookup fails`() = runTest {
        val peakLookupRepository: PeakLookupRepository = mock()
        val highTraining = makeTraining(id = "1").copy(elevHigh = 3500.0, map = Route(summaryPolyline = summitPolyline))
        val elevPoints = listOf(2000.0, 3500.0, 2500.0)
        whenever(bookmarkedTrainingRepository.getAllBookmarksList()).thenReturn(emptyList())
        whenever(trainingApi.download(token, null)).thenReturn(listOf(highTraining))
        whenever(trainingApi.athlete(token)).thenReturn(athlete)
        whenever(trainingApi.fetchElevationStream(token, "1")).thenReturn(elevPoints)
        whenever(peakLookupRepository.findNearestPeak(any(), any()))
            .thenThrow(com.statsup.domain.repository.PeakLookupException("Overpass unavailable"))
        val useCaseWithPeaks = FullImportUseCase(
            trainingRepository, athleteRepository, bookmarkedTrainingRepository, trainingApi,
            peakLookupRepository = peakLookupRepository
        )

        useCaseWithPeaks(token)

        verify(trainingRepository).replaceAll(argThat { any { it.peakName == null && it.peakElevation == null } })
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
