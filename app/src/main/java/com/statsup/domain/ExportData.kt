package com.statsup.domain

/**
 * Full snapshot of every piece of user data StatsUp stores locally, used by the
 * compact text export/import (see [StatsUpExportFormat]). An import always replaces
 * the whole local state with the content of this snapshot — there is no merge.
 */
data class ExportData(
    val exportDate: Long = System.currentTimeMillis(),
    val trainings: List<Training>,
    val bookmarkedTrainings: List<BookmarkedTraining>,
    val athlete: Athlete?,
    val settings: ExportSettings,
    val weightEntries: List<WeightEntry> = emptyList()
)

data class ExportSettings(
    val theme: Int,
    val monthlyGoal: Int,
    val monthlyTrainingGoal: Int,
    val autoTargets: Boolean = false,
    val remindersEnabled: Boolean = true,
    val heightCm: Int = 0,
    val weightTargetKg: Double = 0.0
)
