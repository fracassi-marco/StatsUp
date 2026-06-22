package com.statsup.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

private const val MAX_POINTS = 300
private const val LABEL_COUNT = 6

@Composable
fun ElevationProfileChart(
    elevationPoints: List<Double>,
    totalDistanceKm: Double
) {
    if (elevationPoints.size < 2) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    val lines = remember(elevationPoints, totalDistanceKm, primaryColor) {
        val step = (elevationPoints.size / MAX_POINTS).coerceAtLeast(1)
        val sampled = if (step == 1) elevationPoints
        else elevationPoints.filterIndexed { i, _ -> i % step == 0 || i == elevationPoints.size - 1 }

        val labelStep = (sampled.size / LABEL_COUNT).coerceAtLeast(1)
        val points = sampled.mapIndexed { i, alt ->
            val distKm = if (sampled.size > 1)
                (i.toDouble() / (sampled.size - 1)) * totalDistanceKm
            else 0.0
            val label = if (i % labelStep == 0 || i == sampled.size - 1)
                "%.1f".format(distKm)
            else ""
            Point(alt.toFloat(), label)
        }
        listOf(
            Line(
                points = points,
                lineDrawer = SolidLineDrawer(thickness = 2.dp, color = primaryColor),
                pointDrawer = NoPointDrawer,
                shader = GradientLineShader(listOf(primaryColor.copy(alpha = 0.35f), Transparent))
            )
        )
    }

    LineChart(
        lines = lines,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(top = 16.dp),
        animation = fadeInAnimation(400),
        xAxisDrawer = LineXAxisDrawer(
            axisLineThickness = 0.dp,
            labelRatio = 1,
            labelTextColor = onSurface.copy(alpha = 0.6f)
        ),
        yAxisDrawer = LineYAxisWithValueDrawer(
            labelValueFormatter = { value -> "%.0f".format(value) },
            labelTextColor = onSurface.copy(alpha = 0.6f),
            axisLineThickness = 0.dp,
            axisLineColor = Transparent,
            minRightPadding = 30.dp
        ),
        horizontalOffsetPercentage = 0f
    )
}
