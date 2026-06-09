package com.statsup.infrastructure.repository

import androidx.room.TypeConverter
import com.statsup.domain.Route

class Converters {
    @TypeConverter
    fun intListToString(value: List<Int>?): String? = value?.joinToString(",")

    @TypeConverter
    fun stringToIntList(value: String?): List<Int>? =
        value?.split(",")?.mapNotNull { it.toIntOrNull() }

    @TypeConverter
    fun routeToString(value: Route?): String? {
        if (value == null) return null
        // Format: "id|resourceState|summaryPolyline" — pipes are safe since polylines use only Base64 chars
        return "${value.id ?: ""}|${value.resourceState ?: ""}|${value.summaryPolyline ?: ""}"
    }

    @TypeConverter
    fun stringToRoute(encoded: String?): Route? {
        if (encoded == null) return null
        val parts = encoded.split("|", limit = 3)
        return if (parts.size == 3) {
            Route(
                id = parts[0].ifEmpty { null },
                resourceState = parts[1].toIntOrNull(),
                summaryPolyline = parts[2].ifEmpty { null }
            )
        } else {
            // Legacy single-value format (plain summaryPolyline)
            Route(summaryPolyline = encoded.ifEmpty { null })
        }
    }
}
