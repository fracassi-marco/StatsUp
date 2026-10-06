package com.statsup.infrastructure.repository

import android.content.Context
import androidx.core.content.edit
import com.statsup.domain.ExportSettings
import com.statsup.domain.repository.SettingRepository
import java.time.LocalDate

class SharedPreferencesSettingRepository(private val context: Context) : SettingRepository {
    override fun saveApiToken(token: String) {
        sharedPreferences().edit { putString("api.access_token", token) }
    }

    override fun loadApiToken(): String? {
        return sharedPreferences().getString("api.access_token", null)
    }

    override fun saveApiRefreshToken(token: String) {
        sharedPreferences().edit { putString("api.refresh_token", token) }
    }

    override fun loadApiRefreshToken(): String? {
        return sharedPreferences().getString("api.refresh_token", null)
    }

    override fun saveApiTokenExpiry(expiresAt: Long) {
        sharedPreferences().edit { putLong("api.token_expiry", expiresAt) }
    }

    override fun loadApiTokenExpiry(): Long {
        return sharedPreferences().getLong("api.token_expiry", 0L)
    }

    override fun saveAthleteId(id: String) {
        sharedPreferences().edit { putString("api.athlete_id", id) }
    }

    override fun loadAthleteId(): String? {
        return sharedPreferences().getString("api.athlete_id", null)
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

    override fun saveWeightTargetDate(value: LocalDate?) {
        saveEpochDay("settings.weightTargetDate", value)
    }

    override fun loadWeightTargetDate(): LocalDate? = loadEpochDay("settings.weightTargetDate")

    override fun saveWeightPlanStartDate(value: LocalDate?) {
        saveEpochDay("settings.weightPlanStartDate", value)
    }

    override fun loadWeightPlanStartDate(): LocalDate? = loadEpochDay("settings.weightPlanStartDate")

    private fun saveEpochDay(key: String, value: LocalDate?) {
        sharedPreferences().edit {
            if (value == null) remove(key) else putLong(key, value.toEpochDay())
        }
    }

    private fun loadEpochDay(key: String): LocalDate? {
        val prefs = sharedPreferences()
        return if (prefs.contains(key)) LocalDate.ofEpochDay(prefs.getLong(key, 0L)) else null
    }

    override fun saveRemindersEnabled(value: Boolean) {
        sharedPreferences().edit { putBoolean("settings.remindersEnabled", value) }
    }

    override fun loadRemindersEnabled(): Boolean {
        return sharedPreferences().getBoolean("settings.remindersEnabled", true)
    }

    override fun saveReminderLastFired(key: String, periodMarker: String) {
        sharedPreferences().edit { putString("reminder.lastFired.$key", periodMarker) }
    }

    override fun loadReminderLastFired(key: String): String? {
        return sharedPreferences().getString("reminder.lastFired.$key", null)
    }

    override fun exportSettings(): ExportSettings {
        return ExportSettings(
            theme = loadTheme(),
            monthlyGoal = loadMonthlyGoal(),
            monthlyTrainingGoal = loadMonthlyTrainingGoal(),
            autoTargets = loadAutoTargets(),
            remindersEnabled = loadRemindersEnabled(),
            heightCm = loadHeightCm(),
            weightTargetKg = loadWeightTargetKg(),
            weightTargetDateEpochDay = loadWeightTargetDate()?.toEpochDay(),
            weightPlanStartEpochDay = loadWeightPlanStartDate()?.toEpochDay()
        )
    }

    override fun importSettings(settings: ExportSettings) {
        saveTheme(settings.theme)
        saveMonthlyGoal(settings.monthlyGoal)
        saveMonthlyTrainingGoal(settings.monthlyTrainingGoal)
        saveAutoTargets(settings.autoTargets)
        saveRemindersEnabled(settings.remindersEnabled)
        saveHeightCm(settings.heightCm)
        saveWeightTargetKg(settings.weightTargetKg)
        saveWeightTargetDate(settings.weightTargetDateEpochDay?.let(LocalDate::ofEpochDay))
        saveWeightPlanStartDate(settings.weightPlanStartEpochDay?.let(LocalDate::ofEpochDay))
    }

    override fun clearAllSettings() {
        sharedPreferences().edit { clear() }
    }

    private fun sharedPreferences() = context.getSharedPreferences("StatsUpPrefs", 0)
}
