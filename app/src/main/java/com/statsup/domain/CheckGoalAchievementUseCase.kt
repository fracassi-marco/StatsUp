package com.statsup.domain

/**
 * Determines whether a monthly goal has just been crossed (crossed the 100% threshold)
 * based on previous and current percentage values.
 *
 * Returns null if no goal was crossed, so the caller only reacts on actual achievements.
 */
class CheckGoalAchievementUseCase {

    operator fun invoke(
        previousDistance: Float,
        currentDistance: Float,
        previousTraining: Float,
        currentTraining: Float
    ): GoalAchievement? {
        val distanceJustReached = previousDistance < 1f && currentDistance >= 1f
        val trainingJustReached = previousTraining < 1f && currentTraining >= 1f

        return when {
            distanceJustReached && trainingJustReached -> GoalAchievement.BOTH
            distanceJustReached -> GoalAchievement.DISTANCE
            trainingJustReached -> GoalAchievement.TRAINING_COUNT
            else -> null
        }
    }
}
