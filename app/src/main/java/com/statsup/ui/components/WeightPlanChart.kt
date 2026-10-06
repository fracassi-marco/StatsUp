package com.statsup.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.statsup.domain.WeightPlan
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import kotlin.math.max

/**
 * Last-30-days chart comparing the ideal path to the weight target (dashed) with the
 * actual measurements (solid line with dots).
 */
@Composable
fun WeightPlanChart(plan: WeightPlan) {
    val planColor = MaterialTheme.colorScheme.tertiary
    val actualColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    val values = plan.plannedPoints.map { it.second } + plan.actualPoints.map { it.second }
    val padding = max((values.max() - values.min()) * 0.1, 0.5)
    val minY = values.min() - padding
    val maxY = values.max() + padding
    val days = max(ChronoUnit.DAYS.between(plan.windowStart, plan.windowEnd), 1L).toFloat()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            Column(
                modifier = Modifier.fillMaxHeight().padding(end = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                listOf(maxY, (maxY + minY) / 2, minY).forEach {
                    Text(
                        text = "%.1f".format(it),
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                }
            }
            Canvas(modifier = Modifier.weight(1f).fillMaxHeight().padding(vertical = 6.dp)) {
                fun x(day: LocalDate) =
                    ChronoUnit.DAYS.between(plan.windowStart, day) / days * size.width
                fun y(kg: Double) = ((maxY - kg) / (maxY - minY)).toFloat() * size.height

                listOf(0f, 0.5f, 1f).forEach { f ->
                    drawLine(gridColor, Offset(0f, f * size.height), Offset(size.width, f * size.height), 1.dp.toPx())
                }

                val planPath = Path()
                plan.plannedPoints.forEachIndexed { i, (day, kg) ->
                    if (i == 0) planPath.moveTo(x(day), y(kg)) else planPath.lineTo(x(day), y(kg))
                }
                drawPath(
                    planPath,
                    planColor,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))
                    )
                )

                if (plan.actualPoints.size >= 2) {
                    val actualPath = Path()
                    plan.actualPoints.forEachIndexed { i, (day, kg) ->
                        if (i == 0) actualPath.moveTo(x(day), y(kg)) else actualPath.lineTo(x(day), y(kg))
                    }
                    drawPath(actualPath, actualColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                }
                plan.actualPoints.forEach { (day, kg) ->
                    drawCircle(actualColor, radius = 3.5.dp.toPx(), center = Offset(x(day), y(kg)))
                }
            }
        }
        val dateFmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 36.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(plan.windowStart.format(dateFmt), style = MaterialTheme.typography.labelSmall, color = labelColor)
            Text(plan.windowEnd.format(dateFmt), style = MaterialTheme.typography.labelSmall, color = labelColor)
        }
    }
}

@Composable
fun WeightPlanLegendItem(color: Color, label: String, dashed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(18.dp).height(10.dp)) {
            Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                drawLine(
                    color,
                    Offset(0f, size.height / 2),
                    Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())) else null
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium)
    }
}
