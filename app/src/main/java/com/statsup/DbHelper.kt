package com.statsup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE users (height INTEGER)""")

        createActivitiesTable(db)

        db.execSQL(
            """CREATE TABLE weights (
            dateInMillis INTEGER PRIMARY KEY,
            kilograms INTEGER)"""
        )
    }

    private fun createActivitiesTable(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE activities (
                id INTEGER PRIMARY KEY,
                sportId INTEGER,
                distanceInMeters REAL,
                durationInSeconds INTEGER,
                dateInMillis INTEGER,
                title TEXT,
                maxSpeedInMetersPerSecond REAL,
                elevationInMeters REAL,
                map TEXT,
                movingTimeInSeconds INTEGER,
                elevHighInMeters REAL,
                elevLowInMeters REAL)"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val migrations = mapOf(
            1 to listOf("ALTER TABLE activities ADD elevationInMeters REAL DEFAULT 0"),
            2 to listOf("ALTER TABLE activities ADD map TEXT DEFAULT NULL"),
            3 to listOf(
                "ALTER TABLE activities ADD movingTimeInSeconds INTEGER DEFAULT 0",
                "ALTER TABLE activities ADD elevHighInMeters REAL DEFAULT 0",
                "ALTER TABLE activities ADD elevLowInMeters REAL DEFAULT 0"
            )
        )
        oldVersion.until(newVersion).forEach {
            migrations[it]!!.forEach { migration ->
                db.execSQL(migration)
            }
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("""DROP TABLE activities""")
        createActivitiesTable(db)
    }

    companion object {
        const val DATABASE_VERSION = 4
        const val DATABASE_NAME = "StatsUp.db"
    }
}