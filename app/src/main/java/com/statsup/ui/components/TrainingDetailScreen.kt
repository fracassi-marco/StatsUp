package com.statsup.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
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
                // Mappa di sfondo
                if (training.trip != null) {
                    val trip = training.trip!!
                    val cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(trip.boundaries.center, trip.zoomForBoundaries)
                    }
                    val googleMapOptionsFactory = { GoogleMapOptions().liteMode(false) }
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(mapType = MapType.NORMAL),
                        googleMapOptionsFactory = googleMapOptionsFactory,
                        uiSettings = MapUiSettings(
                            mapToolbarEnabled = false,
                            scrollGesturesEnabled = false,
                            scrollGesturesEnabledDuringRotateOrZoom = false,
                            zoomControlsEnabled = false,
                            zoomGesturesEnabled = false,
                            rotationGesturesEnabled = false
                        )
                    ) {
                        Circle(center = trip.begin(), strokeColor = Color.Green, fillColor = Color.Green, radius = 12.0)
                        Circle(center = trip.end(), strokeColor = Color.Red, fillColor = Color.Red, radius = 12.0)
                        Polyline(points = trip.steps(), width = 8f, color = Color.Blue, geodesic = true)
                    }
                }

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
                        Column(modifier = Modifier.padding(16.dp)) {
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
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Grid con le statistiche
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatItem(
                                    label = stringResource(id = R.string.distance),
                                    value = String.format(Locale.getDefault(), "%.2f", training.distanceInKilometers()),
                                    unit = stringResource(id = R.string.km)
                                )
                                StatItem(
                                    label = stringResource(id = R.string.duration),
                                    value = Measure.hm(training.movingTime),
                                    unit = ""
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatItem(
                                    label = stringResource(id = R.string.min_altitude),
                                    value = String.format(Locale.getDefault(), "%.0f", training.elevLow),
                                    unit = stringResource(id = R.string.m)
                                )
                                StatItem(
                                    label = stringResource(id = R.string.max_altitude),
                                    value = String.format(Locale.getDefault(), "%.0f", training.elevHigh),
                                    unit = stringResource(id = R.string.m)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatItem(
                                    label = stringResource(id = R.string.average_pace),
                                    value = calculatePace(training.distance, training.movingTime),
                                    unit = stringResource(id = R.string.pace_unit)
                                )
                                StatItem(
                                    label = stringResource(id = R.string.elevation_gain),
                                    value = String.format(Locale.getDefault(), "%.0f", training.totalElevationGain),
                                    unit = stringResource(id = R.string.m)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = " $unit",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

private fun calculatePace(distanceInMeters: Double, timeInSeconds: Int): String {
    if (distanceInMeters == 0.0) return "0:00"

    val paceInMinutesPerKm = (timeInSeconds / 60.0) / (distanceInMeters / 1000.0)
    val minutes = paceInMinutesPerKm.toInt()
    val seconds = ((paceInMinutesPerKm - minutes) * 60).toInt()

    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}

