package com.statsup.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val ScoreColorLow = Color(0xFFE53935)
private val ScoreColorMid = Color(0xFFFFB300)
private val ScoreColorHigh = Color(0xFF43A047)

fun scoreColor(score: Int): Color {
    val clamped = score.coerceIn(0, 100)
    return if (clamped <= 50) {
        lerp(ScoreColorLow, ScoreColorMid, clamped / 50f)
    } else {
        lerp(ScoreColorMid, ScoreColorHigh, (clamped - 50) / 50f)
    }
}

@Composable
fun ScoreRing(
    score: Int,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    sizeDp: Dp = 96.dp
) {
    val clamped = score.coerceIn(0, 100)
    val color = scoreColor(clamped)
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    Box(modifier = modifier.size(sizeDp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * (clamped / 100f),
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = stroke
            )
        }
        Text(
            text = clamped.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = color
        )
    }
}
