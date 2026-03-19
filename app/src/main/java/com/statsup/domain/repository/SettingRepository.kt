package com.statsup.domain.repository

import com.statsup.domain.ExportSettings

interface SettingRepository {
    fun saveTheme(value: Int)
    fun saveMonthlyGoal(value: Int)
    fun saveMonthlyTrainingGoal(value: Int)
    fun saveAutoTargets(value: Boolean)
    fun loadTheme(): Int
    fun loadMonthlyGoal(): Int
    fun loadMonthlyTrainingGoal(): Int
    fun loadAutoTargets(): Boolean
    fun exportSettings(): ExportSettings
    fun importSettings(settings: ExportSettings)
    fun clearAllSettings()
}
