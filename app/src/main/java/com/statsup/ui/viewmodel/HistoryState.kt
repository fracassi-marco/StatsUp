package com.statsup.ui.viewmodel

import com.statsup.domain.Training

data class HistoryState(
    val activities: List<Training> = emptyList(),
    val selectedSportType: String? = null // null = tutti gli sport
) {
    val show = activities.isNotEmpty()

    // Filtra gli allenamenti in base al tipo di sport selezionato
    val filteredActivities: List<Training> = if (selectedSportType == null) {
        activities
    } else {
        activities.filter { it.sportType == selectedSportType }
    }

    // Ottieni la lista di tutti i tipi di sport disponibili
    val availableSportTypes: List<String> = activities
        .mapNotNull { it.sportType }
        .distinct()
        .sorted()
}
