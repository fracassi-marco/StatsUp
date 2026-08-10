package com.statsup.infrastructure.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface ImportResult {
    data class Success(val count: Int) : ImportResult
    data class ReimportSuccess(val trainingId: String, val peakName: String? = null) : ImportResult
    data class MissingPeaksResolved(val count: Int) : ImportResult
    data class Error(val message: String) : ImportResult
}

object ImportEventBus {
    private val _result = MutableSharedFlow<ImportResult>(extraBufferCapacity = 1)
    val result = _result.asSharedFlow()

    suspend fun emitSuccess(count: Int) { _result.emit(ImportResult.Success(count)) }
    suspend fun emitReimportSuccess(trainingId: String, peakName: String? = null) {
        _result.emit(ImportResult.ReimportSuccess(trainingId, peakName))
    }
    suspend fun emitMissingPeaksResolved(count: Int) { _result.emit(ImportResult.MissingPeaksResolved(count)) }
    suspend fun emitError(message: String) { _result.emit(ImportResult.Error(message)) }
}
