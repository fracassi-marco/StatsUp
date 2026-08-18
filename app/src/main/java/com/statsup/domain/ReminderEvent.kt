package com.statsup.domain

/**
 * A reminder-worthy situation detected by [EvaluateRemindersUseCase], evaluated once a day in
 * the background by ReminderWorker so the user gets nudged even when the app isn't open.
 *
 * [dedupeKey] identifies the *kind* of reminder (not the specific values) so the worker can avoid
 * firing the same notification more than once within its dedupe window (a day for daily checks,
 * a month for goal checks).
 */
sealed class ReminderEvent(val dedupeKey: String) {
    data class Inactivity(val daysSinceLastTraining: Int) : ReminderEvent("inactivity")
    data class StreakAtRisk(val currentStreak: Int) : ReminderEvent("streak_risk")
    data class DistanceGoalAtRisk(val percentage: Int) : ReminderEvent("distance_goal_risk")
    data class TrainingGoalAtRisk(val percentage: Int) : ReminderEvent("training_goal_risk")
    data class DistanceGoalNearCompletion(val percentage: Int) : ReminderEvent("distance_goal_near")
    data class TrainingGoalNearCompletion(val percentage: Int) : ReminderEvent("training_goal_near")
    data class WeightReminder(val daysSinceLastEntry: Int) : ReminderEvent("weight_reminder")
}
