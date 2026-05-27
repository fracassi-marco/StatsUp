package com.statsup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.statsup.R
import com.statsup.domain.RecoveryContribution
import com.statsup.ui.viewmodel.DashboardViewModel
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Composable
fun RecoveryDetailScreen(viewModel: DashboardViewModel, onNavigateBack: () -> Unit) {
    val recoveryHours = viewModel.recoveryTime()
    val breakdown = viewModel.recoveryBreakdown()
    val isReady = recoveryHours <= 0.0
    val progress = if (isReady) 1f else (1f - (recoveryHours / 72.0).coerceIn(0.0, 1.0)).toFloat()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
            }
            Text(
                text = stringResource(R.string.recovery_detail_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                RecoverySummaryCard(recoveryHours, isReady, progress)
            }

            item {
                DailyContributionChart(breakdown)
            }

            item {
                Text(
                    text = stringResource(R.string.recovery_detail_contributions_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (breakdown.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.recovery_detail_no_activities),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                val maxContrib = breakdown.maxOf { it.contributionHours }
                items(breakdown) { item ->
                    RecoveryActivityRow(item = item, maxContrib = maxContrib)
                }
            }
        }
    }
}

@Composable
private fun RecoverySummaryCard(recoveryHours: Double, isReady: Boolean, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.recovery_time),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (isReady) {
                    Text(
                        text = stringResource(R.string.recovery_ready),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    val h = recoveryHours.toInt()
                    val m = ((recoveryHours - h) * 60).toInt()
                    val label = if (m > 0) stringResource(R.string.recovery_hours, h, m)
                    else stringResource(R.string.recovery_hours_only, h)
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.recovery_detail_explanation),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun DailyContributionChart(breakdown: List<RecoveryContribution>) {
    val today = LocalDate.now()
    val dailyData = (6 downTo 0).map { daysAgo ->
        val day = today.minusDays(daysAgo.toLong())
        val total = breakdown
            .filter { it.training.date.toLocalDate() == day }
            .sumOf { it.contributionHours }
        daysAgo to total
    }
    val maxContrib = dailyData.maxOfOrNull { it.second } ?: 0.0

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.recovery_detail_chart_title),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            dailyData.forEach { (daysAgo, contrib) ->
                val fraction = if (maxContrib > 0.0) (contrib / maxContrib).toFloat() else 0f
                val isToday = daysAgo == 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(fraction.coerceAtLeast(0.03f))
                        .background(
                            color = if (isToday) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = if (contrib > 0.0) 0.55f else 0.15f),
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                        )
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            dailyData.forEach { (daysAgo, _) ->
                val day = today.minusDays(daysAgo.toLong())
                val label = "${day.dayOfMonth}"
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (daysAgo == 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun RecoveryActivityRow(item: RecoveryContribution, maxContrib: Double) {
    val now = ZonedDateTime.now()
    val hoursSince = ChronoUnit.HOURS.between(item.training.date, now).toInt()
    val timeAgoLabel = if (hoursSince < 24) {
        stringResource(R.string.recovery_detail_hours_ago, hoursSince)
    } else {
        stringResource(R.string.recovery_detail_days_ago, hoursSince / 24)
    }

    val totalMins = (item.contributionHours * 60).toInt().coerceAtLeast(1)
    val contribH = totalMins / 60
    val contribM = totalMins % 60
    val contribLabel = buildString {
        append("+")
        if (contribH > 0) {
            append("${contribH}h")
            if (contribM > 0) append(" ${contribM}m")
        } else {
            append("${contribM}m")
        }
    }

    val recencyPct = (item.recencyWeight * 100).toInt()
    val loadFormatted = "%.0f".format(item.load)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.training.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = timeAgoLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.recovery_detail_load, loadFormatted),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = stringResource(R.string.recovery_detail_recency, recencyPct.toString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val barFraction = if (maxContrib > 0.0) (item.contributionHours / maxContrib).toFloat() else 0f
                LinearProgressIndicator(
                    progress = { barFraction },
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                Text(
                    text = contribLabel,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
