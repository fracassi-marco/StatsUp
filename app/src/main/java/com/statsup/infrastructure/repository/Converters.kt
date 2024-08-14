package com.statsup.infrastructure.repository

import androidx.room.TypeConverter
import com.statsup.domain.Route

class Converters {
    @TypeConverter
    fun routeToString(value: Route?): String? {
        return value?.summaryPolyline
    }

    @TypeConverter
    fun stringToRoute(route: String?): Route? {
        return route?.let { Route(summaryPolyline = route) }
    }
}