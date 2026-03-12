package com.statsup.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.statsup.R
import com.statsup.ui.components.dashboard.ActivityHeatmap
import com.statsup.ui.components.dashboard.DistanceMonthOverMonthChart
import com.statsup.ui.components.dashboard.StreakCard
import com.statsup.ui.components.dashboard.DurationCard
import com.statsup.ui.components.dashboard.MaxAltitudeCard
import com.statsup.ui.components.dashboard.MaxElevationGainCard
import com.statsup.ui.components.dashboard.MonthlyDistanceGoalCard
import com.statsup.ui.components.dashboard.MonthlyTrainingGoalCard
import com.statsup.ui.components.dashboard.TopTrainingTypes
import com.statsup.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    Column {
        ScreenTitle(text = stringResource(R.string.dashboard_title))
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 20.dp)) {
        MonthlyDistanceGoalCard(viewModel)
        Spacer(modifier = Modifier.height(10.dp))
        MonthlyTrainingGoalCard(viewModel)
        Spacer(modifier = Modifier.height(10.dp))
        StreakCard(viewModel)
        Row(
            modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MaxElevationGainCard(viewModel)
                Spacer(modifier = Modifier.height(10.dp))
                MaxAltitudeCard(viewModel)
            }
            Column(modifier = Modifier.weight(1f)) {
                DurationCard(viewModel)
            }
        }
        Column(modifier = Modifier.padding(top = 40.dp)) {
            DistanceMonthOverMonthChart(viewModel)
        }
        Column(modifier = Modifier.padding(top = 40.dp)) {
            TopTrainingTypes(viewModel)
        }
        Column(modifier = Modifier.padding(top = 40.dp)) {
            Text(
                text = stringResource(R.string.activity_heatmap_title),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ActivityHeatmap(viewModel)
        }
        }
    }
}