package com.statsup

import android.content.ContentValues
import android.content.Context


object ActivityRepository {

    private var activities: List<Activity> = emptyList()

    fun clean(context: Context) {
        DbHelper(context).writableDatabase.use { it.delete("activities", null, null) }
        activities = emptyList()
    }

    fun load(context: Context) {
        val result = mutableListOf<Activity>()
        DbHelper(context).readableDatabase.use {
            it.query(
                "activities",
                null,
                null,
                null,
                null,
                null,
                "dateInMillis DESC"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    result.add(
                        Activity(
                            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                            Sports.byId(cursor.getLong(cursor.getColumnIndexOrThrow("sportId"))),
                            cursor.getFloat(cursor.getColumnIndexOrThrow("distanceInMeters")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("durationInSeconds")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("dateInMillis")),
                            cursor.getString(cursor.getColumnIndexOrThrow("title")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("maxSpeedInMetersPerSecond"))
                        )
                    )
                }
            }
        }

        activities = result
    }

    fun saveAll(context: Context, toAdd: List<Activity>) {
        toAdd.forEach {
            val values = ContentValues().apply {
                put("id", it.id)
                put("sportId", it.sport.id)
                put("distanceInMeters", it.distanceInMeters)
                put("durationInSeconds", it.durationInSeconds)
                put("dateInMillis", it.dateInMillis)
                put("title", it.title)
            }

            DbHelper(context).writableDatabase.use { it.insert("activities", null, values) }
        }
        activities = toAdd
    }

    fun anyActivities()= activities.isNotEmpty()

    fun all(): List<Activity> = activities
}
