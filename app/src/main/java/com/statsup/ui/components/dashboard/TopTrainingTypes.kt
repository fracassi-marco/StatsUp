package com.statsup.ui.components.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.statsup.R
import com.statsup.ui.components.Title
import com.statsup.ui.theme.SecondaryText
import com.statsup.ui.viewmodel.DashboardViewModel

@Composable
fun TopTrainingTypes(viewModel: DashboardViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Title(text = stringResource(id = R.string.top_training))
        val total = viewModel.topTrainings().values.sumOf { it.size }
        viewModel.topTrainings().forEach { (k, v) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = k, modifier = Modifier.weight(2f))
                LinearProgressIndicator(
                    progress = { v.size / total.toFloat() },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = SecondaryText,
                    modifier = Modifier
                        .weight(4f)
                        .padding(horizontal = 10.dp)
                )
                Text(text = v.size.toString(), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
    }
}