package com.statsup.domain

import java.time.LocalDate

data class LevelXpTrendPoint(
    val date: LocalDate,
    val xp: Int
)

/**
 * Backfills the total Level XP for each of the last [WINDOW_DAYS] days by re-running
 * [EvaluateLevelUseCase] with `now` pinned to that day and only the trainings available up to
 * that point — mirrors [FitnessScoreTrendUseCase], cheap enough to compute on demand.
 */
class LevelXpTrendUseCase(
    private val evaluateLevel: EvaluateLevelUseCase = EvaluateLevelUseCase()
) {

    operator fun invoke(
        trainings: List<Training>,
        now: LocalDate = LocalDate.now()
    ): List<LevelXpTrendPoint> {
        return (WINDOW_DAYS - 1 downTo 0).map { daysAgo ->
            val day = now.minusDays(daysAgo.toLong())
            val trainingsUpToDay = trainings.filter { !it.date.toLocalDate().isAfter(day) }
            val totalXp = evaluateLevel(trainingsUpToDay, day).totalXp
            LevelXpTrendPoint(day, totalXp)
        }
    }

    companion object {
        private const val WINDOW_DAYS = 30
    }
}
