package com.statsup

import android.content.ContentValues
import android.content.Context
import java.util.*

object WeightRepository {
    private val listeners: MutableList<Listener<List<Weight>>> = ArrayList()
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

    fun listen(vararg listeners: Listener<List<Weight>>) {
        listeners.forEach {
            this.listeners.add(it)
            it.update(weights)
        }
    }

    private fun update() {
        listeners.forEach { it.update(weights) }
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

    fun removeListener(vararg listeners: Listener<List<Weight>>) {
        listeners.forEach { this.listeners.remove(it) }
    }
}
