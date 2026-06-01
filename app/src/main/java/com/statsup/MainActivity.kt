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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.statsup.domain.FullImportUseCase
import com.statsup.domain.UpdateTrainingsUseCase
import com.statsup.infrastructure.ActivityExportService
import kotlinx.coroutines.launch
import com.statsup.infrastructure.TrainingShareService
import com.statsup.infrastructure.StravaTrainingApi
import com.statsup.infrastructure.repository.SharedPreferencesSettingRepository
import com.statsup.infrastructure.repository.TrainingDatabase
import com.statsup.infrastructure.service.DataExportImportService
import com.statsup.ui.components.AllRoutesMapScreen
import com.statsup.ui.components.BookmarksScreen
import com.statsup.ui.components.BottomMenuBar
import com.statsup.ui.components.DashboardScreen
import com.statsup.ui.components.HistoryScreen
import com.statsup.ui.components.ImportButton
import com.statsup.ui.components.LoadingBox
import com.statsup.ui.components.MapFullscreenScreen
import com.statsup.ui.components.LevelsScreen
import com.statsup.ui.components.RecoveryDetailScreen
import com.statsup.ui.components.ProfileScreen
import com.statsup.ui.components.SettingsScreen
import com.statsup.ui.components.SplashScreen
import com.statsup.ui.components.StatsScreen
import com.statsup.ui.components.TrainingDetailScreen
import com.statsup.ui.components.WelcomeScreen
import com.statsup.ui.theme.StatsUpTheme
import com.statsup.ui.viewmodel.AllRoutesViewModel
import com.statsup.ui.viewmodel.BookmarksViewModel
import com.statsup.ui.viewmodel.DashboardViewModel
import com.statsup.ui.viewmodel.HistoryViewModel
import com.statsup.ui.viewmodel.MainViewModel
import com.statsup.ui.viewmodel.ProfileViewModel
import com.statsup.ui.viewmodel.SettingsViewModel
import com.statsup.ui.viewmodel.StatsViewModel
import com.statsup.ui.viewmodel.TrainingDetailViewModel
import net.openid.appauth.AuthorizationService

class MainActivity : ComponentActivity() {

    private val db: TrainingDatabase by lazy { TrainingDatabase.getInstance(application) }
    private var authService: AuthorizationService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authService = AuthorizationService(this)
        setContent {
            val settingRepository = remember { SharedPreferencesSettingRepository(applicationContext) }
            val dataExportImportService = remember {
                DataExportImportService(
                    applicationContext,
                    db,
                    settingRepository
                )
            }
            val updateActivitiesUseCase = remember { UpdateTrainingsUseCase(db.trainingRepository, db.athleteRepository, StravaTrainingApi()) }
            val fullImportUseCase = remember { FullImportUseCase(db.trainingRepository, db.athleteRepository, db.bookmarkedTrainingRepository, StravaTrainingApi()) }
            val navController = rememberNavController()
            val mainViewModel: MainViewModel = viewModel { MainViewModel(updateActivitiesUseCase, fullImportUseCase, settingRepository) }
            val settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(settingRepository, db.trainingRepository, dataExportImportService, applicationContext) }
            val historyViewModel: HistoryViewModel = viewModel { HistoryViewModel(db.trainingRepository) }
            val dashboardViewModel: DashboardViewModel = viewModel { DashboardViewModel(application, db.trainingRepository, settingRepository) }
            val statsViewModel: StatsViewModel = viewModel { StatsViewModel(db.trainingRepository) }
            val allRoutesViewModel: AllRoutesViewModel = viewModel { AllRoutesViewModel(db.trainingRepository) }
            val bookmarksViewModel: BookmarksViewModel = viewModel { BookmarksViewModel(db.bookmarkedTrainingRepository, db.trainingRepository) }
            val profileViewModel: ProfileViewModel = viewModel { ProfileViewModel(db.trainingRepository, db.athleteRepository, settingRepository, applicationContext) }
            val snackBarHostState = remember { SnackbarHostState() }
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
                onResult = { result -> authService?.let { mainViewModel.onStravaResult(result, it) } }
            )
            val fullImportLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
                onResult = { result -> authService?.let { mainViewModel.onStravaResult(result, it) } }
            )

            val isInitialLoading = historyViewModel.isInitialLoading.value
            val hasTrainings = historyViewModel.state.value.show

            StatsUpTheme(settingsViewModel) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // Mostra SplashScreen durante il caricamento iniziale
                    if (isInitialLoading) {
                        SplashScreen()
                    } else {
                        LoadingBox(isLoading = mainViewModel.loading.value) {
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                bottomBar = {
                                    // Menu sempre visibile, ma disabilitato se non ci sono allenamenti
                                    BottomMenuBar(navController, enabled = hasTrainings)
                                },
                                floatingActionButton = { authService?.let { service -> ImportButton(launcher, mainViewModel, service) } },
                                floatingActionButtonPosition = FabPosition.Center,
                                snackbarHost = { SnackbarHost(snackBarHostState) },
                            ) { innerPadding ->
                            // Se non ci sono allenamenti, mostra la WelcomeScreen
                            if (!hasTrainings) {
                                WelcomeScreen()
                            } else {
                                NavHost(navController = navController, startDestination = Screens.Dashboard.route, Modifier.padding(innerPadding)) {
                                composable(Screens.Dashboard.route) {
                                    DashboardScreen(
                                        viewModel = dashboardViewModel,
                                        onProfileClick = { navController.navigate(Screens.PROFILE) },
                                        onLevelClick = { navController.navigate(Screens.LEVELS) },
                                        onRecoveryClick = { navController.navigate(Screens.RECOVERY_DETAIL) }
                                    )
                                }
                                composable(Screens.PROFILE) {
                                    ProfileScreen(profileViewModel) { navController.popBackStack() }
                                }
                                composable(Screens.LEVELS) {
                                    LevelsScreen(dashboardViewModel) { navController.popBackStack() }
                                }
                                composable(Screens.RECOVERY_DETAIL) {
                                    RecoveryDetailScreen(dashboardViewModel) { navController.popBackStack() }
                                }
                                composable(Screens.History.route) {
                                    HistoryScreen(historyViewModel) { trainingId ->
                                        navController.navigate(Screens.trainingDetailRoute(trainingId))
                                    }
                                }
                                composable(Screens.Bookmarks.route) {
                                    BookmarksScreen(bookmarksViewModel) { trainingId ->
                                        navController.navigate(Screens.trainingDetailRoute(trainingId))
                                    }
                                }
                                composable(Screens.Map.route) { AllRoutesMapScreen(allRoutesViewModel) }
                                composable(Screens.Stats.route) { StatsScreen(statsViewModel) }
                                composable(Screens.Settings.route) {
                                    SettingsScreen(
                                        viewModel = settingsViewModel,
                                        onImportSuccess = {
                                            // Navigate to dashboard after successful import
                                            navController.navigate(Screens.Dashboard.route) {
                                                popUpTo(Screens.Dashboard.route) { inclusive = true }
                                            }
                                        },
                                        onFullImportFromStrava = {
                                            authService?.let { service ->
                                                fullImportLauncher.launch(mainViewModel.startFullImport(service))
                                            }
                                        }
                                    )
                                }

                                // Training Detail Screen
                                composable(
                                    route = Screens.TRAINING_DETAIL,
                                    arguments = listOf(navArgument("trainingId") { type = NavType.LongType })
                                ) { backStackEntry ->
                                    val trainingId = backStackEntry.arguments?.getLong("trainingId") ?: 0L
                                    val context = LocalContext.current
                                    val scope = rememberCoroutineScope()
                                    val detailViewModel: TrainingDetailViewModel = viewModel(backStackEntry) {
                                        TrainingDetailViewModel(
                                            db.trainingRepository,
                                            db.bookmarkedTrainingRepository,
                                            settingRepository,
                                            StravaTrainingApi(),
                                            trainingId
                                        )
                                    }
                                    TrainingDetailScreen(
                                        training = detailViewModel.training.value,
                                        isLoading = detailViewModel.isLoading.value,
                                        isBookmarked = detailViewModel.isBookmarked.value,
                                        bookmarkNote = detailViewModel.bookmarkNote.value,
                                        customTitle = detailViewModel.customTitle.value,
                                        difficulty = detailViewModel.difficulty.value,
                                        laps = detailViewModel.laps.value,
                                        showBookmarkDialog = detailViewModel.showBookmarkDialog.value,
                                        onNavigateBack = { navController.popBackStack() },
                                        onOpenFullscreenMap = { navController.navigate(Screens.mapFullscreenRoute(trainingId)) },
                                        onToggleBookmark = { detailViewModel.toggleBookmark() },
                                        onShare = {
                                            detailViewModel.training.value?.let { t ->
                                                TrainingShareService.share(context, t)
                                            }
                                        },
                                        onExportGpx = {
                                            detailViewModel.training.value?.let { t ->
                                                scope.launch { ActivityExportService.exportGpx(context, t) }
                                            }
                                        },
                                        onExportTcx = {
                                            detailViewModel.training.value?.let { t ->
                                                scope.launch { ActivityExportService.exportTcx(context, t) }
                                            }
                                        },
                                        onDismissDialog = { detailViewModel.dismissBookmarkDialog() },
                                        onConfirmBookmark = { note, customTitle, difficulty ->
                                            detailViewModel.addBookmarkWithNote(note, customTitle, difficulty)
                                        },
                                        onRemoveBookmark = { detailViewModel.removeBookmark() },
                                        showDeleteDialog = detailViewModel.showDeleteDialog.value,
                                        onRequestDelete = { detailViewModel.requestDeleteTraining() },
                                        onDismissDeleteDialog = { detailViewModel.dismissDeleteDialog() },
                                        onConfirmDelete = {
                                            detailViewModel.confirmDeleteTraining {
                                                navController.popBackStack()
                                            }
                                        }
                                    )
                                }

                                // Map Fullscreen Screen
                                composable(
                                    route = Screens.MAP_FULLSCREEN,
                                    arguments = listOf(navArgument("trainingId") { type = NavType.LongType })
                                ) { backStackEntry ->
                                    val trainingId = backStackEntry.arguments?.getLong("trainingId") ?: 0L
                                    val detailViewModel: TrainingDetailViewModel = viewModel(backStackEntry) {
                                        TrainingDetailViewModel(
                                            db.trainingRepository,
                                            db.bookmarkedTrainingRepository,
                                            settingRepository,
                                            StravaTrainingApi(),
                                            trainingId
                                        )
                                    }
                                    MapFullscreenScreen(
                                        training = detailViewModel.training.value,
                                        isLoading = detailViewModel.isLoading.value,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                            }
                            }  // chiude NavHost e else
                        }  // chiude Scaffold content lambda

                        LaunchedEffect(Unit) {
                            mainViewModel.newTrainingsCounter.collect { count ->
                                snackBarHostState.showSnackbar(
                                    message = "$count activities have been imported",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        }  // chiude LoadingBox
                    }  // chiude else (isInitialLoading)
                }  // chiude Surface
            }  // chiude StatsUpTheme
        }  // chiude setContent
    }  // chiude onCreate

    override fun onDestroy() {
        authService?.dispose()
        authService = null
        super.onDestroy()
    }
}