package com.statsup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.statsup.R
import com.statsup.domain.Training
import com.statsup.ui.viewmodel.AllRoutesViewModel
import kotlin.math.floor
import kotlin.math.pow

private const val INDIVIDUAL_ROUTE_ZOOM = 13f

private data class RouteCluster(val center: LatLng, val count: Int)

private fun clusterRoutes(trainings: List<Training>, zoom: Float): List<RouteCluster> {
    val gridSize = 360.0 / 2.0.pow(zoom.toDouble()) / 4.0
    val grid = mutableMapOf<Pair<Long, Long>, MutableList<LatLng>>()

    trainings.forEach { training ->
        val center = training.trip?.centerPoint() ?: return@forEach
        val key = Pair(
            floor(center.longitude / gridSize).toLong(),
            floor(center.latitude / gridSize).toLong()
        )
        grid.getOrPut(key) { mutableListOf() }.add(center)
    }

    return grid.map { (_, centers) ->
        RouteCluster(
            center = LatLng(
                centers.map { it.latitude }.average(),
                centers.map { it.longitude }.average()
            ),
            count = centers.size
        )
    }
}

@Composable
fun AllRoutesMapScreen(
    viewModel: AllRoutesViewModel
) {
    val trainings by viewModel.trainings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTitle(text = stringResource(id = R.string.all_routes))
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                LoadingBox(isLoading = true) { }
            } else if (trainings.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AllRoutesMap(trainings = trainings)

                    Card(
                        modifier = Modifier.padding(16.dp),
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
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AllRoutesMap(trainings: List<Training>) {
    val cameraPositionState = rememberCameraPositionState()

    // Quantize zoom to 0.5 steps to avoid recomputing clusters on every animation frame
    val quantizedZoom by remember { derivedStateOf { (cameraPositionState.position.zoom * 2f).toInt() / 2f } }
    val showClusters = quantizedZoom < INDIVIDUAL_ROUTE_ZOOM
    val clusters = remember(trainings, quantizedZoom) {
        if (showClusters) clusterRoutes(trainings, quantizedZoom) else emptyList()
    }

    LaunchedEffect(trainings) {
        if (trainings.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            var hasPoints = false

            trainings.forEach { training ->
                training.trip?.let { trip ->
                    val steps = trip.steps()
                    if (steps.isNotEmpty()) {
                        boundsBuilder.include(steps.first())
                        boundsBuilder.include(steps.last())
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
                    cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 20))
                } catch (_: Exception) {
                    try {
                        trainings.firstOrNull()?.trip?.steps()?.firstOrNull()?.let { firstPoint ->
                            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(firstPoint, 12f))
                        }
                    } catch (_: Exception) { }
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
        if (showClusters) {
            clusters.forEach { cluster ->
                MarkerComposable(
                    keys = arrayOf(cluster.center, cluster.count),
                    state = MarkerState(position = cluster.center),
                    anchor = Offset(0.5f, 0.5f)
                ) {
                    ClusterMarker(count = cluster.count)
                }
            }
        } else {
            trainings.forEachIndexed { index, training ->
                training.trip?.let { trip ->
                    Polyline(
                        points = trip.simplifiedSteps(tolerance = 20.0),
                        width = 3f,
                        color = getRouteColor(index),
                        geodesic = true
                    )
                }
            }
        }
    }
}

@Composable
private fun ClusterMarker(count: Int) {
    val size = when {
        count <= 5 -> 40.dp
        count <= 20 -> 48.dp
        else -> 56.dp
    }
    val bgColor = when {
        count <= 5 -> Color(0xFF2196F3)  // Blue
        count <= 20 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336)        // Red
    }
    Box(
        modifier = Modifier
            .size(size)
            .background(bgColor.copy(alpha = 0.85f), CircleShape)
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun getRouteColor(index: Int): Color {
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
