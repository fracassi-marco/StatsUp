package com.statsup

import android.content.Context
import android.os.AsyncTask
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import org.joda.time.format.DateTimeFormat
import java.io.Reader

class CsvWeights(private val context: Context, private val reader: Reader, private val onComplete: () -> Unit) :  AsyncTask<Void, Void, Void>() {

    override fun doInBackground(vararg ignore: Void): Void? {
        val weights : List<Weight> = CSVReaderBuilder(reader)
                .withCSVParser(CSVParserBuilder().withSeparator(';').build())
                .build()
                .readAll()
                .filter { it.size > 1 }
                .filter { isValid(it) }
                .map {
                    asWeight(it)
                }
        WeightRepository.addIfNotExists(context, weights)
        return null
    }

    override fun onPostExecute(result: Void?) {
        onComplete.invoke()
    }

    private fun isValid(record: Array<String>): Boolean {
        return try {
            parseDate(record)
            parseWeight(record)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun asWeight(record: Array<String>): Weight = Weight(parseWeight(record), parseDate(record).millis)

    private fun parseDate(record: Array<String>) = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(record[0])

    private fun parseWeight(record: Array<String>) = record[1].toDouble()
}