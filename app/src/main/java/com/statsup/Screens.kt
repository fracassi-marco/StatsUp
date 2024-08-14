package com.statsup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screens(val route: String, val icon: ImageVector) {
    Dashboard("dashboard", Icons.Outlined.Home),
    History("history", Icons.AutoMirrored.Outlined.List),
    Separator("", Icons.Filled.Edit),
    Stats("profile", Icons.Outlined.DateRange),
    Settings("settings", Icons.Outlined.Settings)
}