package com.statsup.ui.components.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.statsup.R
import com.statsup.domain.Training
import com.statsup.domain.Trainings
import com.statsup.ui.components.SecondaryCard
import com.statsup.ui.components.Title
import com.statsup.ui.components.chart.NoXAxisDrawer
import com.statsup.ui.viewmodel.DashboardViewModel
import io.jetchart.common.animation.fadeInAnimation
import io.jetchart.line.Line
import io.jetchart.line.LineChart
import io.jetchart.line.Point
import io.jetchart.line.renderer.line.GradientLineShader
import io.jetchart.line.renderer.line.SolidLineDrawer
import io.jetchart.line.renderer.point.NoPointDrawer
import io.jetchart.line.renderer.yaxis.LineYAxisWithValueDrawer
import java.time.Year
import java.time.ZonedDateTime
import java.util.Locale

@Composable
fun DurationCard(viewModel: DashboardViewModel, modifier: Modifier) {
    SecondaryCard(modifier = modifier, icon = Icons.Outlined.AccessTime,
        bottom = {
            LineChart(
                lines = listOf(
                    Line(points = points(viewModel.trainings), lineDrawer = SolidLineDrawer(thickness = 1.dp, color = MaterialTheme.colorScheme.primary))
                ),
                modifier = Modifier
                    .padding(10.dp, 0.dp, 0.dp, 10.dp)
                    .fillMaxWidth()
                    .height(100.dp),
                animation = fadeInAnimation(3000),
                pointDrawer = NoPointDrawer,
                xAxisDrawer = NoXAxisDrawer(),
                yAxisDrawer = LineYAxisWithValueDrawer(labelValueFormatter = { value -> "%.0f".format(value) }, axisLineThickness = 0.dp, axisLineColor = Transparent),
                horizontalOffsetPercentage = 1f,
                lineShader = GradientLineShader(listOf(MaterialTheme.colorScheme.primary, Transparent))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.8f)
        ) {
            Text(
                text = stringResource(R.string.duration),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
            )
            Title(
                text = "${String.format(Locale.getDefault(), "%.0f", viewModel.totalDuration())} ${stringResource(R.string.h)}",
            )
        }
    }
}

@Composable
private fun points(count: List<Training>) = Trainings(count) { it.count().toDouble() }.cumulativeDays(Year.from(ZonedDateTime.now()), ZonedDateTime.now().month).map { Point(it.value.toFloat(), "") }