package com.statsup.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.statsup.ui.components.stats.MonthBarChart
import com.statsup.ui.components.stats.MonthCumulativeChart
import com.statsup.ui.components.stats.ProviderSelector
import com.statsup.ui.components.stats.SpanSelector
import com.statsup.ui.components.stats.YearBarChart
import com.statsup.ui.components.stats.YearCumulativeChart
import com.statsup.ui.viewmodel.StatsViewModel

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    Column(modifier = Modifier.padding(20.dp)) {
        SpanSelector(viewModel)
        Spacer(modifier = Modifier.height(10.dp))
        ProviderSelector(viewModel)
        Spacer(modifier = Modifier.height(10.dp))
        MonthCumulativeChart(viewModel)
        MonthBarChart(viewModel)
        YearCumulativeChart(viewModel)
        YearBarChart(viewModel)
    }
}
