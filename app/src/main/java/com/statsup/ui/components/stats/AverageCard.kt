package com.statsup.ui.components.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.statsup.R
import com.statsup.ui.components.SecondaryCard
import com.statsup.ui.components.Title
import java.util.Locale

@Composable
fun AverageCard(average: Double) {
    SecondaryCard(icon = Icons.Outlined.Compress) {
        Column {
            Text(
                text = stringResource(R.string.average),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
            )
            Row {
                Title(
                    text = String.format(Locale.getDefault(), "%.0f", average),
                )
            }
        }
    }
}