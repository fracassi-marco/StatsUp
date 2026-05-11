package com.statsup.domain.repository

import com.statsup.domain.Training
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    fun all(): Flow<List<Training>>
    suspend fun add(training: Training): Long
    fun latest(): Training?
    fun byId(id: Long): Training
    suspend fun deleteAll()
    suspend fun deleteById(id: Long)
    suspend fun getAllTrainings(): List<Training>
    suspend fun updateCenter(id: Long, lat: Double, lng: Double)
}
