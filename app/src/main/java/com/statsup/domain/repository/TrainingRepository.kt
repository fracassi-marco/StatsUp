package com.statsup.domain.repository

import com.statsup.domain.Training
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    fun all(): Flow<List<Training>>
    suspend fun add(training: Training): Long
    fun latest(): Training?
    fun byId(id: String): Training
    suspend fun deleteAll()
    suspend fun deleteById(id: String)
    suspend fun getAllTrainings(): List<Training>
    suspend fun updateCenter(id: String, lat: Double, lng: Double)
}
