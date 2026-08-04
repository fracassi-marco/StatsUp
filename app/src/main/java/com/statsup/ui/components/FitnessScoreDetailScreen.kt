package com.statsup.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.statsup.R
import com.statsup.domain.FitnessFactorType
import com.statsup.domain.FitnessScoreFactor
import com.statsup.domain.FitnessScoreTrendPoint
import com.statsup.ui.viewmodel.FitnessScoreViewModel

@Composable
fun FitnessScoreDetailScreen(viewModel: FitnessScoreViewModel, onNavigateBack: () -> Unit) {
    val fitnessScore = viewModel.fitnessScore

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenTitle(
            text = stringResource(R.string.fitness_score_detail_title),
            onBack = onNavigateBack
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FitnessScoreSummaryCard(fitnessScore.score)
            }

            if (viewModel.fitnessScoreTrend.size >= 2) {
                item {
                    FitnessScoreTrendCard(viewModel.fitnessScoreTrend)
                }
            }

            item {
                Text(
                    text = stringResource(R.string.fitness_score_factors_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(fitnessScore.factors) { factor ->
                FitnessScoreFactorRow(factor)
            }
        }
    }
}

@Composable
private fun FitnessScoreSummaryCard(score: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScoreRing(score = score, sizeDp = 88.dp, strokeWidth = 8.dp)
            Column {
                Text(
                    text = stringResource(R.string.fitness_score_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.fitness_score_detail_explanation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun FitnessScoreTrendCard(trend: List<FitnessScoreTrendPoint>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.fitness_score_trend_title),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FitnessScoreTrendChart(points = trend)
        }
    }
}

@Composable
private fun FitnessScoreFactorRow(factor: FitnessScoreFactor) {
    var showInfo by remember { mutableStateOf(false) }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(factorLabel(factor.type)),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = { showInfo = true }, modifier = Modifier.size(24.dp).padding(start = 4.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.cd_fitness_score_factor_info),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                Text(
                    text = factor.score.toInt().toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (factor.included) {
                LinearProgressIndicator(
                    progress = { (factor.score / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.fitness_score_weight_label, factor.weightPercent.toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = stringResource(R.string.fitness_score_factor_excluded),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text(stringResource(factorLabel(factor.type))) },
            text = { Text(stringResource(factorDescription(factor.type))) },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }
}

private fun factorLabel(type: FitnessFactorType): Int = when (type) {
    FitnessFactorType.TRAINING_LOAD -> R.string.fitness_score_factor_training_load
    FitnessFactorType.RECOVERY -> R.string.fitness_score_factor_recovery
    FitnessFactorType.CONSISTENCY -> R.string.fitness_score_factor_consistency
    FitnessFactorType.LOAD_BALANCE -> R.string.fitness_score_factor_load_balance
    FitnessFactorType.INTENSITY_BALANCE -> R.string.fitness_score_factor_intensity_balance
    FitnessFactorType.SPORT_VARIETY -> R.string.fitness_score_factor_sport_variety
    FitnessFactorType.PERFORMANCE_TREND -> R.string.fitness_score_factor_performance_trend
    FitnessFactorType.MEDIUM_TERM_TREND -> R.string.fitness_score_factor_medium_term_trend
    FitnessFactorType.WEIGHT_TREND -> R.string.fitness_score_factor_weight_trend
}

private fun factorDescription(type: FitnessFactorType): Int = when (type) {
    FitnessFactorType.TRAINING_LOAD -> R.string.fitness_score_factor_training_load_desc
    FitnessFactorType.RECOVERY -> R.string.fitness_score_factor_recovery_desc
    FitnessFactorType.CONSISTENCY -> R.string.fitness_score_factor_consistency_desc
    FitnessFactorType.LOAD_BALANCE -> R.string.fitness_score_factor_load_balance_desc
    FitnessFactorType.INTENSITY_BALANCE -> R.string.fitness_score_factor_intensity_balance_desc
    FitnessFactorType.SPORT_VARIETY -> R.string.fitness_score_factor_sport_variety_desc
    FitnessFactorType.PERFORMANCE_TREND -> R.string.fitness_score_factor_performance_trend_desc
    FitnessFactorType.MEDIUM_TERM_TREND -> R.string.fitness_score_factor_medium_term_trend_desc
    FitnessFactorType.WEIGHT_TREND -> R.string.fitness_score_factor_weight_trend_desc
}
