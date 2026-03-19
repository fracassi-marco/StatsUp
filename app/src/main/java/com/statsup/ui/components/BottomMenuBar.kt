package com.statsup.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.statsup.Screens

@Composable
fun BottomMenuBar(navController: NavHostController, enabled: Boolean = true) {
    val unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    androidx.compose.material3.NavigationBar(
        modifier = Modifier.border(1.dp, color = MaterialTheme.colorScheme.outlineVariant),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        Screens.entries.forEach { screen ->
            if (screen == Screens.Separator) {
                // Spazio più largo per il FAB centrale
                Box(modifier = Modifier.size(90.dp, 1.dp))
                return@forEach
            }
            NavigationBarItem(
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp) // Icone più grandi
                    )
                },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                enabled = enabled,
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = if (enabled) unselectedColor else unselectedColor.copy(alpha = 0.38f),
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.background,
                    disabledIconColor = unselectedColor.copy(alpha = 0.38f)
                ),
                onClick = {
                    if (enabled) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}