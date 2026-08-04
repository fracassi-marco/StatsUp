package com.statsup.ui.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.statsup.R
import com.statsup.ui.components.PrimaryCard
import com.statsup.ui.components.ScoreRing
import com.statsup.ui.viewmodel.FitnessScoreViewModel

@Composable
fun FitnessScoreCard(viewModel: FitnessScoreViewModel, onClick: (() -> Unit)? = null) {
    val fitnessScore = viewModel.fitnessScore

    PrimaryCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScoreRing(score = fitnessScore.score, sizeDp = 64.dp, strokeWidth = 6.dp)
            Text(
                text = stringResource(R.string.fitness_score_title),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
