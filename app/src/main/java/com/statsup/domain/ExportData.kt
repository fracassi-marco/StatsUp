package com.statsup.domain

import com.google.gson.annotations.SerializedName

data class ExportData(
    @SerializedName("version")
    val version: Int = 1,

    @SerializedName("exportDate")
    val exportDate: Long = System.currentTimeMillis(),

    @SerializedName("trainings")
    val trainings: List<Training>,

    @SerializedName("bookmarkedTrainings")
    val bookmarkedTrainings: List<BookmarkedTraining>,

    @SerializedName("athlete")
    val athlete: Athlete?,

    @SerializedName("settings")
    val settings: ExportSettings
)

data class ExportSettings(
    @SerializedName("theme")
    val theme: Int,

    @SerializedName("monthlyGoal")
    val monthlyGoal: Int,

    @SerializedName("monthlyTrainingGoal")
    val monthlyTrainingGoal: Int,

    @SerializedName("autoTargets")
    val autoTargets: Boolean = false
)

