package com.statsup.ui.components.stats

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.statsup.R
import com.statsup.ui.components.Title
import com.statsup.ui.viewmodel.StatsViewModel
import io.jetchart.bar.Bar
import io.jetchart.bar.BarChart
import io.jetchart.bar.Bars
import io.jetchart.bar.renderer.label.SimpleBarLabelDrawer
import io.jetchart.bar.renderer.label.SimpleBarValueDrawer
import io.jetchart.bar.renderer.label.SimpleBarValueDrawer.ValueDrawLocation.Outside
import io.jetchart.bar.renderer.xaxis.BarXAxisDrawer
import io.jetchart.bar.renderer.yaxis.BarEmptyYAxisDrawer
import io.jetchart.common.animation.fadeInAnimation


@Composable
fun MonthBarChart(viewModel: StatsViewModel) {
    if (viewModel.hideMonthChart())
        return

    Title(text = stringResource(R.string.day_by_day), marginTop = 40.dp)
    BarChart(
        bars = Bars(bars = viewModel.groupByDay().map {
            Bar(label = if (it.key % 2 != 0) it.key.toString() else "", value = it.value.toFloat(), color = MaterialTheme.colorScheme.primary)
        }),
        modifier = Modifier
            .padding(0.dp, 10.dp, 0.dp, 0.dp)
            .fillMaxWidth()
            .height(120.dp),
        animation = fadeInAnimation(1000),
        xAxisDrawer = BarXAxisDrawer(),
        yAxisDrawer = BarEmptyYAxisDrawer(),
        labelDrawer = SimpleBarLabelDrawer(),
        valueDrawer = SimpleBarValueDrawer(drawLocation = Outside, formatter = barValueFormatter()),
        barHorizontalMargin = 3.dp
    )
}
private fun barValueFormatter() = { value: Float -> if (value == 0f) "" else "%.0f".format(value) }