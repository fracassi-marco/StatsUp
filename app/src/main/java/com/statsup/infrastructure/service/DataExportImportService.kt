package com.statsup.infrastructure.service

import android.content.Context
import android.net.Uri
import com.statsup.domain.ExportData
import com.statsup.domain.StatsUpExportFormat
import com.statsup.domain.repository.SettingRepository
import com.statsup.infrastructure.repository.TrainingDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * Exports the whole local database (trainings, bookmarks, athlete, settings, weight history)
 * to a single compact text file (see [StatsUpExportFormat]), and imports it back.
 *
 * Import is destructive by design: it is meant to restore a device to an exact previous state,
 * not to merge two histories. All local data is wiped and replaced with the content of the
 * imported file, which becomes the sole source of truth.
 */
class DataExportImportService(
    private val context: Context,
    private val database: TrainingDatabase,
    private val settingRepository: SettingRepository
) {

    suspend fun exportData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val trainings = database.trainingRepository.getAllTrainings()
            val bookmarks = database.bookmarkedTrainingRepository.getAllBookmarksList()
            val athlete = database.athleteRepository.load()
            val settings = settingRepository.exportSettings()
            val weightEntries = database.weightRepository.getAllSync()

            val exportData = ExportData(
                trainings = trainings,
                bookmarkedTrainings = bookmarks,
                athlete = athlete,
                settings = settings,
                weightEntries = weightEntries
            )

            val text = StatsUpExportFormat.serialize(exportData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                    writer.write(text)
                }
            } ?: return@withContext Result.failure(Exception("Cannot open output file"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importData(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val text = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).readText()
            } ?: return@withContext Result.failure(Exception("Failed to read file"))

            val exportData = StatsUpExportFormat.parse(text)

            // The imported file becomes the new source of truth: every existing record is
            // discarded first, no merge is attempted with what's already on the device.
            // Trainings are handled by replaceAll(), which performs delete+insert atomically
            // in a single @Transaction: no separate deleteAll() call is needed (and doing so
            // here would only add a non-transactional window where trainings are momentarily
            // absent, without any benefit).
            database.bookmarkedTrainingRepository.deleteAllBookmarks()
            database.athleteRepository.deleteAthlete()
            database.weightRepository.deleteAll()
            settingRepository.clearAllSettings()

            database.trainingRepository.replaceAll(computeCenters(exportData))

            exportData.bookmarkedTrainings.forEach { bookmark ->
                database.bookmarkedTrainingRepository.addBookmark(bookmark.copy(id = 0))
            }

            exportData.athlete?.let { athlete ->
                database.athleteRepository.update(athlete)
            }

            database.weightRepository.insertAll(exportData.weightEntries.map { it.copy(id = 0) })

            settingRepository.importSettings(exportData.settings)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun computeCenters(exportData: ExportData) = exportData.trainings.map { training ->
        if (training.centerLat == null) {
            val center = training.trip?.centerPoint()
            if (center != null) training.copy(centerLat = center.latitude, centerLng = center.longitude)
            else training
        } else training
    }
}
