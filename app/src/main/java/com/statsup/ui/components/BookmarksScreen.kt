package com.statsup.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.statsup.domain.BookmarkedTrainingWithDetails
import com.statsup.domain.Measure
import com.statsup.domain.formatLocal
import com.statsup.ui.viewmodel.BookmarksViewModel
import java.util.Locale

@Composable
fun BookmarksScreen(
    viewModel: BookmarksViewModel,
    onTrainingClick: (Long) -> Unit
) {
    val trainings by viewModel.bookmarkedTrainings.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTitle(text = stringResource(id = R.string.bookmarked_trainings))

        if (trainings.isEmpty()) {
            EmptyBookmarksState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(trainings) { bookmarkedTraining ->
                    BookmarkedTrainingListItem(
                        bookmarkedTraining = bookmarkedTraining,
                        onTrainingClick = { onTrainingClick(bookmarkedTraining.training.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookmarkedTrainingListItem(
    bookmarkedTraining: BookmarkedTrainingWithDetails,
    onTrainingClick: () -> Unit
) {
    val training = bookmarkedTraining.training
    val displayTitle = bookmarkedTraining.customTitle.ifEmpty { training.name }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { onTrainingClick() },
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        // Titolo centrato (custom o originale)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                textAlign = TextAlign.Center
            )
        }

        // Badge difficoltà se presente
        if (bookmarkedTraining.difficulty.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                DifficultyBadge(difficulty = bookmarkedTraining.difficulty)
            }
        }

        // Data centrata
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, 4.dp, 10.dp, 4.dp),
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
                Text(text = String.format(Locale.getDefault(), "%.2f %s", training.distanceInKilometers(), stringResource(R.string.km)))
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(id = R.string.elevation_gain))
                Text(text = String.format(Locale.getDefault(), "%.0f %s", training.totalElevationGain, stringResource(R.string.m)))
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(id = R.string.duration))
                Text(text = Measure.hm(training.movingTime))
            }
        }
        if (training.trip != null) {
            MapListItemPreview(
                trip = training.trip!!,
                trainingId = training.id,
                modifier = Modifier.fillMaxWidth(),
                height = 180,
                onClick = { onTrainingClick() }
            )
        } else {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable { onTrainingClick() },
                painter = painterResource(id = R.drawable.bg),
                contentDescription = null,
                contentScale = ContentScale.FillWidth
            )
        }
    }
}

@Composable
private fun EmptyBookmarksState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
            Text(
                text = stringResource(id = R.string.no_bookmarked_trainings),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

