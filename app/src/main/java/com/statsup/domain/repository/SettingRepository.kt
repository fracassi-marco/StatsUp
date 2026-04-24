package com.statsup.domain.repository

import com.statsup.domain.ExportSettings

interface SettingRepository {
    fun saveStravaToken(token: String)
    fun loadStravaToken(): String?
    fun saveStravaRefreshToken(token: String)
    fun loadStravaRefreshToken(): String?
    fun saveStravaTokenExpiry(expiresAt: Long)
    fun loadStravaTokenExpiry(): Long
    fun saveTheme(value: Int)
    fun saveMonthlyGoal(value: Int)
    fun saveMonthlyTrainingGoal(value: Int)
    fun saveAutoTargets(value: Boolean)
    fun saveLastSuggestedYearMonth(value: String)
    fun loadTheme(): Int
    fun loadMonthlyGoal(): Int
    fun loadMonthlyTrainingGoal(): Int
    fun loadAutoTargets(): Boolean
    fun loadLastSuggestedYearMonth(): String
    fun exportSettings(): ExportSettings
    fun importSettings(settings: ExportSettings)
    fun clearAllSettings()
}
