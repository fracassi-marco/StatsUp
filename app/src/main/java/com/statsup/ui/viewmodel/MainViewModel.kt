package com.statsup.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.UpdateTrainingsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(private val updateActivitiesUseCase: UpdateTrainingsUseCase) : ViewModel() {

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading
    val newTrainingsCounter = MutableSharedFlow<Int>()

    fun updateActivities(token: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val trainings = updateActivitiesUseCase(token)
                newTrainingsCounter.emit(trainings.count())
                stopLoading()
            }
        }
    }

    fun startLoading() {
        _loading.value = true
    }

    fun stopLoading() {
        _loading.value = false
    }
}