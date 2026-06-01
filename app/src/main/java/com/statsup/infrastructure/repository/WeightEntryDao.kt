package com.statsup.infrastructure.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.statsup.domain.WeightEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {
    @Query("SELECT * FROM weight_entry ORDER BY date DESC")
    fun all(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entry ORDER BY date ASC")
    suspend fun getAllSync(): List<WeightEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WeightEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WeightEntry>)

    @Query("DELETE FROM weight_entry WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM weight_entry")
    suspend fun deleteAll()
}
