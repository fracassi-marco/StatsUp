package com.statsup.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.Training
import com.statsup.domain.repository.TrainingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrainingDetailViewModel(
    private val trainingRepository: TrainingRepository,
    private val trainingId: Long
) : ViewModel() {

    private val _training = mutableStateOf<Training?>(null)
    val training: State<Training?> = _training

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadTraining()
    }

    private fun loadTraining() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _training.value = withContext(Dispatchers.IO) {
                    trainingRepository.byId(trainingId)
                }
            } catch (e: Exception) {
                // Log the error but don't crash
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

