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
    /**
     * Atomically replaces the entire training history with [trainings] in a single transaction.
     * Used by full import so that a failure mid-fetch never runs `deleteAll()` without a
     * complete replacement ready — the existing data stays intact until the swap is safe.
     */
    suspend fun replaceAll(trainings: List<Training>)
    suspend fun getAllTrainings(): List<Training>
    suspend fun updateCenter(id: String, lat: Double, lng: Double)
}
