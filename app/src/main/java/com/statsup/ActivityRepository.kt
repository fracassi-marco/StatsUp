package com.statsup

import android.content.ContentValues
import android.content.Context
import com.statsup.Sports.All


object ActivityRepository {

    private var activities: List<Activity> = emptyList()
    var selectedSportPosition = 0

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
                            cursor.getInt(cursor.getColumnIndexOrThrow("movingTimeInSeconds")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("dateInMillis")),
                            cursor.getString(cursor.getColumnIndexOrThrow("title")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("maxSpeedInMetersPerSecond")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("elevationInMeters")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("elevHighInMeters")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("elevLowInMeters")),
                            cursor.getString(cursor.getColumnIndexOrThrow("map"))
                        )
                    )
                }
            }
        }

        activities = result
    }

    fun saveAll(context: Context, toAdd: List<Activity>) {
        DbHelper(context).writableDatabase.use { connection ->
            toAdd.forEach {
                val values = ContentValues().apply {
                    put("id", it.id)
                    put("sportId", it.sport.id)
                    put("distanceInMeters", it.distanceInMeters)
                    put("durationInSeconds", it.durationInSeconds)
                    put("dateInMillis", it.dateInMillis)
                    put("title", it.title)
                    put("maxSpeedInMetersPerSecond", it.maxSpeedInMetersPerSecond)
                    put("elevationInMeters", it.elevationInMeters)
                    put("map", it.map)
                    put("movingTimeInSeconds", it.movingTimeInSeconds)
                    put("elevHighInMeters", it.elevHighInMeters)
                    put("elevLowInMeters", it.elevLowInMeters)
                }

                connection.insert("activities", null, values)
            }
        }
        activities = toAdd
    }

    fun anyActivities()= activities.isNotEmpty()

    fun selectedSportPosition() = selectedSportPosition

    fun sports(): List<Sports> = listOf(All) + activities.map { it.sport }.distinct()

    fun filterBySelectedSport() =
        if(selectedSportPosition == 0)
            activities
        else
            activities.filter { it.sport == sports()[selectedSportPosition] }

    fun changeSport(position: Int): Boolean {
        val isChanged = selectedSportPosition != position
        selectedSportPosition = position

        return isChanged
    }

    fun byId(id: Long)= activities.single { it.id == id }
}
