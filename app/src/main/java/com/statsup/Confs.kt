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

    fun stravaClientId(): Int {
        return properties.getProperty("strava.clientId").toInt()
    }

    fun stravaClientSecret(): String {
        return properties.getProperty("strava.clientSecret")
    }
}