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
import com.statsup.domain.FullImportUseCase
import com.statsup.domain.UpdateTrainingsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.NoClientAuthentication
import net.openid.appauth.ResponseTypeValues.CODE

class MainViewModel(
    private val updateActivitiesUseCase: UpdateTrainingsUseCase,
    private val fullImportUseCase: FullImportUseCase
) : ViewModel() {

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading
    val newTrainingsCounter = MutableSharedFlow<Int>()

    private var fullImportPending = false

    private fun updateActivities(token: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val trainings = if (fullImportPending) {
                    fullImportPending = false
                    fullImportUseCase(token)
                } else {
                    updateActivitiesUseCase(token)
                }
                newTrainingsCounter.emit(trainings.count())
                stopLoading()
            }
        }
    }

    private fun startLoading() {
        _loading.value = true
    }

    private fun stopLoading() {
        _loading.value = false
    }

    fun onStravaResult(activityResult: ActivityResult, authService: AuthorizationService) {
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
            } else {
                val result = AuthorizationResponse.fromIntent(data)
                if (result == null) {
                    Log.e("StatsUp", "launcher: AuthorizationResponse is null")
                    stopLoading()
                    return
                }
                val tokenRequest = result.createTokenExchangeRequest(mapOf("client_secret" to BuildConfig.STRAVA_CLIENT_SECRET))
                authService.performTokenRequest(tokenRequest, NoClientAuthentication.INSTANCE) { res, exception ->
                    if (exception != null) {
                        Log.e("StatsUp", "launcher: ${exception.cause?.message}")
                        stopLoading()
                    } else {
                        val token = res?.accessToken
                        updateActivities(token!!)
                    }
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
        val redirectUri = "oauth://com-sportshub".toUri()
        val authorizeUri = "https://www.strava.com/oauth/mobile/authorize".toUri()
        val tokenUri = "https://www.strava.com/api/v3/oauth/token".toUri()

        val config = AuthorizationServiceConfiguration(authorizeUri, tokenUri)
        val request = AuthorizationRequest
            .Builder(config, BuildConfig.STRAVA_CLIENT_ID, CODE, redirectUri)
            .setAdditionalParameters(mapOf("approval_prompt" to "auto"))
            .setScopes("activity:read")
            .build()

        return authService.getAuthorizationRequestIntent(request)
    }
}