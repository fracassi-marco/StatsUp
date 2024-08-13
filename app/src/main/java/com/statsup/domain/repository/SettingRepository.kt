package com.statsup.domain.repository

interface SettingRepository {
    fun saveTheme(value: Int)
    fun saveMonthlyGoal(value: Int)
    fun loadTheme(): Int
    fun loadMonthlyGoal(): Int
}