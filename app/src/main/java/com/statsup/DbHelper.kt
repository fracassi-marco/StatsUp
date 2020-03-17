package com.statsup

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