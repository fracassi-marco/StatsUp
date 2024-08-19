package com.statsup.ui.components.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.statsup.R
import com.statsup.ui.components.SecondaryCard
import com.statsup.ui.components.Title
import com.statsup.ui.viewmodel.DashboardViewModel
import java.util.Locale

@Composable
fun MaxElevationGainCard(viewModel: DashboardViewModel) {
    SecondaryCard(icon = Icons.AutoMirrored.Outlined.ShowChart) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.8f)
        ) {
            Text(
                text = stringResource(R.string.max_elevation_gain),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
            )
            Title(
                text = String.format(Locale.getDefault(), "%.0f ${stringResource(id = R.string.m)}", viewModel.maxElevation()),
            )
        }
    }
}