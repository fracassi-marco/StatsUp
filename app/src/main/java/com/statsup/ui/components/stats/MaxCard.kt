package com.statsup.ui.components.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowCircleUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.statsup.R
import com.statsup.ui.components.SecondaryCard
import com.statsup.ui.components.Title

@Composable
fun MaxCard(max: Double) {
    SecondaryCard(icon = Icons.Outlined.ArrowCircleUp) {
        Column {
            Text(
                text = stringResource(R.string.max),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
            )
            Title(
                text = String.format(LocalLocale.current.platformLocale, "%.0f", max),
            )
        }
    }
}

@Composable
fun SmallCard(value: Double, label: String) {
    SecondaryCard() {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
            )
            Title(
                text = String.format(LocalLocale.current.platformLocale, "%.0f", value),
            )
        }
    }
}
