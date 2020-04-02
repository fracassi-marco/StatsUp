package com.statsup

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE

object UserRepository {

    var user = User()
    private val listeners: MutableMap<String, Listener<User>> = mutableMapOf()

    private fun update() {
        listeners.values.forEach { it.update(user) }
    }

    fun load(context: Context) {
        DbHelper(context).readableDatabase.use {
            it.query("users", null, null, null, null, null, null, "1").use { cursor ->
                if (cursor.moveToNext()) {
                    user = User(cursor.getInt(cursor.getColumnIndexOrThrow("height")))
                }
            }
        }
    }

    fun listen(key: String, listener: Listener<User>) {
        listeners[key] = listener
        listener.update(user)
    }

    fun update(context: Context, user: User) {
        val values = ContentValues().apply {
            put("height", user.height)
        }

        DbHelper(context).writableDatabase.use {
            it.insertWithOnConflict("users", null, values, CONFLICT_REPLACE)
        }

        this.user = user
        update()
    }

    fun removeListener(key: String) {
        if(listeners.containsKey(key)){
            listeners.remove(key)
        }
    }
}

