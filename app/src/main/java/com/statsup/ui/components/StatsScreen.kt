package com.statsup.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.statsup.ui.components.stats.AverageCard
import com.statsup.ui.components.stats.MaxCard
import com.statsup.ui.components.stats.MonthBarChart
import com.statsup.ui.components.stats.MonthCumulativeChart
import com.statsup.ui.components.stats.ProviderSelector
import com.statsup.ui.components.stats.SmallCard
import com.statsup.ui.components.stats.SpanSelector
import com.statsup.ui.components.stats.YearBarChart
import com.statsup.ui.components.stats.YearCumulativeChart
import com.statsup.ui.viewmodel.StatsViewModel
import com.statsup.R

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    Column {
        ScreenTitle(text = stringResource(R.string.stats_title))
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)) {
        SpanSelector(viewModel)
        Spacer(modifier = Modifier.height(10.dp))
        ProviderSelector(viewModel)
        Spacer(modifier = Modifier.height(10.dp))

        if(viewModel.hideYearChart()) {
            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SmallCard(viewModel.doneOfMonth(), stringResource(R.string.done))
                }
                Column(modifier = Modifier.weight(1f)) {
                    SmallCard(viewModel.trendOfMonth(), stringResource(R.string.trend))
                }
                Column(modifier = Modifier.weight(1f)) {
                    SmallCard(viewModel.doneOfPastMonth(), stringResource(R.string.past))
                }
            }
            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MaxCard(viewModel.maxOfMonth())
                }
                Column(modifier = Modifier.weight(1f)) {
                    AverageCard(viewModel.averageOfMonth())
                }
            }
        }
        if(viewModel.hideMonthChart()) {
            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SmallCard(viewModel.doneOfYear(), stringResource(R.string.done))
                }
                Column(modifier = Modifier.weight(1f)) {
                    SmallCard(viewModel.trendOfYear(), stringResource(R.string.trend))
                }
                Column(modifier = Modifier.weight(1f)) {
                    SmallCard(viewModel.doneOfPastYear(), stringResource(R.string.past))
                }
            }
            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MaxCard(viewModel.maxOfYear())
                }
                Column(modifier = Modifier.weight(1f)) {
                    AverageCard(viewModel.averageOfYear())
                }
            }
        }

        MonthCumulativeChart(viewModel)
        MonthBarChart(viewModel)
        YearCumulativeChart(viewModel)
        YearBarChart(viewModel)
        }
    }
}
