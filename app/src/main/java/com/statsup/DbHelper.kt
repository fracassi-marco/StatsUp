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
                maxSpeedInMetersPerSecond REAL)"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val migrations = mapOf<Int, String>()
        oldVersion.until(newVersion).forEach { db.execSQL(migrations[it]) }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("""DROP TABLE activities""")
        createActivitiesTable(db)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "StatsUp.db"
    }
}