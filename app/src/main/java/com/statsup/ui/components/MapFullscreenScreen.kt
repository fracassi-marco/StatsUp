package com.statsup.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.statsup.R
import com.statsup.domain.Training

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapFullscreenScreen(
    training: Training?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.map)) },
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
        } else if (training?.trip != null) {
            val trip = training.trip!!
            val cameraPositionState = rememberCameraPositionState()

            // LaunchedEffect per centrare con zoom ottimale automatico
            LaunchedEffect(trip) {
                try {
                    val paddedBounds = trip.getBoundariesWithPadding(0.1) // 10% di padding
                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(paddedBounds, 50) // 50px per controlli UI
                    cameraPositionState.move(cameraUpdate)
                } catch (_: Exception) {
                    // Fallback: prova senza padding aggiuntivo
                    try {
                        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(trip.boundaries, 50)
                        cameraPositionState.move(cameraUpdate)
                    } catch (_: Exception) {
                        // Ultimo fallback
                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(trip.boundaries.center, 13f)
                        cameraPositionState.move(cameraUpdate)
                    }
                }
            }

            val googleMapOptionsFactory = { GoogleMapOptions().liteMode(false) }
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = MapType.NORMAL),
                googleMapOptionsFactory = googleMapOptionsFactory,
                uiSettings = MapUiSettings(
                    mapToolbarEnabled = true,
                    scrollGesturesEnabled = true,
                    scrollGesturesEnabledDuringRotateOrZoom = true,
                    zoomControlsEnabled = true,
                    zoomGesturesEnabled = true,
                    rotationGesturesEnabled = true
                )
            ) {
                Circle(center = trip.begin(), strokeColor = Color.Green, fillColor = Color.Green, radius = 12.0)
                Circle(center = trip.end(), strokeColor = Color.Red, fillColor = Color.Red, radius = 12.0)
                Polyline(points = trip.steps(), width = 8f, color = Color.Blue, geodesic = true)
            }
        }
    }
}

