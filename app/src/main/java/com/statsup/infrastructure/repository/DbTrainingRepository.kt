package com.statsup.infrastructure

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.statsup.domain.Training
import com.statsup.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow

@Dao
interface DbTrainingRepository : TrainingRepository {
    @Insert(onConflict = REPLACE)
    override suspend fun add(training: Training): Long

    @Query("SELECT * FROM training ORDER BY startDate DESC")
    override fun all(): Flow<List<Training>>

    @Query("SELECT * FROM training ORDER BY startDate DESC LIMIT 1")
    override fun latest(): Training

    @Query("SELECT * FROM training WHERE id = :id")
    override fun byId(id: Long): Training

    @Query("DELETE FROM training")
    override suspend fun deleteAll()
}

