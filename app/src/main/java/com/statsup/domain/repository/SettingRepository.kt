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
    fun saveRemindersEnabled(value: Boolean)
    fun loadRemindersEnabled(): Boolean
    /**
     * Marks a reminder kind ([ReminderEvent.dedupeKey]) as fired for a given period marker
     * (e.g. an ISO date for daily reminders, a year-month for monthly goal reminders) so
     * ReminderWorker doesn't notify about the same thing more than once per period.
     */
    fun saveReminderLastFired(key: String, periodMarker: String)
    fun loadReminderLastFired(key: String): String?
    fun exportSettings(): ExportSettings
    fun importSettings(settings: ExportSettings)
    fun clearAllSettings()
}
