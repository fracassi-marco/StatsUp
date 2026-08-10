package com.statsup.infrastructure.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ImportResult {
    data class Success(val count: Int) : ImportResult
    data class ReimportSuccess(val trainingId: String, val peakName: String? = null) : ImportResult
    data class Error(val message: String) : ImportResult
}

data class ImportProgress(val current: Int, val total: Int)

object ImportEventBus {
    private val _result = MutableSharedFlow<ImportResult>(extraBufferCapacity = 1)
    val result = _result.asSharedFlow()

    private val _progress = MutableStateFlow<ImportProgress?>(null)
    val progress = _progress.asStateFlow()

    suspend fun emitSuccess(count: Int) { _result.emit(ImportResult.Success(count)) }
    suspend fun emitReimportSuccess(trainingId: String, peakName: String? = null) {
        _result.emit(ImportResult.ReimportSuccess(trainingId, peakName))
    }
    suspend fun emitError(message: String) { _result.emit(ImportResult.Error(message)) }

    fun emitProgress(current: Int, total: Int) { _progress.value = ImportProgress(current, total) }
    fun resetProgress() { _progress.value = null }
}
