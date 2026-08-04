package com.statsup.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.FitnessScore
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

    private val useCase = FitnessScoreUseCase()

    init {
        viewModelScope.launch {
            combine(trainingRepository.all(), weightRepository.all()) { trainings, weightEntries ->
                trainings to weightEntries
            }.collect { (trainings, weightEntries) ->
                fitnessScore = withContext(Dispatchers.Default) {
                    useCase(trainings, weightEntries, settingRepository.loadWeightTargetKg())
                }
            }
        }
    }
}
