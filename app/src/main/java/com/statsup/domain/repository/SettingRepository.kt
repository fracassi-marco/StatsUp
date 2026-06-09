package com.statsup.domain.repository

import com.statsup.domain.ExportSettings

interface SettingRepository {
    fun saveApiToken(token: String)
    fun loadApiToken(): String?
    fun saveApiRefreshToken(token: String)
    fun loadApiRefreshToken(): String?
    fun saveApiTokenExpiry(expiresAt: Long)
    fun loadApiTokenExpiry(): Long
    fun saveAthleteId(id: String)
    fun loadAthleteId(): String?
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
    fun saveHeightCm(value: Int)
    fun loadHeightCm(): Int
    fun saveWeightTargetKg(value: Double)
    fun loadWeightTargetKg(): Double
    fun exportSettings(): ExportSettings
    fun importSettings(settings: ExportSettings)
    fun clearAllSettings()
}
