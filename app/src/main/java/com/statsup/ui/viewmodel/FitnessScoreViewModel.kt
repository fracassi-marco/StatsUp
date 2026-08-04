package com.statsup.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.FitnessScore
import com.statsup.domain.FitnessScoreTrendPoint
import com.statsup.domain.FitnessScoreTrendUseCase
import com.statsup.domain.FitnessScoreUseCase
import com.statsup.domain.repository.SettingRepository
import com.statsup.domain.repository.TrainingRepository
import com.statsup.domain.repository.WeightRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FitnessScoreViewModel(
    private val trainingRepository: TrainingRepository,
    private val weightRepository: WeightRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    var fitnessScore by mutableStateOf(FitnessScore())
        private set

    var fitnessScoreTrend by mutableStateOf(emptyList<FitnessScoreTrendPoint>())
        private set

    private val useCase = FitnessScoreUseCase()
    private val trendUseCase = FitnessScoreTrendUseCase(useCase)

    init {
        viewModelScope.launch {
            combine(trainingRepository.all(), weightRepository.all()) { trainings, weightEntries ->
                trainings to weightEntries
            }.collect { (trainings, weightEntries) ->
                withContext(Dispatchers.Default) {
                    val weightTargetKg = settingRepository.loadWeightTargetKg()
                    fitnessScore = useCase(trainings, weightEntries, weightTargetKg)
                    fitnessScoreTrend = trendUseCase(trainings, weightEntries, weightTargetKg)
                }
            }
        }
    }
}
