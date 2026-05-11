package com.statsup.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.statsup.domain.Lap
import com.statsup.domain.ManageBookmarkUseCase
import com.statsup.domain.Training
import com.statsup.domain.TrainingApi
import com.statsup.domain.repository.SettingRepository
import com.statsup.domain.repository.TrainingRepository
import com.statsup.infrastructure.repository.DbBookmarkedTrainingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrainingDetailViewModel(
    private val trainingRepository: TrainingRepository,
    bookmarkedTrainingRepository: DbBookmarkedTrainingRepository,
    private val settingRepository: SettingRepository,
    private val trainingApi: TrainingApi,
    private val trainingId: Long
) : ViewModel() {

    private val jsonMapper = jsonMapper { addModule(kotlinModule()) }.apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val _training = mutableStateOf<Training?>(null)
    val training: State<Training?> = _training

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _isBookmarked = mutableStateOf(false)
    val isBookmarked: State<Boolean> = _isBookmarked

    private val _bookmarkNote = mutableStateOf("")
    val bookmarkNote: State<String> = _bookmarkNote

    private val _customTitle = mutableStateOf("")
    val customTitle: State<String> = _customTitle

    private val _difficulty = mutableStateOf("")
    val difficulty: State<String> = _difficulty

    private val _showBookmarkDialog = mutableStateOf(false)
    val showBookmarkDialog: State<Boolean> = _showBookmarkDialog

    private val _showDeleteDialog = mutableStateOf(false)
    val showDeleteDialog: State<Boolean> = _showDeleteDialog

    private val _laps = mutableStateOf<List<Lap>>(emptyList())
    val laps: State<List<Lap>> = _laps

    private val manageBookmark = ManageBookmarkUseCase(bookmarkedTrainingRepository)

    init {
        loadTraining()
        checkBookmarkStatus()
    }

    private fun loadTraining() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val training = withContext(Dispatchers.IO) {
                    trainingRepository.byId(trainingId)
                }
                _training.value = training
                if (training.lapsJson != null) {
                    val typeRef: TypeReference<List<Lap>> = object : TypeReference<List<Lap>>() {}
                    _laps.value = jsonMapper.readValue(training.lapsJson, typeRef)
                } else {
                    fetchAndCacheLaps(training)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchAndCacheLaps(training: Training) {
        viewModelScope.launch {
            val token = getValidToken() ?: return@launch
            try {
                val fetchedLaps = withContext(Dispatchers.IO) {
                    trainingApi.laps(token, trainingId)
                }
                if (fetchedLaps.isNotEmpty()) {
                    _laps.value = fetchedLaps
                    val lapsJson = jsonMapper.writeValueAsString(fetchedLaps)
                    val updated = training.copy(lapsJson = lapsJson)
                    withContext(Dispatchers.IO) {
                        trainingRepository.add(updated)
                    }
                    _training.value = updated
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun getValidToken(): String? {
        val token = settingRepository.loadStravaToken() ?: return null
        val expiry = settingRepository.loadStravaTokenExpiry()
        val nowSecs = System.currentTimeMillis() / 1000
        if (expiry == 0L || nowSecs < expiry - 60) return token

        val savedRefreshToken = settingRepository.loadStravaRefreshToken() ?: return null
        return try {
            val newTokenData = withContext(Dispatchers.IO) {
                trainingApi.refreshToken(savedRefreshToken)
            }
            settingRepository.saveStravaToken(newTokenData.accessToken)
            settingRepository.saveStravaRefreshToken(newTokenData.refreshToken)
            settingRepository.saveStravaTokenExpiry(newTokenData.expiresAt)
            newTokenData.accessToken
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun checkBookmarkStatus() {
        viewModelScope.launch {
            try {
                val bookmark = withContext(Dispatchers.IO) {
                    manageBookmark.getBookmark(trainingId)
                }
                _isBookmarked.value = bookmark != null
                _bookmarkNote.value = bookmark?.note ?: ""
                _customTitle.value = bookmark?.customTitle ?: ""
                _difficulty.value = bookmark?.difficulty ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleBookmark() {
        _showBookmarkDialog.value = true
    }

    fun dismissBookmarkDialog() {
        _showBookmarkDialog.value = false
    }

    fun addBookmarkWithNote(note: String, customTitle: String, difficulty: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    manageBookmark.addOrUpdate(trainingId, note, customTitle, difficulty)
                }
                _isBookmarked.value = true
                _bookmarkNote.value = note
                _customTitle.value = customTitle
                _difficulty.value = difficulty
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeBookmark() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    manageBookmark.remove(trainingId)
                }
                _isBookmarked.value = false
                _bookmarkNote.value = ""
                _customTitle.value = ""
                _difficulty.value = ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun requestDeleteTraining() {
        _showDeleteDialog.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun confirmDeleteTraining(onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    manageBookmark.remove(trainingId)
                    trainingRepository.deleteById(trainingId)
                }
                onDeleted()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
