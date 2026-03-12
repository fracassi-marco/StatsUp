package com.statsup.ui.components.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.statsup.R
import com.statsup.ui.components.Title
import com.statsup.ui.theme.SecondaryText
import com.statsup.ui.viewmodel.DashboardViewModel

private val ZONE_COLORS = listOf(
    Color(0xFF64B5F6), // Z1 blue
    Color(0xFF81C784), // Z2 green
    Color(0xFFFFD54F), // Z3 yellow
    Color(0xFFFF8A65), // Z4 orange
    Color(0xFFE57373), // Z5 red
)

private val ZONE_LABELS = listOf(
    R.string.hr_zone_1,
    R.string.hr_zone_2,
    R.string.hr_zone_3,
    R.string.hr_zone_4,
    R.string.hr_zone_5,
)

@Composable
fun HeartRateZonesCard(viewModel: DashboardViewModel) {
    val zones = viewModel.hrZoneDistribution()
    val total = zones.values.sum()

    Column(modifier = Modifier.fillMaxWidth()) {
        Title(text = stringResource(R.string.hr_zones_title))

        if (total == 0) {
            Text(
                text = stringResource(R.string.hr_zones_no_data),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
            return@Column
        }

        zones.entries.sortedBy { it.key }.forEach { (zone, count) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(ZONE_LABELS[zone - 1]),
                    modifier = Modifier.weight(2f)
                )
                LinearProgressIndicator(
                    progress = { if (total > 0) count / total.toFloat() else 0f },
                    color = ZONE_COLORS[zone - 1],
                    trackColor = SecondaryText,
                    modifier = Modifier
                        .weight(4f)
                        .padding(horizontal = 10.dp)
                )
                Text(
                    text = count.toString(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
