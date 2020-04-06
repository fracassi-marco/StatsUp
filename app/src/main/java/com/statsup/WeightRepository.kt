package com.statsup

import android.content.ContentValues
import android.content.Context
import org.joda.time.DateTime

object WeightRepository {
    private var weights: List<Weight> = emptyList()

    fun clean(context: Context) {
        DbHelper(context).writableDatabase.use { it.delete("weights", null, null) }
        weights = emptyList()
    }

    fun load(context: Context) {
        val result = mutableListOf<Weight>()
        DbHelper(context).readableDatabase.use {
            it.query("weights", null, null, null, null, null, "dateInMillis DESC").use { cursor ->
                while (cursor.moveToNext()) {
                    result.add(
                        Weight(
                            cursor.getDouble(cursor.getColumnIndexOrThrow("kilograms")),
                            cursor.getLong(cursor.getColumnIndexOrThrow("dateInMillis"))
                        )
                    )
                }
            }
        }

        weights = result
    }

    fun addIfNotExists(context: Context, newWeights: List<Weight>) {
        val toAdd = newWeights.minus(weights)
        if (toAdd.isNotEmpty()) {
            saveAll(context, toAdd)
            weights = weights.plus(toAdd).sortedByDescending { it.dateInMillis }
        }
    }

    fun delete(context: Context, weight: Weight) {
        DbHelper(context).writableDatabase.use {
            it.delete("weights", "dateInMillis = ?", arrayOf(weight.dateInMillis.toString()))
        }
        weights = weights.minus(weight).sortedByDescending { it.dateInMillis }
    }

    private fun saveAll(context: Context, toAdd: List<Weight>) {
        toAdd.forEach {
            val values = ContentValues().apply {
                put("kilograms", it.kilograms)
                put("dateInMillis", it.dateInMillis)
            }

            DbHelper(context).writableDatabase.use { it.insert("weights", null, values) }
        }
    }

    fun any() = weights.isNotEmpty()

    fun all(): List<Weight> = weights

    fun latest(): Weight {
        if (weights.isEmpty())
            return Weight(50.0, DateTime().millis)
        return all().first()
    }
}
