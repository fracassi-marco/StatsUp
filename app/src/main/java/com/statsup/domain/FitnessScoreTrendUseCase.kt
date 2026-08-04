package com.statsup.domain

import java.time.ZonedDateTime

/**
 * Backfills the Fitness Score for each of the last [WINDOW_DAYS] days by re-running
 * [FitnessScoreUseCase] with `now` pinned to that day and only the data available up to
 * that point — cheap enough to compute on demand given typical personal training history
 * sizes, so no persisted daily snapshot is needed.
 */
class FitnessScoreTrendUseCase(
    private val fitnessScoreUseCase: FitnessScoreUseCase = FitnessScoreUseCase()
) {

    operator fun invoke(
        trainings: List<Training>,
        weightEntries: List<WeightEntry>,
        weightTargetKg: Double,
        now: ZonedDateTime = ZonedDateTime.now()
    ): List<FitnessScoreTrendPoint> {
        return (WINDOW_DAYS - 1 downTo 0).map { daysAgo ->
            val day = now.minusDays(daysAgo.toLong())
            val dayEndMillis = day.toInstant().toEpochMilli()
            val trainingsUpToDay = trainings.filter { !it.date.isAfter(day) }
            val weightEntriesUpToDay = weightEntries.filter { it.date <= dayEndMillis }
            val score = fitnessScoreUseCase(trainingsUpToDay, weightEntriesUpToDay, weightTargetKg, day).score
            FitnessScoreTrendPoint(day, score)
        }
    }

    companion object {
        private const val WINDOW_DAYS = 30
    }
}
