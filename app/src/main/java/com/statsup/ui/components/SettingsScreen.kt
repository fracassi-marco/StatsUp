package com.statsup.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chargemap.compose.numberpicker.NumberPicker
import com.statsup.BuildConfig
import com.statsup.R
import com.statsup.ui.viewmodel.SettingsViewModel
import com.statsup.ui.viewmodel.WeightViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    weightViewModel: WeightViewModel,
    onImportSuccess: () -> Unit = {},
    onFullImport: () -> Unit = {}
) {
    val monthlyGoalSheetState = rememberModalBottomSheetState()
    val monthlyTrainingGoalSheetState = rememberModalBottomSheetState()
    val themeSheetState = rememberModalBottomSheetState()
    val languageSheetState = rememberModalBottomSheetState()
    val heightSheetState = rememberModalBottomSheetState()
    val weightTargetSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val weightImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { weightViewModel.importFromUri(it) }
    }

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

    Column {
        ScreenTitle(text = stringResource(id = R.string.settings_title))
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp, 16.dp)
        ) {
            Title(text = stringResource(R.string.settings_personal_data))
            SettingsClickableComponent(
                icon = Icons.Outlined.EmojiEvents,
                name = R.string.settings_weight_height,
                value = if (weightViewModel.heightCm > 0) "${weightViewModel.heightCm} cm"
                        else stringResource(R.string.settings_weight_not_set),
                onClick = { viewModel.showHeightSheet() }
            )
            Title(text = stringResource(R.string.settings_screen_goals), marginTop = 22.dp)
            SettingsClickableComponent(
                icon = Icons.Outlined.EmojiEvents,
                name = R.string.settings_weight_target,
                value = if (weightViewModel.weightTargetKg > 0) "%.1f kg".format(weightViewModel.weightTargetKg)
                        else stringResource(R.string.settings_weight_not_set),
                onClick = { viewModel.showWeightTargetSheet() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingsToggleComponent(
                icon = Icons.Outlined.AutoMode,
                name = R.string.settings_screen_goals_auto_targets,
                checked = viewModel.autoTargets,
                onCheckedChange = { viewModel.toggleAutoTargets() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            val manualGoalAlpha = if (viewModel.autoTargets) 0.38f else 1f
            SettingsClickableComponent(
                icon = Icons.Outlined.EmojiEvents,
                name = R.string.settings_screen_goals_monthly,
                value = "${viewModel.effectiveMonthlyDistanceGoal()} ${stringResource(id = R.string.km)}",
                onClick = { if (!viewModel.autoTargets) viewModel.showMonthlyGoal() },
                modifier = Modifier.alpha(manualGoalAlpha)
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingsClickableComponent(
                icon = Icons.Outlined.EmojiEvents,
                name = R.string.settings_screen_goals_monthly_trainings,
                value = "${viewModel.effectiveMonthlyTrainingGoal()} ${stringResource(id = R.string.trainings)}",
                onClick = { if (!viewModel.autoTargets) viewModel.showMonthlyTrainingGoal() },
                modifier = Modifier.alpha(manualGoalAlpha)
            )
            Title(text = stringResource(R.string.settings_screen_app), marginTop = 22.dp)
            SettingsClickableComponent(
                icon = Icons.Outlined.Style,
                name = R.string.settings_screen_theme,
                value = viewModel.themeLabel(),
                onClick = { viewModel.showTheme() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            val systemLanguageLabel = stringResource(R.string.settings_screen_language_system)
            SettingsClickableComponent(
                icon = Icons.Outlined.Language,
                name = R.string.settings_screen_language,
                value = viewModel.languageLabel(systemLanguageLabel),
                onClick = { viewModel.showLanguage() }
            )
            Title(text = stringResource(R.string.settings_data_management), marginTop = 22.dp)
            SettingsClickableComponent(
                icon = Icons.Outlined.Upload,
                name = R.string.settings_export_data,
                value = stringResource(R.string.settings_export_to_json),
                onClick = {
                    exportLauncher.launch("statsup_export_${System.currentTimeMillis()}.json")
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingsClickableComponent(
                icon = Icons.Outlined.Download,
                name = R.string.settings_import_data,
                value = stringResource(R.string.settings_import_from_json),
                onClick = {
                    importLauncher.launch("application/json")
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingsClickableComponent(
                icon = Icons.Outlined.Refresh,
                name = R.string.settings_full_import,
                value = stringResource(R.string.settings_full_import_description),
                onClick = { viewModel.showFullImportConfirmDialog() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            SettingsClickableComponent(
                icon = Icons.Outlined.Download,
                name = R.string.weight_import_libra,
                value = stringResource(R.string.weight_import_libra_description),
                onClick = { weightImportLauncher.launch("*/*") }
            )
            if (weightViewModel.isImporting) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            weightViewModel.importMessage?.let { message ->
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(5000)
                    weightViewModel.clearImportMessage()
                }
                Snackbar(modifier = Modifier.padding(16.dp)) { Text(message) }
            }

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

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = TextStyle(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
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
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp),
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
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp),
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
                        label = { value -> viewModel.themeLabel(value) },
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp),
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

        if (viewModel.showLanguageSheet) {
            val systemLabel = stringResource(R.string.settings_screen_language_system)
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideLanguageSheet() },
                sheetState = languageSheetState,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NumberPicker(
                        value = viewModel.language,
                        range = 0..4,
                        onValueChange = { value -> viewModel.language(value) },
                        dividersColor = MaterialTheme.colorScheme.primary,
                        label = { value -> viewModel.languageLabel(value, systemLabel) },
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp),
                    )
                    Button(
                        onClick = {
                            scope.launch { languageSheetState.hide() }.invokeOnCompletion {
                                if (!languageSheetState.isVisible) {
                                    viewModel.saveLanguage()
                                }
                            }
                        }) {
                        Text(text = stringResource(R.string.settings_screen_set_language))
                    }
                }
            }
        }

        if (viewModel.showHeightSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideHeightSheet() },
                sheetState = heightSheetState,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NumberPicker(
                            value = viewModel.heightCm,
                            range = 100..230,
                            onValueChange = { value -> viewModel.heightCm(value) },
                            dividersColor = MaterialTheme.colorScheme.primary,
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp),
                        )
                        Text(text = "cm")
                    }
                    Button(
                        onClick = {
                            scope.launch { heightSheetState.hide() }.invokeOnCompletion {
                                if (!heightSheetState.isVisible) {
                                    viewModel.saveHeight(weightViewModel)
                                }
                            }
                        }) {
                        Text(text = stringResource(R.string.save))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (viewModel.showWeightTargetSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideWeightTargetSheet() },
                sheetState = weightTargetSheetState,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NumberPicker(
                            value = viewModel.weightTargetInt,
                            range = 30..300,
                            onValueChange = { value -> viewModel.weightTargetInt(value) },
                            dividersColor = MaterialTheme.colorScheme.primary,
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp),
                        )
                        Text(text = ".")
                        NumberPicker(
                            value = viewModel.weightTargetDec,
                            range = 0..9,
                            onValueChange = { value -> viewModel.weightTargetDec(value) },
                            dividersColor = MaterialTheme.colorScheme.primary,
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp),
                        )
                        Text(text = " kg")
                    }
                    Button(
                        onClick = {
                            scope.launch { weightTargetSheetState.hide() }.invokeOnCompletion {
                                if (!weightTargetSheetState.isVisible) {
                                    viewModel.saveWeightTarget(weightViewModel)
                                }
                            }
                        }) {
                        Text(text = stringResource(R.string.save))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Full import from Strava confirmation dialog
        if (viewModel.showFullImportDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideFullImportDialog() },
                title = { Text(stringResource(R.string.settings_full_import_confirm_title)) },
                text = { Text(stringResource(R.string.settings_full_import_confirm_body)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.hideFullImportDialog()
                        onFullImport()
                    }) {
                        Text(stringResource(R.string.settings_full_import_action))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideFullImportDialog() }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Import confirmation dialog
        if (viewModel.showImportConfirmDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.hideImportConfirmDialog() },
                title = { Text(stringResource(R.string.settings_import_confirm_title)) },
                text = {
                    Text(stringResource(R.string.settings_import_confirm_body))
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmImport() }) {
                        Text(stringResource(R.string.settings_import_action))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideImportConfirmDialog() }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
}
