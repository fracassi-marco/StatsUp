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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.statsup.domain.SportTypeFormatter
import com.statsup.domain.Training
import com.statsup.domain.formatLocal
import com.statsup.ui.theme.DifficultyEasy
import com.statsup.ui.theme.DifficultyHard
import com.statsup.ui.theme.DifficultyMedium
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingDetailScreen(
    training: Training?,
    isLoading: Boolean,
    isBookmarked: Boolean,
    bookmarkNote: String,
    customTitle: String,
    difficulty: String,
    showBookmarkDialog: Boolean,
    onNavigateBack: () -> Unit,
    onOpenFullscreenMap: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmBookmark: (String, String, String) -> Unit,
    onRemoveBookmark: () -> Unit
) {
    if (isLoading) {
        LoadingBox(isLoading = true) { }
    } else if (training != null) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    Button(
                        onClick = onOpenFullscreenMap,
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(6.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 3.dp,
                            pressedElevation = 6.dp
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.ZoomOutMap,
                            contentDescription = "Full screen map",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.padding(3.dp))
                        Text(
                            text = stringResource(id = R.string.view_fullscreen_map),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
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
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Titolo con emoji del tipo di sport
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = SportTypeFormatter.getEmojiForSportType(training.sportType),
                                fontSize = 28.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (customTitle.isNotEmpty()) customTitle else training.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                // Mostra badge difficoltà se presente
                                if (difficulty.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    DifficultyBadge(difficulty = difficulty)
                                }
                            }
                        }
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
                                        value = String.format(
                                            Locale.getDefault(),
                                            "%.2f",
                                            training.distanceInKilometers()
                                        ),
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
                                            value = String.format(
                                                Locale.getDefault(),
                                                "%.0f",
                                                training.totalElevationGain
                                            ),
                                            unit = stringResource(id = R.string.m),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    if (training.elevationPerKm() > 0) {
                                        StatItemWithIcon(
                                            icon = Icons.Default.Terrain,
                                            label = stringResource(id = R.string.elevation_per_km),
                                            value = String.format(
                                                Locale.getDefault(),
                                                "%.1f",
                                                training.elevationPerKm()
                                            ),
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
                                            value = String.format(
                                                Locale.getDefault(),
                                                "%.0f",
                                                training.elevHigh
                                            ),
                                            unit = stringResource(id = R.string.m),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    if (training.elevLow > 0 || training.elevHigh > 0) {
                                        StatItemWithIcon(
                                            icon = Icons.Default.Landscape,
                                            label = stringResource(id = R.string.min_altitude),
                                            value = String.format(
                                                Locale.getDefault(),
                                                "%.0f",
                                                training.elevLow
                                            ),
                                            unit = stringResource(id = R.string.m),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        // Sezione Performance
                        val hasPerformanceData = training.averagePace() > 0 ||
                                training.vam() > 0 ||
                                training.averageSpeedKmh() > 0 ||
                                (training.averageHeartrate != null && training.averageHeartrate!! > 0)

                        if (hasPerformanceData) {
                            val hasPreviousSections =
                                (training.distance > 0 || training.movingTime > 0) || hasElevationData

                            if (hasPreviousSections) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            }

                            StatSection(
                                title = stringResource(id = R.string.performance),
                                icon = Icons.Default.Speed
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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
                                    if (training.averageSpeedKmh() > 0) {
                                        StatItemWithIcon(
                                            icon = Icons.Default.Speed,
                                            label = stringResource(id = R.string.average_speed),
                                            value = String.format(
                                                Locale.getDefault(),
                                                "%.1f",
                                                training.averageSpeedKmh()
                                            ),
                                            unit = stringResource(id = R.string.speed_unit),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                // Seconda riga con VAM e FC media
                                if (training.vam() > 0 || (training.averageHeartrate != null && training.averageHeartrate!! > 0)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        if (training.vam() > 0) {
                                            StatItemWithIcon(
                                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                                label = stringResource(id = R.string.vam),
                                                value = String.format(
                                                    Locale.getDefault(),
                                                    "%.0f",
                                                    training.vam()
                                                ),
                                                unit = stringResource(id = R.string.m_per_hour),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (training.averageHeartrate != null && training.averageHeartrate!! > 0) {
                                            StatItemWithIcon(
                                                icon = Icons.Default.Favorite,
                                                label = stringResource(id = R.string.average),
                                                value = String.format(
                                                    Locale.getDefault(),
                                                    "%.0f",
                                                    training.averageHeartrate
                                                ),
                                                unit = stringResource(id = R.string.bpm),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Sezione Nota (se bookmarkato e c'è una nota)
                        if (isBookmarked && bookmarkNote.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))


                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = bookmarkNote,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Header buttons row - renderizzati per ultimi per stare sempre sopra
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pulsante back in alto a sinistra
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Row {
                    // Pulsante share
                    IconButton(onClick = onShare) {
                        Icon(
                            Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Pulsante bookmark
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        if (showBookmarkDialog) {
            BookmarkDialog(
                trainingName = training.name,
                isBookmarked = isBookmarked,
                currentNote = bookmarkNote,
                currentCustomTitle = customTitle,
                currentDifficulty = difficulty,
                onDismiss = onDismissDialog,
                onConfirm = onConfirmBookmark,
                onRemove = onRemoveBookmark
            )
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

@Composable
fun DifficultyBadge(difficulty: String) {
    val (color, text, icon) = when (difficulty) {
        "easy" -> Triple(
            DifficultyEasy,
            stringResource(id = R.string.difficulty_easy),
            Icons.Filled.SentimentSatisfied
        )
        "medium" -> Triple(
            DifficultyMedium,
            stringResource(id = R.string.difficulty_medium),
            Icons.Filled.FitnessCenter
        )
        "hard" -> Triple(
            DifficultyHard,
            stringResource(id = R.string.difficulty_hard),
            Icons.Filled.Whatshot
        )
        else -> return
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}


