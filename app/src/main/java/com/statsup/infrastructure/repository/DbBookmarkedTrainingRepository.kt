package com.statsup.infrastructure.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.statsup.domain.BookmarkedTraining
import com.statsup.domain.Training
import kotlinx.coroutines.flow.Flow

@Dao
interface DbBookmarkedTrainingRepository {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: BookmarkedTraining): Long

    @Delete
    suspend fun removeBookmark(bookmark: BookmarkedTraining)

    @Query("DELETE FROM bookmarked_training WHERE trainingId = :trainingId")
    suspend fun removeBookmarkByTrainingId(trainingId: Long)

    @Query("SELECT * FROM bookmarked_training WHERE trainingId = :trainingId")
    suspend fun getBookmarkByTrainingId(trainingId: Long): BookmarkedTraining?

    @Query("SELECT * FROM bookmarked_training ORDER BY bookmarkedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkedTraining>>

    @Transaction
    @Query("""
        SELECT t.* FROM training t
        INNER JOIN bookmarked_training bt ON t.id = bt.trainingId
        ORDER BY bt.bookmarkedAt DESC
    """)
    fun getBookmarkedTrainings(): Flow<List<Training>>

    @Query("UPDATE bookmarked_training SET note = :note WHERE trainingId = :trainingId")
    suspend fun updateNote(trainingId: Long, note: String)
}

