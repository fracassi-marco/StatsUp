package com.statsup.infrastructure.service

import android.content.Context
import android.net.Uri
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.statsup.domain.ExportData
import com.statsup.domain.Route
import com.statsup.domain.repository.SettingRepository
import com.statsup.infrastructure.repository.TrainingDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.reflect.Type

class DataExportImportService(
    private val context: Context,
    private val database: TrainingDatabase,
    private val settingRepository: SettingRepository
) {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Route::class.java, object : JsonSerializer<Route>, JsonDeserializer<Route> {
            override fun serialize(src: Route?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                val obj = JsonObject()
                src?.id?.let { obj.addProperty("id", it) }
                src?.summaryPolyline?.let { obj.addProperty("summaryPolyline", it) }
                src?.resourceState?.let { obj.addProperty("resourceState", it) }
                return obj
            }

            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Route {
                val obj = json.asJsonObject
                return Route(
                    id = if (obj.has("id")) obj.get("id").asString else null,
                    summaryPolyline = if (obj.has("summaryPolyline")) obj.get("summaryPolyline").asString else null,
                    resourceState = if (obj.has("resourceState")) obj.get("resourceState").asInt else null
                )
            }
        })
        .setExclusionStrategies(object : ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes): Boolean {
                // Skip lazy properties and Kotlin synthetic fields
                val fieldName = f.name

                // Check if it's a lazy property by checking if the type is Lazy
                if (f.declaredType.toString().contains("Lazy")) {
                    return true
                }

                // Skip known lazy properties
                if (fieldName == "date" || fieldName == "trip") {
                    return true
                }

                // Skip Kotlin synthetic fields
                if (fieldName.startsWith("$") || fieldName.contains("$")) {
                    return true
                }

                return false
            }

            override fun shouldSkipClass(clazz: Class<*>): Boolean {
                return false
            }
        })
        .create()

    suspend fun exportData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val trainings = database.trainingRepository.getAllTrainings()
            val bookmarks = database.bookmarkedTrainingRepository.getAllBookmarksList()
            val athlete = database.athleteRepository.load()
            val settings = settingRepository.exportSettings()

            val exportData = ExportData(
                trainings = trainings,
                bookmarkedTrainings = bookmarks,
                athlete = athlete,
                settings = settings
            )

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    gson.toJson(exportData, writer)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val exportData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    gson.fromJson(reader, ExportData::class.java)
                }
            }

            if (exportData == null) {
                return@withContext Result.failure(Exception("Failed to read file"))
            }

            // Clear all existing data
            database.trainingRepository.deleteAll()
            database.bookmarkedTrainingRepository.deleteAllBookmarks()
            database.athleteRepository.deleteAthlete()
            settingRepository.clearAllSettings()

            // Import new data
            exportData.trainings.forEach { training ->
                val withCenter = if (training.centerLat == null) {
                    val center = training.trip?.centerPoint()
                    if (center != null) training.copy(centerLat = center.latitude, centerLng = center.longitude)
                    else training
                } else training
                database.trainingRepository.add(withCenter)
            }

            exportData.bookmarkedTrainings.forEach { bookmark ->
                database.bookmarkedTrainingRepository.addBookmark(bookmark)
            }

            exportData.athlete?.let { athlete ->
                database.athleteRepository.update(athlete)
            }

            settingRepository.importSettings(exportData.settings)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

