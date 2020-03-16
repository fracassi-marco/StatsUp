package com.statsup

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE

object UserRepository {

    private var user = User()
    private val listeners: MutableList<Listener<User>> = mutableListOf()

    private fun update() {
        listeners.forEach { it.update(user) }
    }

    fun load(context: Context) {
        DbHelper(context).readableDatabase.query(
            "users",
            null,
            null,
            null,
            null,
            null,
            null,
            "1"
        ).use { cursor ->
            if (cursor.moveToNext()) {
                user = User(cursor.getInt(cursor.getColumnIndexOrThrow("height")))
            }
        }
    }

    fun listen(listener: Listener<User>) {
        listeners.add(listener)
        listener.update(user)
    }

    fun update(context: Context, user: User) {
        val values = ContentValues().apply {
            put("height", user.height)
        }

        DbHelper(context).writableDatabase.insertWithOnConflict(
            "users",
            null,
            values,
            CONFLICT_REPLACE
        )

        this.user = user
        update()
    }

    fun removeListener(vararg listeners: Listener<User>) {
        listeners.forEach { this.listeners.remove(it) }
    }
}

