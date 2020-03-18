package com.statsup

import android.content.ContentValues
import android.content.Context

object WeightRepository {
    private val listeners: MutableMap<String, Listener<List<Weight>>> = mutableMapOf()
    private var weights: MutableList<Weight> = mutableListOf()

    fun load(context: Context) {
        val result = mutableListOf<Weight>()
        DbHelper(context).readableDatabase.query(
            "weights",
            null,
            null,
            null,
            null,
            null,
            "dateInMillis DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(
                    Weight(
                        cursor.getDouble(cursor.getColumnIndexOrThrow("kilograms")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("dateInMillis"))
                    )
                )
            }
        }

        weights = result
    }

    fun listen(key: String, listener: Listener<List<Weight>>) {
        listeners[key] = listener
        listener.update(weights)
    }

    private fun update() {
        listeners.values.forEach { it.update(weights) }
    }

    fun addIfNotExists(context: Context, newActivities: List<Weight>) {
        val toAdd = newActivities.minus(weights)
        if(toAdd.isNotEmpty()) {
            saveAll(context, toAdd)
            weights = weights.plus(toAdd).toMutableList()
        }

        update()
    }

    fun delete(context: Context, weight: Weight) {
        weights.remove(weight)

        DbHelper(context).writableDatabase.delete("weights", "dateInMillis = ?", arrayOf(weight.dateInMillis.toString()))

        update()
    }

    private fun saveAll(context: Context, toAdd: List<Weight>) {
        toAdd.forEach {
            val values = ContentValues().apply {
                put("kilograms", it.kilograms)
                put("dateInMillis", it.dateInMillis)
            }

            DbHelper(context).writableDatabase.insert("weights", null, values)
        }
    }

    fun removeListener(key: String) {
        if(listeners.containsKey(key)){
            listeners.remove(key)
        }
    }
}
