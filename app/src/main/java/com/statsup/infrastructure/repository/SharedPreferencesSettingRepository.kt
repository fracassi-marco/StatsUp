package com.statsup.infrastructure.repository

import android.content.Context
import com.statsup.domain.ExportSettings
import com.statsup.domain.repository.SettingRepository

class SharedPreferencesSettingRepository(private val context: Context) : SettingRepository {
    override fun saveTheme(value: Int) {
        val editor = sharedPreferences().edit()
        editor.putInt("settings.theme", value)
        editor.apply()
    }

    override fun saveMonthlyGoal(value: Int) {
        val editor = sharedPreferences().edit()
        editor.putInt("settings.monthlyGoal", value)
        editor.apply()
    }

    override fun saveMonthlyTrainingGoal(value: Int) {
        val editor = sharedPreferences().edit()
        editor.putInt("settings.monthlyTrainingGoal", value)
        editor.apply()
    }

    override fun loadTheme(): Int {
        val sp = sharedPreferences()
        val value = sp.getInt("settings.theme", 0)
        return value
    }

    override fun loadMonthlyGoal(): Int {
        val sp = sharedPreferences()
        val value = sp.getInt("settings.monthlyGoal", 10)
        return value
    }

    override fun loadMonthlyTrainingGoal(): Int {
        val sp = sharedPreferences()
        val value = sp.getInt("settings.monthlyTrainingGoal", 12)
        return value
    }

    override fun exportSettings(): ExportSettings {
        return ExportSettings(
            theme = loadTheme(),
            monthlyGoal = loadMonthlyGoal(),
            monthlyTrainingGoal = loadMonthlyTrainingGoal()
        )
    }

    override fun importSettings(settings: ExportSettings) {
        saveTheme(settings.theme)
        saveMonthlyGoal(settings.monthlyGoal)
        saveMonthlyTrainingGoal(settings.monthlyTrainingGoal)
    }

    override fun clearAllSettings() {
        val editor = sharedPreferences().edit()
        editor.clear()
        editor.apply()
    }

    private fun sharedPreferences() = context.getSharedPreferences("StatsUpPrefs", 0)
}