package com.statsup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.statsup.domain.UpdateTrainingsUseCase
import com.statsup.infrastructure.StravaTrainingApi
import com.statsup.infrastructure.repository.SharedPreferencesSettingRepository
import com.statsup.infrastructure.repository.TrainingDatabase
import com.statsup.ui.components.BottomMenuBar
import com.statsup.ui.components.HistoryScreen
import com.statsup.ui.components.ImportButton
import com.statsup.ui.components.SettingsScreen
import com.statsup.ui.theme.StatsUpTheme
import com.statsup.ui.viewmodel.HistoryViewModel
import com.statsup.ui.viewmodel.MainViewModel
import com.statsup.ui.viewmodel.SettingsViewModel
import net.openid.appauth.AuthorizationService

class MainActivity : ComponentActivity() {

    private val db: TrainingDatabase by lazy { TrainingDatabase.getInstance(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authService = AuthorizationService(this)
        setContent {
            val navController = rememberNavController()
            val updateActivitiesUseCase = UpdateTrainingsUseCase(db.trainingRepository, db.athleteRepository, StravaTrainingApi())
            val mainViewModel = remember { MainViewModel(updateActivitiesUseCase, authService) }
            val settingsViewModel = remember { SettingsViewModel(SharedPreferencesSettingRepository(applicationContext)) }
            val historyViewModel = remember { HistoryViewModel(db.trainingRepository) }
            val snackBarHostState = remember { SnackbarHostState() }
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
                onResult = { mainViewModel.onStravaResult(it) }
            )
            val context = LocalContext.current
            StatsUpTheme(settingsViewModel) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = { BottomMenuBar(navController) },
                        floatingActionButton = { ImportButton(launcher, mainViewModel) },
                        floatingActionButtonPosition = FabPosition.Center,
                        snackbarHost = { SnackbarHost(snackBarHostState) },
                    ) { innerPadding ->
                        NavHost(navController = navController, startDestination = Screens.Dashboard.route, Modifier.padding(innerPadding)) {
                            composable(Screens.Dashboard.route) { Text(text = "Dashboard") }
                            composable(Screens.History.route) { HistoryScreen(historyViewModel) }
                            composable(Screens.Stats.route) { Text(text = "Stats") }
                            composable(Screens.Settings.route) { SettingsScreen(settingsViewModel) }
                        }
                    }
                    LaunchedEffect(Unit) {
                        mainViewModel.newTrainingsCounter.collect { message ->
                            snackBarHostState.showSnackbar(
                                message = context.getString(R.string.imported, message), duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            }
        }
    }
}