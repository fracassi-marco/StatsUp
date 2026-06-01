package com.statsup.infrastructure.service

import android.content.Context
import android.net.Uri
import com.statsup.domain.WeightEntry
import java.time.Instant

class WeightImportService(private val context: Context) {

    fun parseLibraCsv(uri: Uri): List<WeightEntry> {
        val entries = mutableListOf<WeightEntry>()
        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
            for (line in reader.lineSequence()) {
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
                val parts = trimmed.split(";")
                if (parts.size < 2) continue
                val dateStr = parts[0].trim()
                val weightStr = parts[1].trim()
                if (weightStr.isEmpty()) continue
                runCatching {
                    val epochMillis = Instant.parse(dateStr).toEpochMilli()
                    val weightKg = weightStr.toDouble()
                    entries.add(WeightEntry(date = epochMillis, weightKg = weightKg))
                }
            }
        }
        return entries
    }
}
