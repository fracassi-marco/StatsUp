package com.statsup.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.statsup.ui.viewmodel.SettingsViewModel

private val DarkColorScheme = darkColorScheme(
    primary = LightPrimary,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkBackground,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = LightBackground,
    surfaceTint = Color(0xFFF5E1D8)
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun StatsUpTheme(
    settingsViewModel: SettingsViewModel,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when(settingsViewModel.theme) {
        1 -> LightColorScheme
        2 -> DarkColorScheme
        else -> when {
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}