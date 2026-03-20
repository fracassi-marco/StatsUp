package com.statsup.domain

import java.time.ZonedDateTime

/**
 * Computes suggested monthly targets based on historical training data using the auto-target
 * algorithm from [Trainings.autoDistanceTarget] and [Trainings.autoTrainingTarget].
 *
 * Returns null when the computed suggestion matches the current manual targets (nothing new to suggest).
 *
 * @param now Injection point for the current date/time — override in tests to keep results deterministic.
 */
class SuggestAutoTargetsUseCase {

    operator fun invoke(
        trainings: List<Training>,
        currentDistanceGoalKm: Int,
        currentTrainingGoal: Int,
        now: ZonedDateTime = ZonedDateTime.now()
    ): TargetSuggestion? {
        val suggestedDistance = Trainings(trainings, provider = Provider.Distance, now = now)
            .autoDistanceTarget(fallbackKm = currentDistanceGoalKm)
        val suggestedTraining = Trainings(trainings, provider = Provider.Frequency, now = now)
            .autoTrainingTarget(fallbackCount = currentTrainingGoal)

        val distanceDiffers = suggestedDistance != currentDistanceGoalKm
        val trainingDiffers = suggestedTraining != currentTrainingGoal

        return if (distanceDiffers || trainingDiffers) {
            TargetSuggestion(suggestedDistance, suggestedTraining)
        } else null
    }
}
