package com.statsup.infrastructure.repository

import android.content.Context
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

    private fun sharedPreferences() = context.getSharedPreferences("StatsUpPrefs", 0)
}