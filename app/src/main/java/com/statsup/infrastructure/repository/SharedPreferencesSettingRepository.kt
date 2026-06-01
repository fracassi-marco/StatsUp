package com.statsup.infrastructure.repository

import android.content.Context
import androidx.core.content.edit
import com.statsup.domain.ExportSettings
import com.statsup.domain.repository.SettingRepository

class SharedPreferencesSettingRepository(private val context: Context) : SettingRepository {
    override fun saveStravaToken(token: String) {
        sharedPreferences().edit { putString("strava.access_token", token) }
    }

    override fun loadStravaToken(): String? {
        return sharedPreferences().getString("strava.access_token", null)
    }

    override fun saveStravaRefreshToken(token: String) {
        sharedPreferences().edit { putString("strava.refresh_token", token) }
    }

    override fun loadStravaRefreshToken(): String? {
        return sharedPreferences().getString("strava.refresh_token", null)
    }

    override fun saveStravaTokenExpiry(expiresAt: Long) {
        sharedPreferences().edit { putLong("strava.token_expiry", expiresAt) }
    }

    override fun loadStravaTokenExpiry(): Long {
        return sharedPreferences().getLong("strava.token_expiry", 0L)
    }

    override fun saveTheme(value: Int) {
        sharedPreferences().edit { putInt("settings.theme", value) }
    }

    override fun saveMonthlyGoal(value: Int) {
        sharedPreferences().edit { putInt("settings.monthlyGoal", value) }
    }

    override fun saveMonthlyTrainingGoal(value: Int) {
        sharedPreferences().edit { putInt("settings.monthlyTrainingGoal", value) }
    }

    override fun saveAutoTargets(value: Boolean) {
        sharedPreferences().edit { putBoolean("settings.autoTargets", value) }
    }

    override fun saveLastSuggestedYearMonth(value: String) {
        sharedPreferences().edit { putString("settings.lastSuggestedYearMonth", value) }
    }

    override fun loadAutoTargets(): Boolean {
        return sharedPreferences().getBoolean("settings.autoTargets", false)
    }

    override fun loadLastSuggestedYearMonth(): String {
        return sharedPreferences().getString("settings.lastSuggestedYearMonth", "") ?: ""
    }

    override fun loadTheme(): Int {
        return sharedPreferences().getInt("settings.theme", 0)
    }

    override fun loadMonthlyGoal(): Int {
        return sharedPreferences().getInt("settings.monthlyGoal", 10)
    }

    override fun loadMonthlyTrainingGoal(): Int {
        return sharedPreferences().getInt("settings.monthlyTrainingGoal", 12)
    }

    override fun saveHeightCm(value: Int) {
        sharedPreferences().edit { putInt("settings.heightCm", value) }
    }

    override fun loadHeightCm(): Int {
        return sharedPreferences().getInt("settings.heightCm", 0)
    }

    override fun saveWeightTargetKg(value: Double) {
        sharedPreferences().edit { putFloat("settings.weightTargetKg", value.toFloat()) }
    }

    override fun loadWeightTargetKg(): Double {
        return sharedPreferences().getFloat("settings.weightTargetKg", 0f).toDouble()
    }

    override fun exportSettings(): ExportSettings {
        return ExportSettings(
            theme = loadTheme(),
            monthlyGoal = loadMonthlyGoal(),
            monthlyTrainingGoal = loadMonthlyTrainingGoal(),
            autoTargets = loadAutoTargets()
        )
    }

    override fun importSettings(settings: ExportSettings) {
        saveTheme(settings.theme)
        saveMonthlyGoal(settings.monthlyGoal)
        saveMonthlyTrainingGoal(settings.monthlyTrainingGoal)
        saveAutoTargets(settings.autoTargets)
    }

    override fun clearAllSettings() {
        sharedPreferences().edit { clear() }
    }

    private fun sharedPreferences() = context.getSharedPreferences("StatsUpPrefs", 0)
}
