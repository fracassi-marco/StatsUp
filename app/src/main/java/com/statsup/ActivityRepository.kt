package com.statsup

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE users (height INTEGER)""")

        db.execSQL(
            """CREATE TABLE activities (
            id INTEGER PRIMARY KEY,
            sportId INTEGER,
            distanceInMeters REAL,
            durationInSeconds INTEGER,
            dateInMillis INTEGER,
            title TEXT)"""
        )

        db.execSQL(
            """CREATE TABLE weights (
            dateInMillis INTEGER PRIMARY KEY,
            kilograms INTEGER)"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "StatsUp.db"
    }
}


object ActivityRepository {

    private val listeners: MutableList<Listener<List<Activity>>> = mutableListOf()
    private var activities: List<Activity> = emptyList()

    fun load(context: Context) {
        val result = mutableListOf<Activity>()
        DbHelper(context).readableDatabase.query(
            "activities",
            null,
            null,
            null,
            null,
            null,
            "dateInMillis"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(
                    Activity(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        Sports.byId(cursor.getLong(cursor.getColumnIndexOrThrow("sportId"))),
                        cursor.getFloat(cursor.getColumnIndexOrThrow("distanceInMeters")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("durationInSeconds")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("dateInMillis")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title"))
                    )
                )
            }
        }

        activities = result
    }


    fun listen(vararg listeners: Listener<List<Activity>>) {
        listeners.forEach {
            this.listeners.add(it)
            it.update(activities)
        }
    }

    private fun update() {
        listeners.forEach { it.update(activities) }
    }

    fun addIfNotExists(context: Context, newActivities: List<Activity>) {
        val toAdd = newActivities.minus(activities)
        if (toAdd.isNotEmpty()) {
            saveAll(context, toAdd)
            activities = activities.plus(toAdd)
        }
        update()
    }

    private fun saveAll(context: Context, toAdd: List<Activity>) {
        toAdd.forEach {
            val values = ContentValues().apply {
                put("id", it.id)
                put("sportId", it.sport.id)
                put("distanceInMeters", it.distanceInMeters)
                put("durationInSeconds", it.durationInSeconds)
                put("dateInMillis", it.dateInMillis)
                put("title", it.title)
            }

            DbHelper(context).writableDatabase.insert("activities", null, values)
        }
    }

    fun removeListener(vararg listeners: Listener<List<Activity>>) {
        listeners.forEach { this.listeners.remove(it) }
    }
}
