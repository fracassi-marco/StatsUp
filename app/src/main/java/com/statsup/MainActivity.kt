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
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.statsup.domain.UpdateTrainingsUseCase
import com.statsup.infrastructure.StravaTrainingApi
import com.statsup.infrastructure.repository.SharedPreferencesSettingRepository
import com.statsup.infrastructure.repository.TrainingDatabase
import com.statsup.ui.components.BottomMenuBar
import com.statsup.ui.components.DashboardScreen
import com.statsup.ui.components.HistoryScreen
import com.statsup.ui.components.ImportButton
import com.statsup.ui.components.LoadingBox
import com.statsup.ui.components.SettingsScreen
import com.statsup.ui.theme.StatsUpTheme
import com.statsup.ui.viewmodel.DashboardViewModel
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
            val settingRepository = SharedPreferencesSettingRepository(applicationContext)
            val updateActivitiesUseCase = UpdateTrainingsUseCase(db.trainingRepository, db.athleteRepository, StravaTrainingApi())
            val navController = rememberNavController()
            val mainViewModel = remember { MainViewModel(updateActivitiesUseCase, authService) }
            val settingsViewModel = remember { SettingsViewModel(settingRepository) }
            val historyViewModel = remember { HistoryViewModel(db.trainingRepository) }
            val dashboardViewModel = remember { DashboardViewModel(db.trainingRepository, settingRepository) }
            val snackBarHostState = remember { SnackbarHostState() }
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
                onResult = { mainViewModel.onStravaResult(it) }
            )
            val context = LocalContext.current
            StatsUpTheme(settingsViewModel) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LoadingBox(isLoading = mainViewModel.loading.value) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = { BottomMenuBar(navController) },
                            floatingActionButton = { ImportButton(launcher, mainViewModel) },
                            floatingActionButtonPosition = FabPosition.Center,
                            snackbarHost = { SnackbarHost(snackBarHostState) },
                        ) { innerPadding ->
                            NavHost(navController = navController, startDestination = Screens.Dashboard.route, Modifier.padding(innerPadding)) {
                                composable(Screens.Dashboard.route) { DashboardScreen(dashboardViewModel) }
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
}