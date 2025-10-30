package com.statsup.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statsup.R
import com.statsup.domain.Measure
import com.statsup.domain.Training
import com.statsup.domain.formatLocal
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingDetailScreen(
    training: Training?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onOpenFullscreenMap: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = training?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            LoadingBox(isLoading = true) { }
        } else if (training != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // Sfondo con immagine a tema basata sul tipo di attività
                Image(
                    painter = painterResource(id = getActivityBackground(training.sportType)),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(
                        color = Color.Black.copy(alpha = 0.3f),
                        blendMode = androidx.compose.ui.graphics.BlendMode.Darken
                    )
                )

                // Box con le informazioni in basso
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pulsante per aprire la mappa a schermo intero
                    if (training.trip != null) {
                        TextButton(
                            onClick = onOpenFullscreenMap,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.ZoomOutMap, contentDescription = "Full screen map")
                            Text(
                                text = stringResource(id = R.string.view_fullscreen_map),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors()
                            .copy(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = training.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatLocal(training.date),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            // Sezione Distanza & Tempo
                            if (training.distance > 0 || training.movingTime > 0) {
                                StatSection(
                                    title = stringResource(id = R.string.distance_and_time),
                                    icon = Icons.AutoMirrored.Filled.DirectionsRun
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    if (training.distance > 0) {
                                        StatItemWithIcon(
                                            icon = Icons.Default.Route,
                                            label = stringResource(id = R.string.distance),
                                            value = String.format(Locale.getDefault(), "%.2f", training.distanceInKilometers()),
                                            unit = stringResource(id = R.string.km),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (training.movingTime > 0) {
                                        StatItemWithIcon(
                                            icon = Icons.Default.AccessTime,
                                            label = stringResource(id = R.string.duration),
                                            value = Measure.hm(training.movingTime),
                                            unit = "",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // Sezione Altimetria
                            val hasElevationData = training.totalElevationGain > 0 ||
                                                   training.elevHigh > 0 ||
                                                   training.elevLow > 0

                            if (hasElevationData) {
                                if (training.distance > 0 || training.movingTime > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                }

                                StatSection(
                                    title = stringResource(id = R.string.elevation),
                                    icon = Icons.Default.Landscape
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        if (training.totalElevationGain > 0) {
                                            StatItemWithIcon(
                                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                                label = stringResource(id = R.string.elevation_gain),
                                                value = String.format(Locale.getDefault(), "%.0f", training.totalElevationGain),
                                                unit = stringResource(id = R.string.m),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        if (training.elevationPerKm() > 0) {
                                            StatItemWithIcon(
                                                icon = Icons.Default.Terrain,
                                                label = stringResource(id = R.string.elevation_per_km),
                                                value = String.format(Locale.getDefault(), "%.1f", training.elevationPerKm()),
                                                unit = stringResource(id = R.string.m_per_km),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        if (training.elevHigh > 0) {
                                            StatItemWithIcon(
                                                icon = Icons.Default.Landscape,
                                                label = stringResource(id = R.string.max_altitude),
                                                value = String.format(Locale.getDefault(), "%.0f", training.elevHigh),
                                                unit = stringResource(id = R.string.m),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        if (training.elevLow > 0 || training.elevHigh > 0) {
                                            StatItemWithIcon(
                                                icon = Icons.Default.Landscape,
                                                label = stringResource(id = R.string.min_altitude),
                                                value = String.format(Locale.getDefault(), "%.0f", training.elevLow),
                                                unit = stringResource(id = R.string.m),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }

                            // Sezione Performance
                            val hasPerformanceData = training.averagePace() > 0 || training.vam() > 0

                            if (hasPerformanceData) {
                                val hasPreviousSections = (training.distance > 0 || training.movingTime > 0) || hasElevationData

                                if (hasPreviousSections) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                                }

                                StatSection(
                                    title = stringResource(id = R.string.performance),
                                    icon = Icons.Default.Speed
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    if (training.averagePace() > 0) {
                                        StatItemWithIcon(
                                            icon = Icons.Default.Speed,
                                            label = stringResource(id = R.string.average_pace),
                                            value = formatPaceFromMinutes(training.averagePace()),
                                            unit = stringResource(id = R.string.pace_unit),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (training.vam() > 0) {
                                        StatItemWithIcon(
                                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                                            label = stringResource(id = R.string.vam),
                                            value = String.format(Locale.getDefault(), "%.0f", training.vam()),
                                            unit = stringResource(id = R.string.m_per_hour),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun StatItemWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.sp
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = " $unit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 2.dp, bottom = 1.dp)
                    )
                }
            }
        }
    }
}


private fun formatPaceFromMinutes(paceInMinutes: Double): String {
    if (paceInMinutes == 0.0) return "0:00"

    val minutes = paceInMinutes.toInt()
    val seconds = ((paceInMinutes - minutes) * 60).toInt()

    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}

private fun getActivityBackground(sportType: String?): Int {
    return when (sportType?.lowercase()) {
        "run", "running" -> R.drawable.bg_running
        "ride", "cycling", "virtualride" -> R.drawable.bg_cycling
        "hike", "hiking" -> R.drawable.bg_hiking
        "walk", "walking" -> R.drawable.bg_walking
        "swim", "swimming" -> R.drawable.bg_swimming
        "workout", "crossfit" -> R.drawable.bg_workout
        "yoga" -> R.drawable.bg_yoga
        "ski", "skiing", "alpineski", "backcountryski", "nordicski" -> R.drawable.bg_skiing
        else -> R.drawable.bg_default
    }
}

