package com.statsup.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.statsup.R
import com.statsup.domain.Training
import com.statsup.ui.viewmodel.AllRoutesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllRoutesMapScreen(
    viewModel: AllRoutesViewModel
) {
    val trainings by viewModel.trainings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.all_routes)) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LoadingBox(isLoading = true) { }
            } else if (trainings.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AllRoutesMap(trainings = trainings)

                    // Info card in alto con numero di percorsi
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .statusBarsPadding(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    ) {
                        Text(
                            text = "${trainings.size} routes displayed",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(id = R.string.no_routes_found),
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun AllRoutesMap(trainings: List<Training>) {
    val cameraPositionState = rememberCameraPositionState()

    // Calcola i bounds per includere tutti i percorsi
    LaunchedEffect(trainings) {
        if (trainings.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            var hasPoints = false

            // Includi più punti rappresentativi per ogni percorso per un centraggio migliore
            trainings.forEach { training ->
                training.trip?.let { trip ->
                    val steps = trip.steps()
                    if (steps.isNotEmpty()) {
                        // Includi primo, ultimo e alcuni punti intermedi
                        boundsBuilder.include(steps.first())
                        boundsBuilder.include(steps.last())

                        // Aggiungi punti al 25%, 50% e 75% del percorso
                        if (steps.size > 3) {
                            boundsBuilder.include(steps[steps.size / 4])
                            boundsBuilder.include(steps[steps.size / 2])
                            boundsBuilder.include(steps[steps.size * 3 / 4])
                        }

                        hasPoints = true
                    }
                }
            }

            if (hasPoints) {
                try {
                    val bounds = boundsBuilder.build()
                    // Padding ridotto a 50px per zoom massimo mantenendo i percorsi visibili
                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50)
                    cameraPositionState.move(cameraUpdate)
                } catch (_: Exception) {
                    // Fallback: centra sulla prima posizione
                    trainings.firstOrNull()?.trip?.steps()?.firstOrNull()?.let { firstPoint ->
                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(firstPoint, 12f)
                        cameraPositionState.move(cameraUpdate)
                    }
                }
            }
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = true,
            compassEnabled = true
        )
    ) {
        // Disegna ogni percorso con un colore diverso usando percorsi semplificati
        trainings.forEachIndexed { index, training ->
            training.trip?.let { trip ->
                val routeColor = getRouteColor(index, trainings.size)
                // Usa simplifiedSteps con tolleranza di 20m per ridurre i punti
                val simplifiedPoints = trip.simplifiedSteps(tolerance = 20.0)
                Polyline(
                    points = simplifiedPoints,
                    width = 3f, // Ridotto da 5f a 3f per migliori prestazioni
                    color = routeColor,
                    geodesic = true
                )
            }
        }
    }
}

// Genera colori diversi per ogni percorso
private fun getRouteColor(index: Int, @Suppress("UNUSED_PARAMETER") totalRoutes: Int): Color {
    val colors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFFF44336), // Red
        Color(0xFF4CAF50), // Green
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFF00BCD4), // Cyan
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFE91E63), // Pink
        Color(0xFF3F51B5), // Indigo
        Color(0xFF009688), // Teal
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF8BC34A), // Light Green
        Color(0xFFCDDC39), // Lime
        Color(0xFFFFC107), // Amber
        Color(0xFF673AB7), // Deep Purple
        Color(0xFF03A9F4), // Light Blue
        Color(0xFF795548), // Brown
        Color(0xFF607D8B)  // Blue Grey
    )

    return colors[index % colors.size]
}

