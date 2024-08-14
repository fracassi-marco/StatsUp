package com.statsup.ui.components

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
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
import com.statsup.ui.components.Title
import com.statsup.ui.viewmodel.HistoryViewModel


@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val state = viewModel.state.value

    if (state.show) {
        LazyColumn {
            items(
                count = state.activities.size,
                key = { state.activities[it].id },
                itemContent = { TrainingListItem(state.activities[it]) })
        }
    }

}

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingListItem(training: Training) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.padding(10.dp),
        /*onClick = {
            val intent = Intent(context, DetailsActivity::class.java).apply {
                putExtra("id", training.id)
            }
            context.startActivity(intent)
        }*/
    ) {
        Title(text = training.name)
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, 0.dp, 10.dp, 4.dp),
            text = formatLocal(training.date),
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, 0.dp, 10.dp, 10.dp)
        ) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(id = R.string.distance))
                Text(text = String.format("%.2f Km", training.distanceInKilometers()))
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(id = R.string.elevation_gain))
                Text(text = String.format("%.0f m", training.totalElevationGain))
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(id = R.string.duration))
                Text(text = Measure.hm(training.movingTime))
            }
        }
        if (training.trip != null) {
            val trip = training.trip!!
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(trip.boundaries.center, trip.zoomForBoundaries)
            }
            val googleMapOptionsFactory = { GoogleMapOptions().liteMode(true) }
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
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
        } else {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                painter = painterResource(id = R.drawable.bg),
                contentDescription = "background",
                contentScale = ContentScale.FillWidth
            )
        }
    }
}