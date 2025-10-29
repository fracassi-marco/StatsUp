package com.statsup.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statsup.R
import com.statsup.domain.Measure
import com.statsup.domain.Training
import com.statsup.domain.formatLocal
import com.statsup.ui.theme.SecondaryText
import com.statsup.ui.viewmodel.HistoryViewModel
import java.util.Locale


@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onTrainingClick: (Long) -> Unit) {
    val state = viewModel.state.value

    if (state.show) {
        LazyColumn {
            items(
                count = state.activities.size,
                key = { state.activities[it].id },
                itemContent = { TrainingListItem(state.activities[it], onTrainingClick) })
        }
    }

}

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingListItem(training: Training, onTrainingClick: (Long) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, SecondaryText),
        onClick = { onTrainingClick(training.id) }
    ) {
        Title(text = training.name, marginStart = 16.dp, marginTop = 8.dp)
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
                Text(text = String.format(Locale.getDefault(), "%.2f Km", training.distanceInKilometers()))
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(id = R.string.elevation_gain))
                Text(text = String.format(Locale.getDefault(), "%.0f m", training.totalElevationGain))
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(id = R.string.duration))
                Text(text = Measure.hm(training.movingTime))
            }
        }
        if (training.trip != null) {
            MapListItemPreview(
                trip = training.trip!!,
                modifier = Modifier.fillMaxWidth(),
                height = 180
            )
        } else {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                painter = painterResource(id = R.drawable.bg),
                contentDescription = "background",
                contentScale = ContentScale.FillWidth
            )
        }
    }
}