package com.statsup

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.statsup.strava.StravaLoginActivity

class StravaLogin(private val context: Context, private val clientId: String) {


    fun makeIntent(): Intent {
        val intent = Intent(context, StravaLoginActivity::class.java)
        intent.putExtra("StravaLoginActivity.EXTRA_LOGIN_URL", makeLoginURL())
        intent.putExtra("StravaLoginActivity.EXTRA_REDIRECT_URL", "oauth://com-sportshub")
        return intent
    }

    private fun makeLoginURL(): String {
        return Uri.parse("https://www.strava.com/oauth/mobile/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", "oauth://com-sportshub")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("approval_prompt", "auto")
            .appendQueryParameter("scope", "activity:read")
            .build().toString()
    }
}