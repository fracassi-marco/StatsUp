package com.statsup.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.statsup.domain.repository.SettingRepository


class SettingsViewModel(
    private val settingRepository: SettingRepository,
) : ViewModel() {

    var monthlyGoal by mutableIntStateOf(settingRepository.loadMonthlyGoal())
        private set
    var showMonthlyGoalSheet by mutableStateOf(false)
        private set
    var theme by mutableIntStateOf(settingRepository.loadTheme())
        private set
    var showThemeSheet by mutableStateOf(false)
        private set

    fun showMonthlyGoal() {
        showMonthlyGoalSheet = true
    }

    fun hideMonthlyGoalSheet() {
        showMonthlyGoalSheet = false
    }

    fun hideThemeSheet() {
        showThemeSheet = false
    }

    fun themeLabel(i: Int) = when (i) {
        1 -> "Light"
        2 -> "Dark"
        else -> "System"
    }

    fun themeLabel() = themeLabel(theme)

    fun showTheme() {
        showThemeSheet = true
    }

    fun theme(value: Int) {
        theme = value
    }

    fun monthlyGoal(value: Int) {
        monthlyGoal = value
    }

    fun saveMonthlyGoal() {
        settingRepository.saveMonthlyGoal(monthlyGoal)
        hideMonthlyGoalSheet()
    }

    fun saveTheme() {
        settingRepository.saveTheme(theme)
        hideThemeSheet()
    }
}
