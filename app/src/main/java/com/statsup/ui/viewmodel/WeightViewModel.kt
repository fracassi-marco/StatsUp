package com.statsup.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.WeightEntry
import com.statsup.domain.WeightStats
import com.statsup.domain.WeightStatsUseCase
import com.statsup.domain.repository.SettingRepository
import com.statsup.domain.repository.WeightRepository
import com.statsup.R
import com.statsup.infrastructure.service.WeightImportService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("StaticFieldLeak") // Only application context is ever stored here (see below)
class WeightViewModel(
    private val weightRepository: WeightRepository,
    private val settingRepository: SettingRepository,
    context: Context
) : ViewModel() {

    // Only the application context is retained here (never an Activity context),
    // so this ViewModel cannot leak a shorter-lived Context.
    private val context: Context = context.applicationContext

    var stats by mutableStateOf(WeightStats())
        private set

    var entries by mutableStateOf(emptyList<WeightEntry>())
        private set

    var heightCm by mutableIntStateOf(settingRepository.loadHeightCm())
        private set

    var weightTargetKg by mutableDoubleStateOf(settingRepository.loadWeightTargetKg())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isImporting by mutableStateOf(false)
        private set

    var importMessage by mutableStateOf<String?>(null)
        private set

    private val useCase = WeightStatsUseCase()

    init {
        viewModelScope.launch {
            weightRepository.all().collect { entries ->
                this@WeightViewModel.entries = entries
                stats = withContext(Dispatchers.Default) {
                    useCase(entries.sortedBy { it.date }, heightCm, weightTargetKg)
                }
                isLoading = false
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
                val parsed = withContext(Dispatchers.IO) { WeightImportService(context).parseLibraCsv(uri) }
                val existingDates = weightRepository.getAllSync().map { it.date }.toSet()
                val newEntries = parsed.filter { it.date !in existingDates }
                weightRepository.insertAll(newEntries)
                importMessage = context.resources.getQuantityString(
                    R.plurals.weight_import_success,
                    newEntries.size,
                    newEntries.size
                )
            }.onFailure {
                importMessage = context.getString(R.string.weight_import_error)
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

    /**
     * Re-reads height/weight-target from [settingRepository] after a full data import
     * replaced the underlying preferences (see [DataExportImportService.importData]) — the
     * in-memory state above would otherwise keep stale pre-import values.
     */
    fun reloadFromSettings() {
        heightCm = settingRepository.loadHeightCm()
        weightTargetKg = settingRepository.loadWeightTargetKg()
        refreshStats()
    }

    private fun refreshStats() {
        viewModelScope.launch {
            val entries = withContext(Dispatchers.IO) { weightRepository.getAllSync() }
            stats = withContext(Dispatchers.Default) {
                useCase(entries.sortedBy { it.date }, heightCm, weightTargetKg)
            }
        }
    }
}
