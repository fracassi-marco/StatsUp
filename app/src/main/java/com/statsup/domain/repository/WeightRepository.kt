package com.statsup.domain.repository

import com.statsup.domain.WeightEntry
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun all(): Flow<List<WeightEntry>>
    suspend fun getAllSync(): List<WeightEntry>
    suspend fun add(entry: WeightEntry): Long
    suspend fun insertAll(entries: List<WeightEntry>)
    suspend fun deleteById(id: Long)
    suspend fun deleteAll()
}
