package com.statsup.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.unit.dp
import io.jetchart.common.animation.fadeInAnimation
import io.jetchart.line.Line
import io.jetchart.line.LineChart
import io.jetchart.line.Point
import io.jetchart.line.renderer.line.GradientLineShader
import io.jetchart.line.renderer.line.SolidLineDrawer
import io.jetchart.line.renderer.point.NoPointDrawer
import io.jetchart.line.renderer.xaxis.LineXAxisDrawer
import io.jetchart.line.renderer.yaxis.LineYAxisWithValueDrawer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WeightLineChart(
    points: List<Pair<Long, Double>>,
    targetKg: Double = 0.0
) {
    if (points.size < 2) return

    val fmt = DateTimeFormatter.ofPattern("MM/yy").withZone(ZoneId.systemDefault())
    val step = maxOf(1, points.size / 6)
    val labeledPoints = points.mapIndexed { i, (ts, kg) ->
        val label = if (i % step == 0) fmt.format(Instant.ofEpochMilli(ts)) else ""
        Point(kg.toFloat(), label)
    }

    val lines = mutableListOf(
        Line(
            points = labeledPoints,
            lineDrawer = SolidLineDrawer(thickness = 2.dp, color = MaterialTheme.colorScheme.primary),
            pointDrawer = NoPointDrawer,
            shader = GradientLineShader(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), Transparent))
        )
    )

    if (targetKg > 0) {
        val targetPoints = listOf(
            Point(targetKg.toFloat(), ""),
            Point(targetKg.toFloat(), "")
        )
        lines.add(
            Line(
                points = targetPoints,
                lineDrawer = SolidLineDrawer(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                ),
                pointDrawer = NoPointDrawer
            )
        )
    }

    LineChart(
        lines = lines,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(160.dp),
        animation = fadeInAnimation(2000),
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
        horizontalOffsetPercentage = 1f
    )
}
