package com.statsup.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statsup.R
import com.statsup.domain.Lap
import java.util.Locale

@Composable
fun SplitsSection(
    laps: List<Lap>,
    averagePace: Double,
    hasHeartrate: Boolean
) {
    if (laps.isEmpty()) return

    val showHr = hasHeartrate && laps.any { it.averageHeartrate != null && it.averageHeartrate > 0 }
    val showElev = laps.any { it.elevationDifference != null }

    StatSection(
        title = stringResource(id = R.string.splits_section_title),
        icon = Icons.Default.Timer
    )

    // Header row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(id = R.string.splits_col_split),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(id = R.string.splits_col_pace),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.weight(1.5f)
        )
        if (showHr) {
            Text(
                text = stringResource(id = R.string.splits_col_hr),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
        if (showElev) {
            Text(
                text = stringResource(id = R.string.splits_col_elev),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }

    laps.forEach { lap ->
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        SplitRow(
            lap = lap,
            averagePace = averagePace,
            showHr = showHr,
            showElev = showElev
        )
    }
}

@Composable
private fun SplitRow(
    lap: Lap,
    averagePace: Double,
    showHr: Boolean,
    showElev: Boolean
) {
    val lapPace = lap.pace()
    val paceColor = when {
        averagePace > 0 && lapPace < averagePace * 0.97 -> MaterialTheme.colorScheme.primary
        averagePace > 0 && lapPace > averagePace * 1.03 -> Color(0xFFE65100)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = lap.split.toString(),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.Center
        )
        Text(
            text = formatPace(lapPace),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = paceColor,
            modifier = Modifier.weight(1.5f)
        )
        if (showHr) {
            val hr = lap.averageHeartrate
            Text(
                text = if (hr != null && hr > 0) "${hr.toInt()} bpm" else "—",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
        if (showElev) {
            val elev = lap.elevationDifference
            Text(
                text = if (elev != null) formatElevation(elev) else "—",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

private fun formatPace(paceInMinutes: Double): String {
    if (paceInMinutes <= 0.0) return "—"
    val minutes = paceInMinutes.toInt()
    val seconds = ((paceInMinutes - minutes) * 60).toInt()
    return String.format(Locale.getDefault(), "%d:%02d /km", minutes, seconds)
}

private fun formatElevation(meters: Double): String {
    return if (meters >= 0) {
        "+${String.format(Locale.getDefault(), "%.0f", meters)}m"
    } else {
        "${String.format(Locale.getDefault(), "%.0f", meters)}m"
    }
}
