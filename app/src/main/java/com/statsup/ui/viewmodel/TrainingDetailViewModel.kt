package com.statsup.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.ManageBookmarkUseCase
import com.statsup.domain.Training
import com.statsup.domain.repository.TrainingRepository
import com.statsup.infrastructure.repository.DbBookmarkedTrainingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrainingDetailViewModel(
    private val trainingRepository: TrainingRepository,
    private val bookmarkedTrainingRepository: DbBookmarkedTrainingRepository,
    private val trainingId: Long
) : ViewModel() {

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

    private val manageBookmark = ManageBookmarkUseCase(bookmarkedTrainingRepository)

    init {
        loadTraining()
        checkBookmarkStatus()
    }

    private fun loadTraining() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _training.value = withContext(Dispatchers.IO) {
                    trainingRepository.byId(trainingId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
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
}
