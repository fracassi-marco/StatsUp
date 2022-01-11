package com.statsup

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.joda.time.format.DateTimeFormat
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class CsvWeights(private val inputStream: InputStream) {
    fun read(scope: CoroutineScope, context: Context, callback: () -> Unit) {
        scope.launch {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val weights = reader.readLines()
                .map { it.split(";") }
                .filter { it.size > 1 }
                .filter { isValid(it) }
                .map { asWeight(it) }

            WeightRepository.addIfNotExists(context, weights)

            callback()
        }
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
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(record[0])

    private fun parseWeight(record: List<String>) = record[1].toDouble()
}