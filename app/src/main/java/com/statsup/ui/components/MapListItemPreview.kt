package com.statsup.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.statsup.domain.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Componente ottimizzato per visualizzare una mappa in modalità lite nella lista.
 * Alla prima visualizzazione renderizza la mappa Google, cattura uno snapshot e
 * lo salva su disco. Alle successive visualizzazioni mostra direttamente l'immagine
 * cached eliminando le richieste di tile alla rete.
 */
@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapListItemPreview(
    trip: Trip,
    trainingId: String,
    modifier: Modifier = Modifier,
    height: Int = 180,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Controlla se esiste già uno snapshot su disco
    var cachedBitmap by remember(trainingId) {
        mutableStateOf(MapSnapshotCache.load(context, trainingId))
    }

    Box(modifier = modifier) {
        if (cachedBitmap != null) {
            // Mostra l'immagine cached senza caricare Google Maps
            Image(
                bitmap = cachedBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .height(height.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            // Prima volta: renderizza la mappa e cattura lo snapshot
            val cameraPositionState = rememberCameraPositionState()

            val updateCamera: suspend () -> Unit = remember(trip) {
                {
                    try {
                        val paddedBounds = trip.getBoundariesWithPadding(0.15)
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngBounds(paddedBounds, 0)
                        )
                    } catch (_: Exception) {
                        try {
                            cameraPositionState.move(
                                CameraUpdateFactory.newLatLngBounds(trip.boundaries, 20)
                            )
                        } catch (_: Exception) {
                            try {
                                cameraPositionState.move(
                                    CameraUpdateFactory.newLatLngZoom(trip.boundaries.center, 13f)
                                )
                            } catch (_: Exception) {}
                        }
                    }
                }
            }

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

            var googleMapRef by remember { mutableStateOf<GoogleMap?>(null) }

            DisposableEffect(Unit) {
                onDispose {
                    googleMapRef?.setOnCameraIdleListener(null)
                    googleMapRef = null
                }
            }

            Log.d("MapSnapshotCache", "Rendering GoogleMap for training $trainingId (no cache found)")

            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .height(height.dp),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                googleMapOptionsFactory = { googleMapOptions },
                uiSettings = mapUiSettings,
                onMapLoaded = {
                    coroutineScope.launch {
                        updateCamera()
                    }
                }
            ) {
                Circle(
                    center = trip.begin(),
                    strokeColor = Color.Green,
                    fillColor = Color.Green.copy(alpha = 0.8f),
                    radius = 10.0,
                    strokeWidth = 1f
                )
                Circle(
                    center = trip.end(),
                    strokeColor = Color.Red,
                    fillColor = Color.Red.copy(alpha = 0.8f),
                    radius = 10.0,
                    strokeWidth = 1f
                )
                Polyline(
                    points = trip.steps(),
                    width = 6f,
                    color = Color.Blue.copy(alpha = 0.7f),
                    geodesic = true
                )

                // Scatta lo snapshot quando la camera è ferma sulla posizione corretta
                MapEffect(Unit) { googleMap ->
                    googleMapRef = googleMap
                    googleMap.setOnCameraIdleListener {
                        googleMap.snapshot { bitmap ->
                            if (bitmap != null) {
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        MapSnapshotCache.save(context, trainingId, bitmap)
                                    }
                                    cachedBitmap = bitmap
                                    // Rimuove il listener: lo snapshot serve solo una volta
                                    googleMap.setOnCameraIdleListener(null)
                                }
                            }
                        }
                    }
                }
            }
        }

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
