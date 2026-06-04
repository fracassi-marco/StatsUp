package com.statsup.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private const val VISIBLE_DAYS = 365

@Composable
fun WeightLineChart(
    points: List<Pair<Long, Double>>,
    targetKg: Double = 0.0
) {
    if (points.size < 2) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val fmt = remember { DateTimeFormatter.ofPattern("MM/yy").withZone(ZoneId.systemDefault()) }
    val step = 180
    val lines = remember(points, targetKg, primaryColor, errorColor) {
        val sampleStep = (points.size / VISIBLE_DAYS).coerceAtLeast(1)
        val sampled = if (sampleStep == 1) points
            else points.filterIndexed { i, _ -> i % sampleStep == 0 || i == points.size - 1 }
        val lastIndex = sampled.size - 1
        val labeledPoints = sampled.mapIndexed { i, (ts, kg) ->
            val label = if (i % step == 0 || i == lastIndex) fmt.format(Instant.ofEpochMilli(ts)) else ""
            Point(kg.toFloat(), label)
        }
        val result = mutableListOf(
            Line(
                points = labeledPoints,
                lineDrawer = SolidLineDrawer(thickness = 2.dp, color = primaryColor),
                pointDrawer = NoPointDrawer,
                shader = GradientLineShader(listOf(primaryColor.copy(alpha = 0.3f), Transparent))
            )
        )
        if (targetKg > 0) {
            val targetPoints = listOf(
                Point(targetKg.toFloat(), ""),
                Point(targetKg.toFloat(), "")
            )
            result.add(
                Line(
                    points = targetPoints,
                    lineDrawer = SolidLineDrawer(
                        thickness = 1.dp,
                        color = errorColor.copy(alpha = 0.6f)
                    ),
                    pointDrawer = NoPointDrawer
                )
            )
        }
        result
    }

    val scrollState = rememberScrollState()
    var initialScrollDone by remember { mutableStateOf(false) }
    LaunchedEffect(scrollState.maxValue) {
        if (!initialScrollDone && scrollState.maxValue > 0) {
            scrollState.scrollTo(scrollState.maxValue)
            initialScrollDone = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        val chartWidth = if (points.size > VISIBLE_DAYS) {
            maxWidth * (points.size.toFloat() / VISIBLE_DAYS.toFloat())
        } else {
            maxWidth
        }

        Row(modifier = Modifier.horizontalScroll(scrollState)) {
            LineChart(
                lines = lines,
                modifier = Modifier
                    .width(chartWidth)
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
    }
}
