package com.statsup.ui.components.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.statsup.R
import com.statsup.ui.viewmodel.DashboardViewModel

@Composable
fun LevelCard(viewModel: DashboardViewModel, onClick: () -> Unit = {}) {
    val level = viewModel.level()

    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = level.emoji,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(R.string.level_card_label, level.number, stringResource(level.nameResId)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = stringResource(R.string.level_card_total_xp, level.totalXp),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { level.progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        when {
            level.isMaxLevel -> Text(
                text = stringResource(R.string.level_card_max_level),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            level.isDecaying -> Text(
                text = stringResource(R.string.level_card_decay_warning, level.dailyDecayRate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            else -> Text(
                text = stringResource(R.string.level_card_xp_to_next, level.nextLevelXp - level.currentLevelXp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}
