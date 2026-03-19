package com.statsup.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.Provider
import com.statsup.domain.Training
import com.statsup.domain.Trainings
import com.statsup.domain.repository.SettingRepository
import com.statsup.domain.repository.TrainingRepository
import com.statsup.infrastructure.service.DataExportImportService
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val settingRepository: SettingRepository,
    private val trainingRepository: TrainingRepository,
    private val dataExportImportService: DataExportImportService
) : ViewModel() {

    private var trainings: List<Training> by mutableStateOf(emptyList())

    var monthlyGoal by mutableIntStateOf(settingRepository.loadMonthlyGoal())
        private set
    var showMonthlyGoalSheet by mutableStateOf(false)
        private set
    var monthlyTrainingGoal by mutableIntStateOf(settingRepository.loadMonthlyTrainingGoal())
        private set
    var showMonthlyTrainingGoalSheet by mutableStateOf(false)
        private set
    var theme by mutableIntStateOf(settingRepository.loadTheme())
        private set
    var showThemeSheet by mutableStateOf(false)
        private set
    var autoTargets by mutableStateOf(settingRepository.loadAutoTargets())
        private set
    var showImportConfirmDialog by mutableStateOf(false)
        private set
    var importUri by mutableStateOf<Uri?>(null)
        private set
    var showFullImportFromStravaDialog by mutableStateOf(false)
        private set
    var exportImportMessage by mutableStateOf<String?>(null)
        private set
    var isExportImportLoading by mutableStateOf(false)
        private set
    var importSuccessful by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            trainingRepository.all().collect { trainings = it }
        }
    }

    fun effectiveMonthlyDistanceGoal(): Int {
        return if (autoTargets) {
            Trainings(trainings, provider = Provider.Distance)
                .autoDistanceTarget(fallbackKm = monthlyGoal)
        } else {
            monthlyGoal
        }
    }

    fun effectiveMonthlyTrainingGoal(): Int {
        return if (autoTargets) {
            Trainings(trainings, provider = Provider.Frequency)
                .autoTrainingTarget(fallbackCount = monthlyTrainingGoal)
        } else {
            monthlyTrainingGoal
        }
    }

    fun showMonthlyGoal() {
        showMonthlyGoalSheet = true
    }

    fun hideMonthlyGoalSheet() {
        showMonthlyGoalSheet = false
    }

    fun showMonthlyTrainingGoal() {
        showMonthlyTrainingGoalSheet = true
    }

    fun hideMonthlyTrainingGoalSheet() {
        showMonthlyTrainingGoalSheet = false
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

    fun monthlyTrainingGoal(value: Int) {
        monthlyTrainingGoal = value
    }

    fun toggleAutoTargets() {
        autoTargets = !autoTargets
        settingRepository.saveAutoTargets(autoTargets)
    }

    fun saveMonthlyGoal() {
        settingRepository.saveMonthlyGoal(monthlyGoal)
        hideMonthlyGoalSheet()
    }

    fun saveMonthlyTrainingGoal() {
        settingRepository.saveMonthlyTrainingGoal(monthlyTrainingGoal)
        hideMonthlyTrainingGoalSheet()
    }

    fun saveTheme() {
        settingRepository.saveTheme(theme)
        hideThemeSheet()
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            isExportImportLoading = true
            val result = dataExportImportService.exportData(uri)
            isExportImportLoading = false

            if (result.isSuccess) {
                exportImportMessage = "Data exported successfully"
            } else {
                exportImportMessage = "Export failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun showImportConfirmDialog(uri: Uri) {
        importUri = uri
        showImportConfirmDialog = true
    }

    fun hideImportConfirmDialog() {
        showImportConfirmDialog = false
        importUri = null
    }

    fun showFullImportFromStravaConfirmDialog() {
        showFullImportFromStravaDialog = true
    }

    fun hideFullImportFromStravaDialog() {
        showFullImportFromStravaDialog = false
    }

    fun confirmImport() {
        importUri?.let { uri ->
            viewModelScope.launch {
                isExportImportLoading = true
                hideImportConfirmDialog()

                val result = dataExportImportService.importData(uri)
                isExportImportLoading = false

                if (result.isSuccess) {
                    exportImportMessage = "Data imported successfully!"
                    // Reload settings
                    monthlyGoal = settingRepository.loadMonthlyGoal()
                    monthlyTrainingGoal = settingRepository.loadMonthlyTrainingGoal()
                    theme = settingRepository.loadTheme()
                    autoTargets = settingRepository.loadAutoTargets()
                    importSuccessful = true
                } else {
                    exportImportMessage = "Import failed: ${result.exceptionOrNull()?.message}"
                }
            }
        }
    }

    fun clearExportImportMessage() {
        exportImportMessage = null
    }

    fun resetImportSuccessful() {
        importSuccessful = false
    }
}

