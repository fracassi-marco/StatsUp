package com.statsup.ui.components.stats

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.unit.dp
import com.statsup.ui.theme.SecondaryText
import com.statsup.ui.viewmodel.StatsViewModel
import io.jetchart.common.animation.fadeInAnimation
import io.jetchart.line.Line
import io.jetchart.line.LineChart
import io.jetchart.line.Point
import io.jetchart.line.renderer.line.GradientLineShader
import io.jetchart.line.renderer.line.SolidLineDrawer
import io.jetchart.line.renderer.point.NoPointDrawer
import io.jetchart.line.renderer.xaxis.LineXAxisDrawer
import io.jetchart.line.renderer.yaxis.LineYAxisWithValueDrawer

@Composable
fun YearCumulativeChart(viewModel: StatsViewModel) {
    if(viewModel.hideYearChart())
        return
    LineChart(
        lines = listOf(
            Line(
                points = viewModel.cumulativeYear().map { Point(it.value.toFloat(), it.key.value.toString()) },
                lineDrawer = SolidLineDrawer(thickness = 2.dp, color = MaterialTheme.colorScheme.primary),
                startAtZero = true,
                shader = GradientLineShader(listOf(MaterialTheme.colorScheme.primary, Transparent))
            ),
            Line(
                points = viewModel.pastCumulativeYear().map { Point(it.value.toFloat(), "") },
                lineDrawer = SolidLineDrawer(thickness = 2.dp, color = SecondaryText),
                startAtZero = true
            )
        ),
        modifier = Modifier
            .padding(0.dp, 10.dp, 0.dp, 0.dp)
            .fillMaxWidth()
            .height(120.dp),
        animation = fadeInAnimation(3000),
        pointDrawer = NoPointDrawer,
        xAxisDrawer = LineXAxisDrawer(axisLineThickness = 0.dp),
        yAxisDrawer = LineYAxisWithValueDrawer(
            labelValueFormatter = { value -> "%.0f".format(value) },
            axisLineThickness = 0.dp,
            axisLineColor = Transparent,
            minRightPadding = 25.dp
        ),
        horizontalOffsetPercentage = 1f
    )
}