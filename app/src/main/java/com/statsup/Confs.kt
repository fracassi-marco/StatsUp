package com.statsup

import android.content.Context
import java.util.*

class Confs(private val context: Context) {

    companion object {
        val properties = Properties()

        fun load(context: Context) {
            if (properties.isEmpty) {
                properties.load(context.assets.open("application.properties"))
            }
        }
    }

    init {
        load(context)
    }

    fun stravaClientId(): String {
        return properties.getProperty("strava.clientId")
    }

    fun stravaClientSecret(): String {
        return properties.getProperty("strava.clientSecret")
    }
}