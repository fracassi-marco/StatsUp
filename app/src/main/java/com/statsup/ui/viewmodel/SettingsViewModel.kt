package com.statsup.ui.viewmodel

import android.app.LocaleManager
import android.content.Context
import android.net.Uri
import android.os.LocaleList
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
import com.statsup.ui.viewmodel.WeightViewModel
import kotlinx.coroutines.launch


class SettingsViewModel(
    private val settingRepository: SettingRepository,
    private val trainingRepository: TrainingRepository,
    private val dataExportImportService: DataExportImportService,
    private val context: Context
) : ViewModel() {

    private val languageTags = listOf("", "it", "en", "fr", "es")

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
    var language by mutableIntStateOf(loadCurrentLanguageIndex())
        private set
    var showLanguageSheet by mutableStateOf(false)
        private set
    var showImportConfirmDialog by mutableStateOf(false)
        private set
    var importUri by mutableStateOf<Uri?>(null)
        private set
    var showFullImportDialog by mutableStateOf(false)
        private set
    var exportImportMessage by mutableStateOf<String?>(null)
        private set
    var isExportImportLoading by mutableStateOf(false)
        private set
    var importSuccessful by mutableStateOf(false)
        private set
    var showHeightSheet by mutableStateOf(false)
        private set
    var heightCm by mutableIntStateOf(settingRepository.loadHeightCm())
        private set
    var showWeightTargetSheet by mutableStateOf(false)
        private set
    var weightTargetInt by mutableIntStateOf(settingRepository.loadWeightTargetKg().toInt().coerceAtLeast(30))
        private set
    var weightTargetDec by mutableIntStateOf(
        ((settingRepository.loadWeightTargetKg() * 10).toInt() % 10).coerceAtLeast(0)
    )
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

    private fun loadCurrentLanguageIndex(): Int {
        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        val tag = if (locales.isEmpty) "" else locales[0]?.language ?: ""
        return languageTags.indexOf(tag).coerceAtLeast(0)
    }

    fun languageLabel(index: Int, systemLabel: String) = when (index) {
        1 -> "Italiano"
        2 -> "English"
        3 -> "Français"
        4 -> "Español"
        else -> systemLabel
    }

    fun languageLabel(systemLabel: String) = languageLabel(language, systemLabel)

    fun showLanguage() {
        showLanguageSheet = true
    }

    fun hideLanguageSheet() {
        showLanguageSheet = false
    }

    fun language(value: Int) {
        language = value
    }

    fun saveLanguage() {
        val tag = languageTags.getOrElse(language) { "" }
        val locales = if (tag.isEmpty()) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(tag)
        context.getSystemService(LocaleManager::class.java).applicationLocales = locales
        hideLanguageSheet()
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

    fun showFullImportConfirmDialog() {
        showFullImportDialog = true
    }

    fun hideFullImportDialog() {
        showFullImportDialog = false
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

    fun showHeightSheet() { showHeightSheet = true }
    fun hideHeightSheet() { showHeightSheet = false }
    fun heightCm(value: Int) { heightCm = value }
    fun saveHeight(weightViewModel: WeightViewModel) {
        weightViewModel.saveHeight(heightCm)
        hideHeightSheet()
    }

    fun showWeightTargetSheet() { showWeightTargetSheet = true }
    fun hideWeightTargetSheet() { showWeightTargetSheet = false }
    fun weightTargetInt(value: Int) { weightTargetInt = value }
    fun weightTargetDec(value: Int) { weightTargetDec = value }
    fun saveWeightTarget(weightViewModel: WeightViewModel) {
        val kg = weightTargetInt + weightTargetDec / 10.0
        weightViewModel.saveWeightTarget(kg)
        hideWeightTargetSheet()
    }
}

