package com.statsup

import android.content.Context
import android.os.AsyncTask
import org.joda.time.format.DateTimeFormat
import java.io.Reader

class CsvWeights(
    private val context: Context,
    private val reader: Reader,
    private val onComplete: () -> Unit
) : AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg ignore: Void): Void? {
        val weights = reader.readLines()
            .map { it.split(";") }
            .filter { it.size > 1 }
            .filter { isValid(it) }
            .map { asWeight(it) }

        WeightRepository.addIfNotExists(context, weights)
        return null
    }

    override fun onPostExecute(result: Void?) {
        onComplete.invoke()
    }

    private fun isValid(record: List<String>): Boolean {
        return try {
            parseDate(record)
            parseWeight(record)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun asWeight(record: List<String>): Weight =
        Weight(parseWeight(record), parseDate(record).millis)

    private fun parseDate(record: List<String>) =
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(record.first())

    private fun parseWeight(record: List<String>) = record.last().toDouble()
}