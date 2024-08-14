package com.statsup.ui.viewmodel

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity.RESULT_OK
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.statsup.BuildConfig
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
    private val authService: AuthorizationService
) : ViewModel() {

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading
    val newTrainingsCounter = MutableSharedFlow<Int>()

    fun updateActivities(token: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val trainings = updateActivitiesUseCase(token)
                newTrainingsCounter.emit(trainings.count())
                stopLoading()
            }
        }
    }

    fun startLoading() {
        _loading.value = true
    }

    fun stopLoading() {
        _loading.value = false
    }

    fun onStravaResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == RESULT_OK) {
            val ex = AuthorizationException.fromIntent(activityResult.data!!)
            if (ex != null) {
                Log.e("StatsUp", "launcher: $ex")
                stopLoading()
            } else {
                val result = AuthorizationResponse.fromIntent(activityResult.data!!)!!
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

    fun startImport(): Intent {
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
}