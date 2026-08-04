package com.statsup.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.unit.dp
import com.statsup.domain.FitnessScoreTrendPoint
import io.jetchart.common.animation.fadeInAnimation
import io.jetchart.line.Line
import io.jetchart.line.LineChart
import io.jetchart.line.Point
import io.jetchart.line.renderer.line.GradientLineShader
import io.jetchart.line.renderer.line.SolidLineDrawer
import io.jetchart.line.renderer.point.NoPointDrawer
import io.jetchart.line.renderer.xaxis.LineXAxisDrawer
import io.jetchart.line.renderer.yaxis.LineYAxisWithValueDrawer
import java.time.format.DateTimeFormatter

private const val LABEL_STEP_DAYS = 7

@Composable
fun FitnessScoreTrendChart(points: List<FitnessScoreTrendPoint>) {
    if (points.size < 2) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val fmt = remember { DateTimeFormatter.ofPattern("dd/MM") }
    val lastIndex = points.size - 1
    val line = remember(points, primaryColor) {
        Line(
            points = points.mapIndexed { i, point ->
                val label = if (i % LABEL_STEP_DAYS == 0 || i == lastIndex) fmt.format(point.date) else ""
                Point(point.score.toFloat(), label)
            },
            lineDrawer = SolidLineDrawer(thickness = 2.dp, color = primaryColor),
            pointDrawer = NoPointDrawer,
            shader = GradientLineShader(listOf(primaryColor.copy(alpha = 0.3f), Transparent))
        )
    }

    LineChart(
        lines = listOf(line),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        animation = fadeInAnimation(400),
        xAxisDrawer = LineXAxisDrawer(
            axisLineThickness = 0.dp,
            labelRatio = 1,
            labelTextColor = MaterialTheme.colorScheme.onSurface
        ),
        yAxisDrawer = LineYAxisWithValueDrawer(
            labelValueFormatter = { value -> "%.0f".format(value) },
            labelTextColor = MaterialTheme.colorScheme.onSurface,
            axisLineThickness = 0.dp,
            axisLineColor = Transparent,
            minRightPadding = 30.dp
        ),
        horizontalOffsetPercentage = 0f
    )
}
