package com.statsup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screens(val route: String, val icon: ImageVector) {
    Dashboard("dashboard", Icons.Outlined.Home),
    History("history", Icons.AutoMirrored.Outlined.List),
    Bookmarks("bookmarks", Icons.Outlined.Bookmark),
    Separator("", Icons.Filled.Edit),
    Map("map", Icons.Outlined.LocationOn),
    Stats("profile", Icons.Outlined.DateRange),
    Settings("settings", Icons.Outlined.Settings);

    companion object {
        const val TRAINING_DETAIL = "training_detail/{trainingId}"
        const val MAP_FULLSCREEN = "map_fullscreen/{trainingId}"
        const val PROFILE = "athlete_profile"
        const val LEVELS = "levels"

        fun trainingDetailRoute(trainingId: Long) = "training_detail/$trainingId"
        fun mapFullscreenRoute(trainingId: Long) = "map_fullscreen/$trainingId"
    }
}