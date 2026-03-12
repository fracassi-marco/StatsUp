package com.statsup.ui.components.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.statsup.ui.viewmodel.DashboardViewModel
import java.time.DayOfWeek
import java.time.LocalDate

private val CELL_SIZE = 10.dp
private val CELL_GAP = 2.dp
private val DAY_LABELS = listOf("L", "M", "M", "G", "V", "S", "D")

@Composable
fun ActivityHeatmap(viewModel: DashboardViewModel) {
    val data = viewModel.activityHeatmap()
    val maxValue = data.values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val today = LocalDate.now()
    val gridStart = today.minusWeeks(51).with(DayOfWeek.MONDAY)

    val weeks = (0 until 52).map { w ->
        (0 until 7).map { d -> gridStart.plusDays((w * 7 + d).toLong()) }
    }

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val primary = MaterialTheme.colorScheme.primary

    Row {
        Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
            DAY_LABELS.forEach { label ->
                Box(modifier = Modifier.size(CELL_SIZE), contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        fontSize = 7.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(CELL_GAP)
        ) {
            weeks.forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(CELL_GAP)) {
                    week.forEach { day ->
                        val color = when {
                            day.isAfter(today) -> Color.Transparent
                            else -> {
                                val value = data[day] ?: 0.0
                                if (value == 0.0) surfaceVariant
                                else primary.copy(
                                    alpha = (0.25f + 0.75f * (value / maxValue).toFloat()).coerceIn(0.25f, 1f)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(CELL_SIZE)
                                .background(color, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}
