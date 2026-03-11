package com.statsup.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chargemap.compose.numberpicker.NumberPicker
import com.statsup.R
import com.statsup.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onImportSuccess: () -> Unit = {}
) {
    val monthlyGoalSheetState = rememberModalBottomSheetState()
    val monthlyTrainingGoalSheetState = rememberModalBottomSheetState()
    val themeSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Handle successful import - navigate to dashboard
    LaunchedEffect(viewModel.importSuccessful) {
        if (viewModel.importSuccessful) {
            viewModel.resetImportSuccessful()
            onImportSuccess()
        }
    }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportData(it) }
    }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.showImportConfirmDialog(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0)
            )
        },
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(it)
                .padding(20.dp, 16.dp)
        ) {
            Title(text = stringResource(R.string.settings_screen_goals))
            SettingsClickableComponent(
                icon = Icons.Outlined.EmojiEvents,
                name = R.string.settings_screen_goals_monthly,
                value = "${viewModel.monthlyGoal} ${stringResource(id = R.string.km)}",
                onClick = { viewModel.showMonthlyGoal() }
            )
            SettingsClickableComponent(
                icon = Icons.Outlined.EmojiEvents,
                name = R.string.settings_screen_goals_monthly_trainings,
                value = "${viewModel.monthlyTrainingGoal} ${stringResource(id = R.string.trainings)}",
                onClick = { viewModel.showMonthlyTrainingGoal() }
            )
            Title(text = stringResource(R.string.settings_screen_app), marginTop = 22.dp)
            SettingsClickableComponent(
                icon = Icons.Outlined.Style,
                name = R.string.settings_screen_theme,
                value = viewModel.themeLabel(),
                onClick = { viewModel.showTheme() }
            )
            Title(text = "Data Management", marginTop = 22.dp)
            SettingsClickableComponent(
                icon = Icons.Outlined.Upload,
                name = R.string.settings_export_data,
                value = "Export to JSON",
                onClick = {
                    exportLauncher.launch("statsup_export_${System.currentTimeMillis()}.json")
                }
            )
            SettingsClickableComponent(
                icon = Icons.Outlined.Download,
                name = R.string.settings_import_data,
                value = "Import from JSON",
                onClick = {
                    importLauncher.launch("application/json")
                }
            )

            // Loading indicator
            if (viewModel.isExportImportLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Success/Error message
            viewModel.exportImportMessage?.let { message ->
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(5000)
                    viewModel.clearExportImportMessage()
                }
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(message)
                }
            }
        }
        if (viewModel.showMonthlyGoalSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.hideMonthlyGoalSheet()
                },
                sheetState = monthlyGoalSheetState,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NumberPicker(
                            value = viewModel.monthlyGoal,
                            range = 0..500,
                            onValueChange = { value -> viewModel.monthlyGoal(value) },
                            dividersColor = MaterialTheme.colorScheme.primary,
                        )
                        Text(text = stringResource(R.string.km))
                    }
                    Button(
                        onClick = {
                            scope.launch { monthlyGoalSheetState.hide() }.invokeOnCompletion {
                                if (!monthlyGoalSheetState.isVisible) {
                                    viewModel.saveMonthlyGoal()
                                }
                            }
                        }) {
                        Text(text = stringResource(R.string.settings_screen_set_goal))
                    }
                }
            }
        }
        if (viewModel.showMonthlyTrainingGoalSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.hideMonthlyTrainingGoalSheet()
                },
                sheetState = monthlyTrainingGoalSheetState,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NumberPicker(
                            value = viewModel.monthlyTrainingGoal,
                            range = 0..100,
                            onValueChange = { value -> viewModel.monthlyTrainingGoal(value) },
                            dividersColor = MaterialTheme.colorScheme.primary,
                        )
                        Text(text = stringResource(R.string.trainings))
                    }
                    Button(
                        onClick = {
                            scope.launch { monthlyTrainingGoalSheetState.hide() }.invokeOnCompletion {
                                if (!monthlyTrainingGoalSheetState.isVisible) {
                                    viewModel.saveMonthlyTrainingGoal()
                                }
                            }
                        }) {
                        Text(text = stringResource(R.string.settings_screen_set_goal))
                    }
                }
            }
        }
        if (viewModel.showThemeSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.hideThemeSheet()
                },
                sheetState = themeSheetState,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NumberPicker(
                        value = viewModel.theme,
                        range = 0..2,
                        onValueChange = { value -> viewModel.theme(value) },
                        dividersColor = MaterialTheme.colorScheme.primary,
                        label = { value -> viewModel.themeLabel(value) }
                    )
                    Button(
                        onClick = {
                            scope.launch { themeSheetState.hide() }.invokeOnCompletion {
                                if (!themeSheetState.isVisible) {
                                    viewModel.saveTheme()
                                }
                            }
                        }) {
                        Text(text = stringResource(R.string.settings_screen_set_theme))
                    }
                }
            }
        }

        // Import confirmation dialog
        if (viewModel.showImportConfirmDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideImportConfirmDialog() },
                title = { Text("Import Data") },
                text = {
                    Text("This will delete all current data and replace it with the imported data. This action cannot be undone. Are you sure you want to continue?")
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmImport() }) {
                        Text("Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideImportConfirmDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}