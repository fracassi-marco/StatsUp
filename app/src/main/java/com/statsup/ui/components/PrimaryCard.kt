package com.statsup.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.statsup.R
import com.statsup.ui.theme.SecondaryText

@Composable
fun SecondaryCard(modifier: Modifier = Modifier,
                  onClick: (() -> Unit)? = null,
                  icon: ImageVector,
                  bottom: @Composable () -> Unit = {},
                  content: @Composable () -> Unit) {
    PrimaryCard(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, SecondaryText),
        icon = icon,
        bottom = bottom,
        content = content)
}

@Composable
fun PrimaryCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: CardColors = CardDefaults.cardColors(),
    border: BorderStroke? = null,
    icon: ImageVector,
    bottom: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val action: () -> Unit = onClick ?:  {}
    Card(modifier = modifier.fillMaxWidth(), onClick = action, colors = colors, border = border) {
        Column(modifier = Modifier.padding(20.dp, 4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                content()
                Spacer(modifier = Modifier.width(6.dp))
                if(onClick != null) {
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceTint, CircleShape)
                            .size(24.dp)
                    )
                }
            }
            Row {
                bottom()
            }
        }
    }
}