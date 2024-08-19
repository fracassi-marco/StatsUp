package com.statsup.ui.components

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.statsup.ui.viewmodel.MainViewModel

@Composable
fun ImportButton(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>, mainViewModel: MainViewModel) {
    Box {
        FloatingActionButton(
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
            modifier = Modifier.align(Alignment.Center).size(70.dp).offset(y = 70.dp),
            onClick = {
                launcher.launch(mainViewModel.startImport())
                mainViewModel.startLoading()
            },
        ) {
            Icon(Icons.Outlined.Autorenew, null, modifier = Modifier.size(44.dp))
        }
    }
}