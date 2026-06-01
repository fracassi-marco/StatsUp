package com.statsup.infrastructure.repository

import com.statsup.domain.WeightEntry
import com.statsup.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow

class DbWeightRepository(private val dao: WeightEntryDao) : WeightRepository {
    override fun all(): Flow<List<WeightEntry>> = dao.all()
    override suspend fun getAllSync(): List<WeightEntry> = dao.getAllSync()
    override suspend fun add(entry: WeightEntry): Long = dao.insert(entry)
    override suspend fun insertAll(entries: List<WeightEntry>) = dao.insertAll(entries)
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
    override suspend fun deleteAll() = dao.deleteAll()
}
