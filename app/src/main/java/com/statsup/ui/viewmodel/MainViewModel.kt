package com.statsup.ui.viewmodel

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.core.net.toUri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.BuildConfig
import com.statsup.domain.ApiException
import com.statsup.domain.FullImportUseCase
import com.statsup.domain.TrainingApi
import com.statsup.domain.UpdateTrainingsUseCase
import com.statsup.domain.repository.SettingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues.CODE

class MainViewModel(
    private val updateActivitiesUseCase: UpdateTrainingsUseCase,
    private val fullImportUseCase: FullImportUseCase,
    private val settingRepository: SettingRepository,
    private val trainingApi: TrainingApi
) : ViewModel() {

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading
    val newTrainingsCounter = MutableSharedFlow<Int>()
    val importError = MutableSharedFlow<String>()

    private var fullImportPending = false

    private fun updateActivities(token: String) {
        viewModelScope.launch {
            val count = try {
                withContext(Dispatchers.IO) {
                    val activeToken = resolveToken(token)
                    val trainings = if (fullImportPending) {
                        fullImportPending = false
                        fullImportUseCase(activeToken)
                    } else {
                        updateActivitiesUseCase(activeToken)
                    }
                    trainings.count()
                }
            } catch (e: ApiException) {
                Log.e("StatsUp", "API error durante il download attività", e)
                if (e.isAuthError) {
                    clearStoredCredentials()
                }
                stopLoading()
                importError.emit(e.message ?: "Import failed. Try again.")
                return@launch
            } catch (e: Exception) {
                Log.e("StatsUp", "Errore durante il download attività", e)
                stopLoading()
                importError.emit(e.message ?: "Import failed. Try again.")
                return@launch
            }
            stopLoading()
            newTrainingsCounter.emit(count)
        }
    }

    /**
     * Returns a valid access token: if the stored token appears expired, try to refresh it
     * using the saved refresh token before returning. Falls back to [initialToken] if the
     * stored value is missing or refresh fails.
     */
    private suspend fun resolveToken(initialToken: String): String {
        val stored = settingRepository.loadApiToken()?.takeIf { it.isNotBlank() } ?: return initialToken
        val expiry = settingRepository.loadApiTokenExpiry()
        val nowSecs = System.currentTimeMillis() / 1000
        if (expiry == 0L || nowSecs < expiry - 60) return stored

        val refreshToken = settingRepository.loadApiRefreshToken()
        if (refreshToken.isNullOrBlank()) return stored
        return try {
            val newToken = trainingApi.refreshToken(refreshToken)
            settingRepository.saveApiToken(newToken.accessToken)
            settingRepository.saveApiRefreshToken(newToken.refreshToken)
            settingRepository.saveApiTokenExpiry(newToken.expiresAt)
            newToken.accessToken
        } catch (e: Exception) {
            Log.w("StatsUp", "Token refresh failed, proceeding with stored token", e)
            stored
        }
    }

    private fun clearStoredCredentials() {
        settingRepository.saveApiToken("")
        settingRepository.saveApiRefreshToken("")
        settingRepository.saveApiTokenExpiry(0L)
    }

    private fun startLoading() {
        _loading.value = true
    }

    private fun stopLoading() {
        _loading.value = false
    }

    fun onOAuthResult(activityResult: ActivityResult, authService: AuthorizationService) {
        if (activityResult.resultCode == RESULT_OK) {
            val data = activityResult.data
            if (data == null) {
                Log.e("StatsUp", "launcher: result data is null")
                stopLoading()
                return
            }
            val ex = AuthorizationException.fromIntent(data)
            if (ex != null) {
                Log.e("StatsUp", "launcher: $ex")
                stopLoading()
                return
            }
            val result = AuthorizationResponse.fromIntent(data)
            if (result == null) {
                Log.e("StatsUp", "launcher: AuthorizationResponse is null")
                stopLoading()
                return
            }
            val code = result.authorizationCode
            if (code == null) {
                Log.e("StatsUp", "launcher: authorizationCode is null")
                stopLoading()
                return
            }
            viewModelScope.launch {
                try {
                    val oauthToken = withContext(Dispatchers.IO) {
                        trainingApi.exchangeCode(code)
                    }
                    settingRepository.saveApiToken(oauthToken.accessToken)
                    settingRepository.saveApiRefreshToken(oauthToken.refreshToken)
                    if (oauthToken.expiresAt > 0L) {
                        settingRepository.saveApiTokenExpiry(oauthToken.expiresAt)
                    }
                    oauthToken.athleteId?.let { settingRepository.saveAthleteId(it) }
                    updateActivities(oauthToken.accessToken)
                } catch (e: Exception) {
                    Log.e("StatsUp", "Crash durante token exchange", e)
                    stopLoading()
                    importError.emit(e.message ?: "Token exchange failed. Try again.")
                }
            }
        } else {
            stopLoading()
        }
    }

    fun startImport(authService: AuthorizationService): Intent {
        startLoading()
        return buildAuthIntent(authService)
    }

    fun startFullImport(authService: AuthorizationService): Intent {
        fullImportPending = true
        startLoading()
        return buildAuthIntent(authService)
    }

    private fun buildAuthIntent(authService: AuthorizationService): Intent {
        val redirectUri = REDIRECT_URI.toUri()
        val authorizeUri = "https://intervals.icu/oauth/authorize".toUri()
        val tokenUri = "https://intervals.icu/oauth/token".toUri()

        val config = AuthorizationServiceConfiguration(authorizeUri, tokenUri)
        val request = AuthorizationRequest
            .Builder(config, BuildConfig.INTERVALS_ICU_CLIENT_ID, CODE, redirectUri)
            .setScope("ACTIVITY:READ")
            .build()

        return authService.getAuthorizationRequestIntent(request)
    }

    companion object {
        private const val REDIRECT_URI = "statsup://oauth"
    }
}
