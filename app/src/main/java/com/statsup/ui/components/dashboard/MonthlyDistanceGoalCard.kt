package com.statsup.ui.components.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statsup.R
import com.statsup.ui.components.PrimaryCard
import com.statsup.ui.viewmodel.DashboardViewModel

@Composable
fun MonthlyDistanceGoalCard(viewModel: DashboardViewModel) {
    PrimaryCard(icon = Icons.Outlined.EmojiEvents) {
        Column {
            Text(
                text = stringResource(R.string.settings_screen_goals_monthly),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${String.format(LocalLocale.current.platformLocale, "%.0f", viewModel.totalDistance())} ${stringResource(R.string.km)} → ~${String.format(LocalLocale.current.platformLocale, "%.0f", viewModel.projectedDistanceEndOfMonth())} ${stringResource(R.string.km)} / ${String.format(LocalLocale.current.platformLocale, "%.0f", viewModel.monthlyDistanceGoal())} ${stringResource(R.string.km)}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { viewModel.distancePercentage() },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }
    }
}