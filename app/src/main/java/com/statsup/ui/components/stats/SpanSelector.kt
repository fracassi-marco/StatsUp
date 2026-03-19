package com.statsup.ui.components.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.statsup.R
import com.statsup.ui.viewmodel.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpanSelector(viewModel: StatsViewModel) {
    val options = listOf(stringResource(R.string.span_month), stringResource(R.string.span_year))
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { viewModel.switchSpan(index) },
                selected = index == viewModel.selectedSpan
            ) {
                Text(label)
            }
        }
    }
}

@Composable
fun PeriodNavigator(viewModel: StatsViewModel) {
    val isCurrent = viewModel.isCurrentPeriod()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { viewModel.previousPeriod() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.period_previous),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = viewModel.periodLabel(),
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(
            onClick = { viewModel.nextPeriod() },
            enabled = !isCurrent
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.period_next),
                tint = if (isCurrent) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                       else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSelector(viewModel: StatsViewModel) {
    val options = listOf(
        stringResource(R.string.provider_distance),
        stringResource(R.string.provider_frequency),
        stringResource(R.string.provider_duration),
        stringResource(R.string.provider_elevation)
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { viewModel.switchProvider(index) },
                selected = index == viewModel.selectedProvider
            ) {
                Text(label)
            }
        }
    }
}
