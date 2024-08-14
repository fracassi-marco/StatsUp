package com.statsup.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Title(text: String, marginTop: Dp = 0.dp, marginStart: Dp = 0.dp) {
    Text(text = text,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
        modifier = Modifier.padding(marginStart, marginTop, 0.dp, 6.dp)
    )
}
