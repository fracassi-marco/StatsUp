package com.statsup.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationDisabled
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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

@Composable
fun MapFullscreenScreen(
    training: Training?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var isLocationEnabled by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        isLocationEnabled = granted
    }

    // Check permissions on composition
    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        isLocationEnabled = fine || coarse
    }

    val cameraPositionState = rememberCameraPositionState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            LoadingBox(isLoading = true) { }
        } else if (training?.trip != null) {
            val trip = training.trip!!

            // LaunchedEffect per centrare con zoom ottimale automatico
            LaunchedEffect(trip) {
                try {
                    val paddedBounds = trip.getBoundariesWithPadding(0.1)
                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(paddedBounds, 50)
                    cameraPositionState.move(cameraUpdate)
                } catch (_: Exception) {
                    try {
                        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(trip.boundaries, 50)
                        cameraPositionState.move(cameraUpdate)
                    } catch (_: Exception) {
                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(trip.boundaries.center, 13f)
                        cameraPositionState.move(cameraUpdate)
                    }
                }
            }

            val googleMapOptionsFactory = { GoogleMapOptions().liteMode(false) }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = MapType.NORMAL,
                    isMyLocationEnabled = isLocationEnabled
                ),
                googleMapOptionsFactory = googleMapOptionsFactory,
                uiSettings = MapUiSettings(
                    mapToolbarEnabled = true,
                    scrollGesturesEnabled = true,
                    scrollGesturesEnabledDuringRotateOrZoom = true,
                    zoomControlsEnabled = true,
                    zoomGesturesEnabled = true,
                    rotationGesturesEnabled = true,
                    myLocationButtonEnabled = isLocationEnabled
                )
            ) {
                Circle(center = trip.begin(), strokeColor = Color.Green, fillColor = Color.Green, radius = 12.0)
                Circle(center = trip.end(), strokeColor = Color.Red, fillColor = Color.Red, radius = 12.0)
                Polyline(points = trip.steps(), width = 8f, color = Color.Blue, geodesic = true)
            }

            FloatingActionButton(
                onClick = {
                    if (isLocationEnabled) {
                        isLocationEnabled = false
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                containerColor = if (isLocationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector = if (isLocationEnabled) Icons.Filled.MyLocation else Icons.Filled.LocationDisabled,
                    contentDescription = if (isLocationEnabled) "Disable my location" else "Show my location",
                    tint = if (isLocationEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Transparent top bar overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = training?.name ?: stringResource(id = R.string.map),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
