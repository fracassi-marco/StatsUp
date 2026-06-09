package com.statsup.ui.components

import android.util.Log
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.statsup.domain.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

/**
 * Componente ottimizzato per visualizzare una mappa in modalità lite nella lista.
 * Alla prima visualizzazione renderizza la mappa Google, cattura uno snapshot e
 * lo salva su disco. Alle successive visualizzazioni mostra direttamente l'immagine
 * cached eliminando le richieste di tile alla rete.
 *
 * Usa MapView imperativa (non il wrapper Compose) per evitare che la libreria
 * maps-compose imposti camera listener e zoom preference non supportati in Lite Mode.
 */
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

    var cachedBitmap by remember(trainingId) {
        mutableStateOf(MapSnapshotCache.load(context, trainingId))
    }

    Box(modifier = modifier) {
        if (cachedBitmap != null) {
            Image(
                bitmap = cachedBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .height(height.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Log.d("MapSnapshotCache", "Rendering MapView for training $trainingId (no cache found)")

            val mapView = remember {
                MapView(
                    context,
                    GoogleMapOptions()
                        .liteMode(true)
                        .compassEnabled(false)
                        .rotateGesturesEnabled(false)
                        .scrollGesturesEnabled(false)
                        .tiltGesturesEnabled(false)
                        .zoomGesturesEnabled(false)
                ).also {
                    it.onCreate(null)
                    it.onResume()
                }
            }

            DisposableEffect(mapView) {
                val job = coroutineScope.launch {
                    val googleMap = suspendCancellableCoroutine { cont ->
                        mapView.getMapAsync { gm -> cont.resume(gm) }
                    }

                    googleMap.addPolyline(
                        PolylineOptions()
                            .addAll(trip.steps())
                            .width(6f)
                            .color(android.graphics.Color.argb(178, 0, 0, 255))
                            .geodesic(true)
                    )
                    googleMap.addCircle(
                        CircleOptions()
                            .center(trip.begin())
                            .strokeColor(android.graphics.Color.GREEN)
                            .fillColor(android.graphics.Color.argb(204, 0, 255, 0))
                            .radius(10.0)
                            .strokeWidth(1f)
                    )
                    googleMap.addCircle(
                        CircleOptions()
                            .center(trip.end())
                            .strokeColor(android.graphics.Color.RED)
                            .fillColor(android.graphics.Color.argb(204, 255, 0, 0))
                            .radius(10.0)
                            .strokeWidth(1f)
                    )

                    // Attendi il tile statico iniziale
                    withTimeoutOrNull(5_000.milliseconds) {
                        suspendCancellableCoroutine { cont ->
                            googleMap.setOnMapLoadedCallback { cont.resume(Unit) }
                        }
                    }

                    // Registra la callback PRIMA di spostare la camera per evitare race condition,
                    // poi aggiorna la camera sul percorso e attendi il tile aggiornato.
                    withTimeoutOrNull(5_000.milliseconds) {
                        suspendCancellableCoroutine { cont ->
                            googleMap.setOnMapLoadedCallback { cont.resume(Unit) }
                            try {
                                googleMap.moveCamera(
                                    CameraUpdateFactory.newLatLngBounds(trip.getBoundariesWithPadding(0.15), 0)
                                )
                            } catch (_: Exception) {
                                try {
                                    googleMap.moveCamera(
                                        CameraUpdateFactory.newLatLngBounds(trip.boundaries, 20)
                                    )
                                } catch (_: Exception) {
                                    googleMap.moveCamera(
                                        CameraUpdateFactory.newLatLngZoom(trip.boundaries.center, 13f)
                                    )
                                }
                            }
                        }
                    }

                    val bitmap = suspendCancellableCoroutine { cont ->
                        googleMap.snapshot { bm -> cont.resume(bm) }
                    }

                    if (bitmap != null) {
                        withContext(Dispatchers.IO) {
                            MapSnapshotCache.save(context, trainingId, bitmap)
                        }
                        cachedBitmap = bitmap
                    }
                }

                onDispose {
                    job.cancel()
                    mapView.onPause()
                    mapView.onStop()
                    mapView.onDestroy()
                }
            }

            AndroidView(
                factory = { mapView },
                modifier = Modifier
                    .fillMaxSize()
                    .height(height.dp)
            )
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
