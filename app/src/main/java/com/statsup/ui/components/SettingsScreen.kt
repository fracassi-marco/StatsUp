package com.statsup.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
fun SettingsScreen(viewModel: SettingsViewModel) {
    val monthlyGoalSheetState = rememberModalBottomSheetState()
    val themeSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
            )
        },
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(it)
                .padding(32.dp, 16.dp)
        ) {
            Title(text = stringResource(R.string.settings_screen_goals))
            SettingsClickableComponent(
                icon = Icons.Outlined.AccountBox,
                iconDesc = R.string.settings_screen_goals_monthly,
                name = R.string.settings_screen_goals_monthly,
                value = "${viewModel.monthlyGoal} ${stringResource(id = R.string.km)}",
                onClick = { viewModel.showMonthlyGoal() }
            )
            Title(text = stringResource(R.string.settings_screen_app), marginTop = 22.dp)
            SettingsClickableComponent(
                icon = Icons.Outlined.AccountBox,
                iconDesc = R.string.settings_screen_theme,
                name = R.string.settings_screen_theme,
                value = viewModel.themeLabel(),
                onClick = { viewModel.showTheme() }
            )
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
    }
}