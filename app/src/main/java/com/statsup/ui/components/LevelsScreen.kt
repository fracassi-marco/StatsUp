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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statsup.R
import com.statsup.domain.Level
import com.statsup.ui.viewmodel.DashboardViewModel

private data class LevelDef(
    val number: Int,
    @androidx.annotation.StringRes val nameResId: Int,
    val emoji: String,
    val thresholdXp: Int
)

private val ALL_LEVELS = listOf(
    LevelDef(1,  R.string.level_name_1,  "🌱", 0),
    LevelDef(2,  R.string.level_name_2,  "🏃", 200),
    LevelDef(3,  R.string.level_name_3,  "💪", 500),
    LevelDef(4,  R.string.level_name_4,  "⚡", 1_000),
    LevelDef(5,  R.string.level_name_5,  "🔥", 2_000),
    LevelDef(6,  R.string.level_name_6,  "🌟", 4_000),
    LevelDef(7,  R.string.level_name_7,  "🏅", 7_000),
    LevelDef(8,  R.string.level_name_8,  "🥇", 11_000),
    LevelDef(9,  R.string.level_name_9,  "🏆", 16_000),
    LevelDef(10, R.string.level_name_10, "👑", 25_000),
    LevelDef(11, R.string.level_name_11, "🔱", 36_000),
    LevelDef(12, R.string.level_name_12, "⭐", 52_000),
    LevelDef(13, R.string.level_name_13, "🦁", 73_000),
    LevelDef(14, R.string.level_name_14, "🌩️", 100_000),
    LevelDef(15, R.string.level_name_15, "🛡️", 135_000),
    LevelDef(16, R.string.level_name_16, "🚀", 180_000),
    LevelDef(17, R.string.level_name_17, "🦅", 240_000),
    LevelDef(18, R.string.level_name_18, "✨", 320_000),
    LevelDef(19, R.string.level_name_19, "🌌", 420_000),
    LevelDef(20, R.string.level_name_20, "♾️", 550_000)
)

@Composable
fun LevelsScreen(viewModel: DashboardViewModel, onNavigateBack: () -> Unit) {
    val level = viewModel.level()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem((level.number - 1).coerceAtLeast(0))
    }

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
                text = stringResource(R.string.levels_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ALL_LEVELS) { def ->
                LevelRow(def = def, currentLevel = level)
            }
        }
    }
}

@Composable
private fun LevelRow(def: LevelDef, currentLevel: Level) {
    val isCurrent = def.number == currentLevel.number
    val isCompleted = def.number < currentLevel.number

    val containerColor = when {
        isCurrent   -> MaterialTheme.colorScheme.primaryContainer
        isCompleted -> MaterialTheme.colorScheme.secondaryContainer
        else        -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }
    val textColor = when {
        isCurrent   -> MaterialTheme.colorScheme.onPrimaryContainer
        isCompleted -> MaterialTheme.colorScheme.onSecondaryContainer
        else        -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = def.emoji,
                fontSize = 36.sp,
                modifier = if (!isCompleted && !isCurrent) Modifier.alpha(0.25f) else Modifier
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lv. ${def.number} · ${stringResource(def.nameResId)}",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Text(
                        text = stringResource(R.string.level_card_total_xp, def.thresholdXp),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
                if (isCurrent) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { currentLevel.progress },
                        modifier = Modifier.fillMaxWidth(),
                        strokeCap = StrokeCap.Round,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.levels_xp_progress,
                            currentLevel.currentLevelXp,
                            currentLevel.nextLevelXp
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            if (isCompleted) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
