package com.statsup.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statsup.R
import com.statsup.domain.Measure
import com.statsup.domain.SportTypeFormatter
import com.statsup.domain.Training
import com.statsup.domain.formatLocal
import com.statsup.ui.theme.SecondaryText
import com.statsup.ui.viewmodel.HistoryViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun HistoryScreen(viewModel: HistoryViewModel, onTrainingClick: (Long) -> Unit) {
    val state = viewModel.state.value

    if (state.show) {
        Column {
            // Filtri per tipo di sport
            if (state.availableSportTypes.isNotEmpty()) {
                SportTypeFilter(
                    availableSportTypes = state.availableSportTypes,
                    selectedSportType = state.selectedSportType,
                    onSportTypeSelected = { viewModel.setSportTypeFilter(it) }
                )
            }

            // Raggruppa i training filtrati per mese
            val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
            val groupedTrainings = state.filteredActivities.groupBy { training ->
                training.date.format(monthYearFormatter)
            }

            LazyColumn {
                groupedTrainings.forEach { (monthYear, trainings) ->
                    // Header del mese
                    item(key = "header_$monthYear") {
                        MonthHeader(monthYear = monthYear)
                    }

                    // Items del mese
                    items(
                        count = trainings.size,
                        key = { trainings[it].id },
                        itemContent = { index ->
                            TrainingListItem(trainings[index], onTrainingClick)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SportTypeFilter(
    availableSportTypes: List<String>,
    selectedSportType: String?,
    onSportTypeSelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chip "All sports"
        FilterChip(
            selected = selectedSportType == null,
            onClick = { onSportTypeSelected(null) },
            label = { Text(stringResource(id = R.string.all_sports)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // Chip per ogni tipo di sport
        availableSportTypes.forEach { sportType ->
            FilterChip(
                selected = selectedSportType == sportType,
                onClick = { onSportTypeSelected(sportType) },
                label = { Text(SportTypeFormatter.getFormattedNameWithEmoji(sportType)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}


@Composable
fun MonthHeader(monthYear: String) {
    Text(
        text = monthYear.replaceFirstChar { it.uppercase() },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, bottom = 8.dp, end = 16.dp)
    )
}

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingListItem(training: Training, onTrainingClick: (Long) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onTrainingClick(training.id) },
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, SecondaryText)
    ) {
        // Titolo centrato
        Text(
            text = training.name,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            textAlign = TextAlign.Center
        )
        // Data centrata
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
                height = 180,
                onClick = { onTrainingClick(training.id) }
            )
        } else {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable { onTrainingClick(training.id) },
                painter = painterResource(id = R.drawable.bg),
                contentDescription = "background",
                contentScale = ContentScale.FillWidth
            )
        }
    }
}