package com.statsup.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.statsup.ui.components.dashboard.DistanceMonthOverMonthChart
import com.statsup.ui.components.dashboard.DurationCard
import com.statsup.ui.components.dashboard.MaxAltitudeCard
import com.statsup.ui.components.dashboard.MaxElevationGainCard
import com.statsup.ui.components.dashboard.MaxHeartRateCard
import com.statsup.ui.components.dashboard.MonthlyDistanceGoalCard
import com.statsup.ui.components.dashboard.TopTrainingTypes
import com.statsup.ui.components.dashboard.TrainingCard
import com.statsup.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    Column(modifier = Modifier.padding(20.dp)) {
        MonthlyDistanceGoalCard(viewModel)
        Row(
            modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TrainingCard(viewModel)
                Spacer(modifier = Modifier.height(10.dp))
                MaxElevationGainCard(viewModel)
                Spacer(modifier = Modifier.height(10.dp))
                MaxHeartRateCard(viewModel)
            }
            Column(modifier = Modifier.weight(1f)) {
                DurationCard(viewModel)
                Spacer(modifier = Modifier.height(10.dp))
                MaxAltitudeCard(viewModel)
            }
        }
        Column(modifier = Modifier.padding(top = 40.dp)) {
            DistanceMonthOverMonthChart(viewModel)
        }
        Column(modifier = Modifier.padding(top = 40.dp)) {
            TopTrainingTypes(viewModel)
        }
    }
}