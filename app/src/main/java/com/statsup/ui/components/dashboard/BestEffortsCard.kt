package com.statsup.ui.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.statsup.R
import com.statsup.domain.BestEffort
import com.statsup.ui.components.Title

@Composable
fun BestEffortsCard(efforts: List<BestEffort>) {
    if (efforts.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.DirectionsRun,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Title(text = stringResource(R.string.best_efforts_title))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "",
                modifier = Modifier.weight(1.5f)
            )
            Text(
                text = stringResource(R.string.best_efforts_time),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.weight(2f)
            )
            Text(
                text = stringResource(R.string.best_efforts_pace),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.weight(2f)
            )
        }
        efforts.forEach { effort ->
            BestEffortRow(effort)
        }
    }
}

@Composable
private fun BestEffortRow(effort: BestEffort) {
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = effort.label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1.5f)
        )
        Text(
            text = formatSeconds(effort.timeSeconds),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = formatPace(effort.paceMinPerKm),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(2f)
        )
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

private fun formatPace(minPerKm: Double): String {
    val mins = minPerKm.toInt()
    val secs = ((minPerKm - mins) * 60).toInt()
    return "%d:%02d /km".format(mins, secs)
}
