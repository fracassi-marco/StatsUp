package com.statsup.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chargemap.compose.numberpicker.NumberPicker
import com.statsup.R
import com.statsup.domain.BmiCategory
import com.statsup.domain.WeightEntry
import com.statsup.domain.WeightStats
import com.statsup.ui.viewmodel.WeightViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun WeightScreen(
    viewModel: WeightViewModel,
    onNavigateBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<WeightEntry?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.importFromUri(it) } }

    LaunchedEffect(viewModel.importMessage) {
        viewModel.importMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearImportMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.weight_add_entry))
            }
        }
    ) { innerPadding ->
        val stats = viewModel.stats
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                    Text(
                        text = stringResource(R.string.weight_screen_title),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )
                    IconButton(onClick = { importLauncher.launch("*/*") }) {
                        Icon(Icons.Outlined.Download, contentDescription = stringResource(R.string.weight_import_libra))
                    }
                }
            }

            item { WeightHeaderCard(stats) }

            if (stats.bmi != null) {
                item { BmiCard(stats) }
            }

            if (stats.chartPoints.size >= 2) {
                item { WeightChartCard(stats, viewModel.weightTargetKg) }
            }

            if (viewModel.weightTargetKg > 0 && stats.latestWeight != null) {
                item { WeightTargetCard(stats, viewModel.weightTargetKg) }
            }

            if (stats.totalMeasurements > 0) {
                item { WeightGamificationCard(stats) }
            }

            if (stats.chartPoints.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.weight_recent_entries),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                val recent = viewModel.entries.sortedByDescending { it.date }.take(10)
                items(recent) { entry ->
                    WeightEntryRow(
                        dateMillis = entry.date,
                        kg = entry.weightKg,
                        onDeleteRequest = { entryToDelete = entry }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        if (viewModel.isImporting) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    if (showAddDialog) {
        AddWeightDialog(
            initialWeight = viewModel.stats.latestWeight ?: 70.0,
            onDismiss = { showAddDialog = false },
            onConfirm = { kg ->
                viewModel.addWeight(kg)
                showAddDialog = false
            }
        )
    }

    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text(stringResource(R.string.weight_delete_confirm_title)) },
            text = { Text(stringResource(R.string.weight_delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(entry.id)
                    entryToDelete = null
                }) {
                    Text(stringResource(R.string.delete_training_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun WeightHeaderCard(stats: WeightStats) {
    val kgUnit = stringResource(R.string.weight_unit_kg)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (stats.latestWeight != null) {
                Text(
                    text = "%.1f $kgUnit".format(stats.latestWeight),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                val delta = stats.previousWeight?.let { stats.latestWeight - it }
                if (delta != null) {
                    val sign = if (delta >= 0) "+" else ""
                    Text(
                        text = "$sign%.1f $kgUnit".format(delta),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (delta <= 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.weight_no_entries),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = stringResource(R.string.weight_measurements_count, stats.totalMeasurements),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun BmiCard(stats: WeightStats) {
    val bmi = stats.bmi ?: return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weight_bmi),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = stringResource(R.string.weight_bmi_display, bmi, bmiLabel(stats.bmiCategory)),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = bmiColor(stats.bmiCategory)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            BmiBar(bmi)
        }
    }
}

@Composable
private fun bmiLabel(category: BmiCategory): String = when (category) {
    BmiCategory.UNDERWEIGHT -> stringResource(R.string.weight_bmi_underweight)
    BmiCategory.NORMAL -> stringResource(R.string.weight_bmi_normal)
    BmiCategory.OVERWEIGHT -> stringResource(R.string.weight_bmi_overweight)
    BmiCategory.OBESE_1 -> stringResource(R.string.weight_bmi_obese1)
    BmiCategory.OBESE_2 -> stringResource(R.string.weight_bmi_obese2)
    BmiCategory.OBESE_3 -> stringResource(R.string.weight_bmi_obese3)
}

@Composable
private fun bmiColor(category: BmiCategory): Color = when (category) {
    BmiCategory.UNDERWEIGHT -> Color(0xFF2196F3)
    BmiCategory.NORMAL -> Color(0xFF4CAF50)
    BmiCategory.OVERWEIGHT -> Color(0xFFFFC107)
    BmiCategory.OBESE_1 -> Color(0xFFFF9800)
    BmiCategory.OBESE_2 -> Color(0xFFF44336)
    BmiCategory.OBESE_3 -> Color(0xFF9C27B0)
}

@Composable
private fun BmiBar(bmi: Double) {
    val segments = listOf(
        Color(0xFF2196F3) to 18.5f,
        Color(0xFF4CAF50) to 25.0f,
        Color(0xFFFFC107) to 30.0f,
        Color(0xFFFF9800) to 35.0f,
        Color(0xFFF44336) to 40.0f,
        Color(0xFF9C27B0) to 50.0f
    )
    val totalRange = 50f - 10f
    val clampedBmi = bmi.coerceIn(10.0, 50.0).toFloat()
    val indicatorFraction = (clampedBmi - 10f) / totalRange

    Column {
        Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(7.dp))) {
            Row(modifier = Modifier.fillMaxSize()) {
                val bounds = listOf(10f, 18.5f, 25f, 30f, 35f, 40f, 50f)
                val colors = segments.map { it.first }
                for (i in colors.indices) {
                    val segWidth = (bounds[i + 1] - bounds[i]) / totalRange
                    Box(
                        modifier = Modifier
                            .weight(segWidth)
                            .fillMaxSize()
                            .background(colors[i])
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(indicatorFraction)
            ) {
                Text(
                    text = "▲",
                    modifier = Modifier.align(Alignment.BottomEnd),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("10", "18.5", "25", "30", "35", "40", "50").forEach { label ->
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightChartCard(stats: WeightStats, targetKg: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.weight_chart_title),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            WeightLineChart(
                points = stats.chartPoints,
                targetKg = targetKg
            )
        }
    }
}

@Composable
private fun WeightTargetCard(stats: WeightStats, targetKg: Double) {
    val kgUnit = stringResource(R.string.weight_unit_kg)
    val latest = stats.latestWeight ?: return
    val startWeight = if (stats.weightLostFromMax > 0) latest + stats.weightLostFromMax else latest
    val progress = if (startWeight > targetKg) {
        ((startWeight - latest) / (startWeight - targetKg)).toFloat().coerceIn(0f, 1f)
    } else 1f

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.weight_target, "%.1f $kgUnit".format(targetKg)),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "%.0f%%".format(progress * 100),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val predictionText = when {
                !stats.canReachTarget -> stringResource(R.string.weight_prediction_never)
                stats.predictedTargetDate != null -> {
                    val fmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                    stringResource(R.string.weight_prediction_date, stats.predictedTargetDate.format(fmt))
                }
                else -> stringResource(R.string.weight_prediction_insufficient_data)
            }
            Text(
                text = predictionText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            if (stats.weeklyRate != 0.0) {
                val sign = if (stats.weeklyRate >= 0) "+" else ""
                Text(
                    text = stringResource(R.string.weight_weekly_rate, "$sign%.2f $kgUnit".format(stats.weeklyRate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun WeightGamificationCard(stats: WeightStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.weight_gamification_title),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.weight_streak_weeks, stats.measurementStreak),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = stringResource(R.string.weight_streak_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                if (stats.personalBest != null || stats.maxWeight != null) {
                    val kgUnit = stringResource(R.string.weight_unit_kg)
                    if (stats.personalBest != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "%.1f $kgUnit".format(stats.personalBest),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = stringResource(R.string.weight_personal_best_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    if (stats.maxWeight != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "%.1f $kgUnit".format(stats.maxWeight),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = stringResource(R.string.weight_max_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
            val earnedBadges = stats.earnedBadges.filter { it.earned }
            if (earnedBadges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    earnedBadges.forEach { badge ->
                        Text(text = badge.emoji, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightEntryRow(
    dateMillis: Long,
    kg: Double,
    onDeleteRequest: () -> Unit
) {
    val kgUnit = stringResource(R.string.weight_unit_kg)
    val date = Instant.ofEpochMilli(dateMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = date, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "%.1f $kgUnit".format(kg),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = onDeleteRequest) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.weight_delete_confirm_title),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWeightDialog(
    initialWeight: Double = 70.0,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var integerPart by remember { mutableIntStateOf(initialWeight.toInt().coerceIn(30, 300)) }
    var decimalPart by remember { mutableIntStateOf(((initialWeight * 10).toInt() % 10).coerceIn(0, 9)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.weight_add_entry),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                NumberPicker(
                    value = integerPart,
                    range = 30..300,
                    onValueChange = { integerPart = it },
                    dividersColor = MaterialTheme.colorScheme.primary,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = ".",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                NumberPicker(
                    value = decimalPart,
                    range = 0..9,
                    onValueChange = { decimalPart = it },
                    dividersColor = MaterialTheme.colorScheme.primary,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.weight_unit_kg),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val kg = integerPart + decimalPart / 10.0
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onConfirm(kg)
                        }
                    }
                }
            ) {
                Text(text = stringResource(R.string.save))
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.cancel))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
