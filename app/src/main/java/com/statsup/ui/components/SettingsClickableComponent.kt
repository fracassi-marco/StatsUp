package com.statsup.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SettingsClickableComponent(
    @StringRes iconDesc: Int,
    @StringRes name: Int,
    icon: ImageVector,
    onClick: () -> Unit,
    value: String = ""
) {
    SecondaryCard(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(id = iconDesc),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = name),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.End,
            overflow = TextOverflow.Ellipsis,
        )
    }
}