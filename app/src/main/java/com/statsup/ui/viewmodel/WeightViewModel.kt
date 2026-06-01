package com.statsup.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.WeightEntry
import com.statsup.domain.WeightStats
import com.statsup.domain.WeightStatsUseCase
import com.statsup.domain.repository.SettingRepository
import com.statsup.domain.repository.WeightRepository
import com.statsup.infrastructure.service.WeightImportService
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WeightViewModel(
    private val weightRepository: WeightRepository,
    private val settingRepository: SettingRepository,
    private val context: Context
) : ViewModel() {

    var stats by mutableStateOf(WeightStats())
        private set

    var heightCm by mutableStateOf(settingRepository.loadHeightCm())
        private set

    var weightTargetKg by mutableStateOf(settingRepository.loadWeightTargetKg())
        private set

    var isImporting by mutableStateOf(false)
        private set

    var importMessage by mutableStateOf<String?>(null)
        private set

    private val useCase = WeightStatsUseCase()

    init {
        viewModelScope.launch {
            weightRepository.all().collect { entries ->
                stats = useCase(entries.sortedBy { it.date }, heightCm, weightTargetKg)
            }
        }
    }

    fun addWeight(kg: Double, date: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            weightRepository.add(WeightEntry(date = date, weightKg = kg))
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            weightRepository.deleteById(id)
        }
    }

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            isImporting = true
            runCatching {
                val entries = WeightImportService(context).parseLibraCsv(uri)
                weightRepository.insertAll(entries)
                importMessage = "Importate ${entries.size} pesate"
            }.onFailure {
                importMessage = "Errore import: ${it.message}"
            }
            isImporting = false
        }
    }

    fun clearImportMessage() {
        importMessage = null
    }

    fun saveHeight(cm: Int) {
        heightCm = cm
        settingRepository.saveHeightCm(cm)
        refreshStats()
    }

    fun saveWeightTarget(kg: Double) {
        weightTargetKg = kg
        settingRepository.saveWeightTargetKg(kg)
        refreshStats()
    }

    private fun refreshStats() {
        viewModelScope.launch {
            val entries = weightRepository.getAllSync()
            stats = useCase(entries.sortedBy { it.date }, heightCm, weightTargetKg)
        }
    }
}
