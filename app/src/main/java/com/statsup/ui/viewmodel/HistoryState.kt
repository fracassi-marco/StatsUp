package com.statsup.ui.viewmodel

import com.statsup.domain.Training

data class HistoryState(
    val activities: List<Training> = emptyList(),
    val selectedSportType: String? = null,
    val searchQuery: String = ""
) {
    val show = activities.isNotEmpty()

    val filteredActivities: List<Training> = activities
        .let { list ->
            if (selectedSportType == null) list
            else list.filter { it.sportType == selectedSportType }
        }
        .let { list ->
            val q = searchQuery.trim()
            if (q.isEmpty()) list
            else list.filter { training ->
                training.name.contains(q, ignoreCase = true) ||
                training.startLocationLabel?.contains(q, ignoreCase = true) == true ||
                training.endLocationLabel?.contains(q, ignoreCase = true) == true
            }
        }

    val availableSportTypes: List<String> = activities
        .mapNotNull { it.sportType }
        .distinct()
        .sorted()
}
