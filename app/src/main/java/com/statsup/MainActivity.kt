package com.statsup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.statsup.domain.UpdateTrainingsUseCase
import com.statsup.infrastructure.StravaTrainingApi
import com.statsup.ui.components.SettingsScreen
import com.statsup.ui.theme.SecondaryText
import com.statsup.ui.theme.StatsUpTheme
import com.statsup.ui.viewmodel.SettingsViewModel
import com.statsup.infrastructure.repository.SharedPreferencesSettingRepository
import com.statsup.infrastructure.repository.TrainingDatabase
import com.statsup.ui.components.HistoryScreen
import com.statsup.ui.viewmodel.HistoryViewModel
import com.statsup.ui.viewmodel.MainViewModel
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.NoClientAuthentication
import net.openid.appauth.ResponseTypeValues.CODE

class MainActivity : ComponentActivity() {

    private lateinit var authService: AuthorizationService
    private val db: TrainingDatabase by lazy { TrainingDatabase.getInstance(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authService = AuthorizationService(this)
        setContent {
            val navController = rememberNavController()
            val mainViewModel by lazy { MainViewModel(UpdateTrainingsUseCase(db.trainingRepository, db.athleteRepository, StravaTrainingApi())) }
            val settingsViewModel by lazy { SettingsViewModel(SharedPreferencesSettingRepository(applicationContext)) }
            val historyViewModel by lazy { HistoryViewModel(db.trainingRepository) }
            StatsUpTheme(settingsViewModel) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult(),
                        onResult = {
                            if (it.resultCode == RESULT_OK) {
                                val ex = AuthorizationException.fromIntent(it.data!!)
                                if (ex != null) {
                                    Log.e("StatsUp", "launcher: $ex")
                                    mainViewModel.stopLoading()
                                } else {
                                    val result = AuthorizationResponse.fromIntent(it.data!!)!!
                                    val tokenRequest =
                                        result.createTokenExchangeRequest(mapOf("client_secret" to BuildConfig.STRAVA_CLIENT_SECRET))
                                    authService.performTokenRequest(tokenRequest, NoClientAuthentication.INSTANCE) { res, exception ->
                                        if (exception != null) {
                                            Log.e("StatsUp", "launcher: ${exception.cause?.message}")
                                            mainViewModel.stopLoading()
                                        } else {
                                            val token = res?.accessToken
                                            mainViewModel.updateActivities(token!!)
                                        }
                                    }
                                }
                            } else {
                                mainViewModel.stopLoading()
                            }
                        }
                    )
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = { NavigationBar(navController) },
                        floatingActionButton = { ImportButton(launcher, mainViewModel) },
                        floatingActionButtonPosition = FabPosition.Center,
                    ) { innerPadding ->
                        NavHost(navController = navController, startDestination = Screens.Dashboard.route, Modifier.padding(innerPadding)) {
                            composable(Screens.Dashboard.route) { Text(text = "Dashboard") }
                            composable(Screens.History.route) { HistoryScreen(historyViewModel) }
                            composable(Screens.Stats.route) { Text(text = "Stats") }
                            composable(Screens.Settings.route) { SettingsScreen(settingsViewModel) }
                        }
                    }
                }
            }
        }
    }

    private fun stravaAuth(): Intent {
        val redirectUri = Uri.parse("oauth://com-sportshub")
        val authorizeUri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
        val tokenUri = Uri.parse("https://www.strava.com/api/v3/oauth/token")

        val config = AuthorizationServiceConfiguration(authorizeUri, tokenUri)
        val request = AuthorizationRequest
            .Builder(config, BuildConfig.STRAVA_CLIENT_ID, CODE, redirectUri)
            .setAdditionalParameters(mapOf("approval_prompt" to "auto"))
            .setScopes("activity:read")
            .build()

        return authService.getAuthorizationRequestIntent(request)
    }

    @Composable
    fun ImportButton(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>, mainViewModel: MainViewModel) {
        Box {
            FloatingActionButton(
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                modifier = Modifier.align(Alignment.Center).size(70.dp).offset(y = 70.dp),
                onClick = {
                    launcher.launch(stravaAuth())
                    mainViewModel.startLoading()
                },
            ) {
                Icon(Icons.Filled.Add, "Localized description")
            }
        }
    }
}

@Composable
fun NavigationBar(navController: NavHostController) {
    NavigationBar(
        modifier = Modifier.border(1.dp, color = SecondaryText),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        Screens.entries.forEach { screen ->
            if (screen == Screens.Separator) {
                Box(modifier = Modifier.size(80.dp, 1.dp))
                return@forEach
            }
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = SecondaryText,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.background
                    ),
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

enum class Screens(val route: String, val icon: ImageVector) {
    Dashboard("dashboard", Icons.Outlined.Home),
    History("history", Icons.AutoMirrored.Outlined.List),
    Separator("", Icons.Filled.Edit),
    Stats("profile", Icons.Outlined.DateRange),
    Settings("settings", Icons.Outlined.Settings)
}