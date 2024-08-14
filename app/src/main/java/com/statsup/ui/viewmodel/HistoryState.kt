package com.statsup.ui.viewmodel

import com.statsup.domain.Training

data class HistoryState(val activities: List<Training> = emptyList()) {
    val show = activities.isNotEmpty()
}
