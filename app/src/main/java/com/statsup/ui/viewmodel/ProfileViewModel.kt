package com.statsup.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.domain.Athlete
import com.statsup.domain.Badge
import com.statsup.domain.BadgeCategory
import com.statsup.domain.BestEffort
import com.statsup.domain.EvaluateBadgesUseCase
import com.statsup.domain.PersonalRecord
import com.statsup.domain.Provider
import com.statsup.domain.Trainings
import com.statsup.domain.repository.AthleteRepository
import com.statsup.domain.repository.SettingRepository
import com.statsup.domain.repository.TrainingRepository
import com.statsup.ui.buildBadgeStringMap
import com.statsup.ui.buildPersonalRecordLabels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(
    private val trainingRepository: TrainingRepository,
    private val athleteRepository: AthleteRepository,
    private val settingRepository: SettingRepository,
    private val context: Context
) : ViewModel() {

    var athlete: Athlete? by mutableStateOf(null)
        private set

    var badges: List<Badge> by mutableStateOf(emptyList())
        private set

    var bestEfforts: List<BestEffort> by mutableStateOf(emptyList())
        private set

    var personalRecords: List<PersonalRecord> by mutableStateOf(emptyList())
        private set

    private val evaluateBadges = EvaluateBadgesUseCase()
    private val recordLabels = buildPersonalRecordLabels(context)

    init {
        viewModelScope.launch {
            athlete = withContext(Dispatchers.IO) { athleteRepository.load() }
            trainingRepository.all().collect { trainings ->
                val monthlyDistGoal = settingRepository.loadMonthlyGoal()
                val monthlyTrainGoal = settingRepository.loadMonthlyTrainingGoal()
                badges = evaluateBadges(
                    trainings = trainings,
                    monthlyDistanceGoalKm = monthlyDistGoal,
                    monthlyTrainingGoal = monthlyTrainGoal,
                    strings = buildBadgeStringMap(context, monthlyDistGoal, monthlyTrainGoal)
                )
                val t = Trainings(trainings, provider = Provider.Distance)
                bestEfforts = t.bestEfforts()
                personalRecords = t.personalRecords(recordLabels)
            }
        }
    }

    fun badgesFor(category: BadgeCategory): List<Badge> =
        badges.filter { it.category == category }

    fun earnedCount(): Int = badges.count { it.earned }
    fun totalCount(): Int = badges.size
}
