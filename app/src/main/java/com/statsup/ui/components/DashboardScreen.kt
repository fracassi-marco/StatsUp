package com.statsup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.statsup.R
import com.statsup.domain.GoalAchievement
import com.statsup.ui.components.dashboard.ActivityHeatmap
import com.statsup.ui.components.dashboard.DistanceMonthOverMonthChart
import com.statsup.ui.components.dashboard.HeartRateZonesCard
import com.statsup.ui.components.dashboard.StreakCard
import com.statsup.ui.components.dashboard.DurationCard
import com.statsup.ui.components.dashboard.MaxAltitudeCard
import com.statsup.ui.components.dashboard.MaxElevationGainCard
import com.statsup.ui.components.dashboard.MonthlyDistanceGoalCard
import com.statsup.ui.components.dashboard.MonthlyTrainingGoalCard
import com.statsup.ui.components.dashboard.BestEffortsCard
import com.statsup.ui.components.dashboard.TopTrainingTypes
import com.statsup.ui.viewmodel.DashboardViewModel
import com.statsup.domain.TargetSuggestion

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    var celebrationAchievement by remember { mutableStateOf<GoalAchievement?>(null) }

    LaunchedEffect(Unit) {
        viewModel.goalAchieved.collect { achievement ->
            celebrationAchievement = achievement
        }
    }

    celebrationAchievement?.let { achievement ->
        CelebrationDialog(
            achievement = achievement,
            onDismiss = { celebrationAchievement = null }
        )
    }

    viewModel.targetSuggestion?.let { suggestion ->
        TargetSuggestionDialog(
            suggestion = suggestion,
            onAccept = { viewModel.acceptTargetSuggestion() },
            onDismiss = { viewModel.dismissTargetSuggestion() },
            onSnooze = { viewModel.snoozeTargetSuggestion() }
        )
    }

    Column {
        ScreenTitle(text = stringResource(R.string.dashboard_title))
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 20.dp)) {

            MonthlyDistanceGoalCard(viewModel)
            Spacer(modifier = Modifier.height(10.dp))
            MonthlyTrainingGoalCard(viewModel)
            Spacer(modifier = Modifier.height(10.dp))
            StreakCard(viewModel)
            Row(
                modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MaxElevationGainCard(viewModel)
                    Spacer(modifier = Modifier.height(10.dp))
                    MaxAltitudeCard(viewModel)
                }
                Column(modifier = Modifier.weight(1f)) {
                    DurationCard(viewModel)
                }
            }
            Column(modifier = Modifier.padding(top = 40.dp)) {
                DistanceMonthOverMonthChart(viewModel)
            }
            Column(modifier = Modifier.padding(top = 40.dp)) {
                BestEffortsCard(viewModel.bestEfforts())
            }
            Column(modifier = Modifier.padding(top = 40.dp)) {
                TopTrainingTypes(viewModel)
            }
            Column(modifier = Modifier.padding(top = 40.dp)) {
                HeartRateZonesCard(viewModel)
            }
            Column(modifier = Modifier.padding(top = 40.dp)) {
                Text(
                    text = stringResource(R.string.activity_heatmap_title),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ActivityHeatmap(viewModel)
            }
        }
    }
}

@Composable
private fun TargetSuggestionDialog(
    suggestion: TargetSuggestion,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    Dialog(
        onDismissRequest = onSnooze,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.target_suggestion_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.target_suggestion_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Suggested values row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SuggestionChip(
                        icon = {
                            Icon(
                                Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        },
                        label = stringResource(R.string.target_suggestion_distance, suggestion.distanceKm),
                        modifier = Modifier.weight(1f)
                    )
                    SuggestionChip(
                        icon = {
                            Icon(
                                Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        },
                        label = stringResource(R.string.target_suggestion_trainings, suggestion.trainingCount),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.target_suggestion_accept))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.target_suggestion_dismiss))
                }

                TextButton(
                    onClick = onSnooze,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.target_suggestion_snooze),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}
