package com.statsup.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.statsup.domain.Trip
import kotlinx.coroutines.launch

/**
 * Componente ottimizzato per visualizzare una mappa in modalità lite nella lista
 * Usa configurazioni minimali per ridurre il consumo di risorse
 * Lo zoom è calcolato automaticamente per mostrare tutto il percorso
 */
@Composable
fun MapListItemPreview(
    trip: Trip,
    modifier: Modifier = Modifier,
    height: Int = 180,
    onClick: (() -> Unit)? = null
) {
    val cameraPositionState = rememberCameraPositionState()
    val coroutineScope = rememberCoroutineScope()

    // Funzione per aggiornare la camera quando la mappa è pronta
    val updateCamera: suspend () -> Unit = remember(trip) {
        {
            try {
                val paddedBounds = trip.getBoundariesWithPadding(0.15) // 15% di padding
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
                    paddedBounds,
                    0 // padding già gestito nei boundaries
                )
                cameraPositionState.move(cameraUpdate)
            } catch (_: Exception) {
                // Fallback: usa boundaries senza padding
                try {
                    val cameraUpdate = CameraUpdateFactory.newLatLngBounds(trip.boundaries, 20)
                    cameraPositionState.move(cameraUpdate)
                } catch (_: Exception) {
                    // Ultimo fallback: usa centro con zoom manuale
                    try {
                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            trip.boundaries.center,
                            13f
                        )
                        cameraPositionState.move(cameraUpdate)
                    } catch (_: Exception) {
                        // Ignora se CameraUpdateFactory non è ancora inizializzato
                    }
                }
            }
        }
    }

    // Ottimizzazione: crea le opzioni una sola volta
    val googleMapOptions = remember {
        GoogleMapOptions()
            .liteMode(true)
            .compassEnabled(false)
            .rotateGesturesEnabled(false)
            .scrollGesturesEnabled(false)
            .tiltGesturesEnabled(false)
            .zoomGesturesEnabled(false)
    }

    val mapProperties = remember {
        MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = false,
            isTrafficEnabled = false
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            mapToolbarEnabled = false,
            scrollGesturesEnabled = false,
            scrollGesturesEnabledDuringRotateOrZoom = false,
            zoomControlsEnabled = false,
            zoomGesturesEnabled = false,
            rotationGesturesEnabled = false,
            compassEnabled = false,
            indoorLevelPickerEnabled = false,
            myLocationButtonEnabled = false,
            tiltGesturesEnabled = false
        )
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .height(height.dp),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            googleMapOptionsFactory = { googleMapOptions },
            uiSettings = mapUiSettings,
            onMapLoaded = {
                // Aggiorna la camera solo quando la mappa è completamente caricata
                coroutineScope.launch {
                    updateCamera()
                }
            }
        ) {
            // Marker di inizio (verde)
            Circle(
                center = trip.begin(),
                strokeColor = Color.Green,
                fillColor = Color.Green.copy(alpha = 0.8f),
                radius = 10.0,
                strokeWidth = 1f
            )

            // Marker di fine (rosso)
            Circle(
                center = trip.end(),
                strokeColor = Color.Red,
                fillColor = Color.Red.copy(alpha = 0.8f),
                radius = 10.0,
                strokeWidth = 1f
            )

            // Polyline del percorso
            Polyline(
                points = trip.steps(),
                width = 6f,
                color = Color.Blue.copy(alpha = 0.7f),
                geodesic = true
            )
        }

        // Overlay trasparente per gestire i click
        if (onClick != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        onClick = onClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )
        }
    }
}

