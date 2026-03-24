package com.statsup.ui.components.dashboard

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.statsup.R
import com.statsup.ui.components.Title
import com.statsup.ui.viewmodel.DashboardViewModel
import io.jetchart.common.animation.fadeInAnimation
import io.jetchart.line.Line
import io.jetchart.line.LineChart
import io.jetchart.line.Point
import io.jetchart.line.renderer.line.GradientLineShader
import io.jetchart.line.renderer.line.SolidLineDrawer
import io.jetchart.line.renderer.point.FilledPointDrawer
import io.jetchart.line.renderer.point.IndexesPointDrawer
import io.jetchart.line.renderer.point.NoPointDrawer
import io.jetchart.line.renderer.xaxis.LineEmptyXAxisDrawer
import io.jetchart.line.renderer.yaxis.LineYAxisWithValueDrawer
import java.time.ZonedDateTime

@Composable
fun DistanceMonthOverMonthChart(viewModel: DashboardViewModel) {
    Title(text = stringResource(R.string.distance_mom))
    LineChart(
        lines = listOf(
            Line(
                points = viewModel.projectedCumulativeDistance().map { Point(it.value.toFloat(), "") },
                lineDrawer = SolidLineDrawer(thickness = 1.5.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                pointDrawer = NoPointDrawer,
                startAtZero = true
            ),
            Line(
                points = viewModel.cumulativeDistance().map { Point(it.value.toFloat(), "") },
                lineDrawer = SolidLineDrawer(thickness = 2.dp, color = MaterialTheme.colorScheme.primary),
                pointDrawer = IndexesPointDrawer(listOf(ZonedDateTime.now().dayOfMonth - 1), FilledPointDrawer(color = MaterialTheme.colorScheme.onBackground)),
                startAtZero = true,
                shader = GradientLineShader(listOf(MaterialTheme.colorScheme.primary, Transparent))
            ),
            Line(
                points = viewModel.pastCumulativeDistance().map { Point(it.value.toFloat(), "") },
                lineDrawer = SolidLineDrawer(thickness = 2.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)),
                pointDrawer = NoPointDrawer,
                startAtZero = true
            ),
            Line(
                points = viewModel.cumulativeDistance().map { Point(viewModel.monthlyDistanceGoal(), "") },
                lineDrawer = SolidLineDrawer(thickness = 1.dp, color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)),
                pointDrawer = NoPointDrawer,
                startAtZero = true
            )
        ),
        modifier = Modifier
            .padding(0.dp, 10.dp, 0.dp, 0.dp)
            .fillMaxWidth()
            .height(120.dp),
        animation = fadeInAnimation(3000),
        xAxisDrawer = LineEmptyXAxisDrawer(),
        yAxisDrawer = LineYAxisWithValueDrawer(
            labelValueFormatter = { value -> "%.0f".format(value) },
            labelTextColor = MaterialTheme.colorScheme.onSurface,
            axisLineThickness = 0.dp,
            axisLineColor = Transparent,
            minRightPadding = 25.dp
        ),
        horizontalOffsetPercentage = 1f
    )
}