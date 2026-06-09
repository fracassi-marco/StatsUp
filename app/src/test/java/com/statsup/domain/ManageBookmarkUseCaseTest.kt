package com.statsup.domain

import com.statsup.infrastructure.repository.DbBookmarkedTrainingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ManageBookmarkUseCaseTest {

    private lateinit var repository: DbBookmarkedTrainingRepository
    private lateinit var useCase: ManageBookmarkUseCase

    private val trainingId = "42"

    @Before
    fun setUp() {
        repository = mock()
        useCase = ManageBookmarkUseCase(repository)
    }

    // -------------------------------------------------------------------------
    // getBookmark
    // -------------------------------------------------------------------------

    @Test
    fun `getBookmark delegates to repository`() = runTest {
        val bookmark = makeBookmark(trainingId = trainingId)
        whenever(repository.getBookmarkByTrainingId(trainingId)).thenReturn(bookmark)

        val result = useCase.getBookmark(trainingId)

        assertEquals(bookmark, result)
        verify(repository).getBookmarkByTrainingId(trainingId)
    }

    @Test
    fun `getBookmark returns null when no bookmark exists`() = runTest {
        whenever(repository.getBookmarkByTrainingId(trainingId)).thenReturn(null)

        assertNull(useCase.getBookmark(trainingId))
    }

    // -------------------------------------------------------------------------
    // addOrUpdate — new bookmark
    // -------------------------------------------------------------------------

    @Test
    fun `addOrUpdate calls addBookmark when no existing bookmark`() = runTest {
        whenever(repository.getBookmarkByTrainingId(trainingId)).thenReturn(null)

        useCase.addOrUpdate(trainingId, note = "great", customTitle = "Hill run", difficulty = "hard")

        val captor = argumentCaptor<BookmarkedTraining>()
        verify(repository).addBookmark(captor.capture())
        verify(repository, never()).updateBookmark(any(), any(), any(), any())
    }

    @Test
    fun `addOrUpdate creates bookmark with correct fields`() = runTest {
        whenever(repository.getBookmarkByTrainingId(trainingId)).thenReturn(null)

        useCase.addOrUpdate(trainingId, note = "fun run", customTitle = "City 10k", difficulty = "easy")

        val captor = argumentCaptor<BookmarkedTraining>()
        verify(repository).addBookmark(captor.capture())
        val created = captor.firstValue
        assertEquals(trainingId, created.trainingId)
        assertEquals("fun run", created.note)
        assertEquals("City 10k", created.customTitle)
        assertEquals("easy", created.difficulty)
    }

    @Test
    fun `addOrUpdate creates bookmark with id=0 so Room auto-generates PK`() = runTest {
        whenever(repository.getBookmarkByTrainingId(trainingId)).thenReturn(null)

        useCase.addOrUpdate(trainingId, note = "", customTitle = "", difficulty = "")

        val captor = argumentCaptor<BookmarkedTraining>()
        verify(repository).addBookmark(captor.capture())
        assertEquals(0L, captor.firstValue.id)
    }

    // -------------------------------------------------------------------------
    // addOrUpdate — existing bookmark
    // -------------------------------------------------------------------------

    @Test
    fun `addOrUpdate calls updateBookmark when bookmark already exists`() = runTest {
        whenever(repository.getBookmarkByTrainingId(trainingId))
            .thenReturn(makeBookmark(trainingId = trainingId))

        useCase.addOrUpdate(trainingId, note = "updated note", customTitle = "New title", difficulty = "medium")

        verify(repository).updateBookmark(trainingId, "updated note", "New title", "medium")
        verify(repository, never()).addBookmark(any())
    }

    @Test
    fun `addOrUpdate passes correct arguments to updateBookmark`() = runTest {
        whenever(repository.getBookmarkByTrainingId(trainingId))
            .thenReturn(makeBookmark(trainingId = trainingId))

        useCase.addOrUpdate(trainingId, note = "note", customTitle = "title", difficulty = "hard")

        verify(repository).updateBookmark(trainingId, "note", "title", "hard")
    }

    // -------------------------------------------------------------------------
    // remove
    // -------------------------------------------------------------------------

    @Test
    fun `remove calls removeBookmarkByTrainingId on repository`() = runTest {
        useCase.remove(trainingId)

        verify(repository).removeBookmarkByTrainingId(trainingId)
    }

    @Test
    fun `remove passes the correct trainingId`() = runTest {
        val otherId = "99"

        useCase.remove(otherId)

        verify(repository).removeBookmarkByTrainingId(otherId)
        verify(repository, never()).removeBookmarkByTrainingId(trainingId)
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private fun makeBookmark(trainingId: String = "1") =
        BookmarkedTraining(id = 1L, trainingId = trainingId, note = "original")
}
