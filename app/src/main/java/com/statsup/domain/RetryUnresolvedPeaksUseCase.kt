package com.statsup.domain

import android.util.Log
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.statsup.domain.repository.PeakLookupRepository
import com.statsup.domain.repository.TrainingRepository

/**
 * Re-attempts peak resolution for trainings a previous import left with `peakName == null`
 * (Overpass lookup failed, see [resolvePeak]) — otherwise they stay unresolved forever, since
 * [UpdateTrainingsUseCase] only ever processes trainings newer than the latest stored one.
 */
class RetryUnresolvedPeaksUseCase(
    private val trainingRepository: TrainingRepository,
    private val peakLookupRepository: PeakLookupRepository?
) {
    private val jsonMapper = jsonMapper { addModule(kotlinModule()) }

    suspend operator fun invoke(): Int {
        if (peakLookupRepository == null) return 0
        var resolvedCount = 0
        for (training in trainingRepository.unresolvedPeakCandidates()) {
            val elevPoints = training.elevationPointsJson?.let { decodeElevationPoints(training.id, it) }
            val retried = resolvePeak(training, elevPoints, peakLookupRepository, existing = training)
            if (retried.peakName != null) {
                trainingRepository.add(retried)
                resolvedCount++
            }
        }
        return resolvedCount
    }

    private fun decodeElevationPoints(trainingId: String, json: String): List<Double>? = try {
        val listType = jsonMapper.typeFactory.constructCollectionType(List::class.java, Double::class.java)
        jsonMapper.readValue<List<Double>>(json, listType)
    } catch (e: Exception) {
        Log.w("RetryUnresolvedPeaksUseCase", "Failed to decode elevationPointsJson for training=$trainingId", e)
        null
    }
}
