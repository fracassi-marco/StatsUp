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

    val stravaClientId = properties.getProperty("strava.clientId")!!

    val stravaClientSecret = properties.getProperty("strava.clientSecret")!!

    val mapsKey = properties.getProperty("google.maps.key")!!
}