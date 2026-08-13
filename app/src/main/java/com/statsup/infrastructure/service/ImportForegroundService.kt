package com.statsup.infrastructure.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.statsup.R
import com.statsup.domain.ApiException
import com.statsup.domain.FullImportUseCase
import com.statsup.domain.ReimportTrainingUseCase
import com.statsup.domain.RetryUnresolvedPeaksUseCase
import com.statsup.domain.UpdateTrainingsUseCase
import com.statsup.infrastructure.IntervalsIcuTrainingApi
import com.statsup.infrastructure.repository.AndroidGeocodingRepository
import com.statsup.infrastructure.repository.FallbackPeakLookupRepository
import com.statsup.infrastructure.repository.GeoNamesPeakRepository
import com.statsup.infrastructure.repository.OverpassPeakRepository
import com.statsup.infrastructure.repository.SharedPreferencesSettingRepository
import com.statsup.infrastructure.repository.TrainingDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ImportForegroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val token = intent?.getStringExtra(EXTRA_TOKEN) ?: run { stopSelf(); return START_NOT_STICKY }
        val fullImport = intent.getBooleanExtra(EXTRA_FULL_IMPORT, false)
        val reimportTrainingId = intent.getStringExtra(EXTRA_REIMPORT_TRAINING_ID)

        startForeground(NOTIFICATION_ID, buildNotification(this))

        ImportEventBus.resetProgress()

        // A full history import can take several minutes of network calls; without a wake lock
        // some OEMs' battery managers suspend the app's CPU access once the screen turns off,
        // even inside a foreground service, silently stalling or killing the import.
        val powerManager = getSystemService(PowerManager::class.java)
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StatsUp:import")
            .apply { acquire(WAKE_LOCK_TIMEOUT_MILLIS) }

        scope.launch {
            try {
                val db = TrainingDatabase.getInstance(applicationContext)
                val settingRepository = SharedPreferencesSettingRepository(applicationContext)
                val api = IntervalsIcuTrainingApi(settingRepository)
                val activeToken = resolveToken(token, api, settingRepository)

                val notificationManager = getSystemService(NotificationManager::class.java)
                val onProgress: suspend (Int, Int) -> Unit = { current, total ->
                    notificationManager.notify(NOTIFICATION_ID, buildProgressNotification(applicationContext, current, total))
                    ImportEventBus.emitProgress(current, total)
                }

                val geocoding = AndroidGeocodingRepository(applicationContext)
                val peakLookup = FallbackPeakLookupRepository(OverpassPeakRepository(), GeoNamesPeakRepository())
                if (reimportTrainingId != null) {
                    val reimported = ReimportTrainingUseCase(db.trainingRepository, api, geocoding, peakLookup)(activeToken, reimportTrainingId)
                    ImportEventBus.emitReimportSuccess(reimportTrainingId, reimported.peakName?.takeIf { it.isNotBlank() })
                } else {
                    val count = if (fullImport) {
                        FullImportUseCase(
                            db.trainingRepository,
                            db.athleteRepository,
                            db.bookmarkedTrainingRepository,
                            api,
                            geocoding,
                            peakLookup
                        )(activeToken, onProgress)
                    } else {
                        UpdateTrainingsUseCase(db.trainingRepository, db.athleteRepository, api, geocoding, peakLookup)(activeToken, onProgress)
                    }
                    // Trainings a previous run left with peakName == null (both Overpass and the
                    // GeoNames fallback failed) are never revisited by UpdateTrainingsUseCase, since it only looks at
                    // trainings newer than the latest stored one — sweep them here instead.
                    try {
                        val retried = RetryUnresolvedPeaksUseCase(db.trainingRepository, peakLookup)()
                        if (retried > 0) Log.i("StatsUp", "Retried $retried previously unresolved peak(s)")
                    } catch (e: Exception) {
                        Log.w("StatsUp", "Retrying unresolved peaks failed, will try again next sync", e)
                    }
                    ImportEventBus.emitSuccess(count)
                }
            } catch (e: ApiException) {
                Log.e("StatsUp", "API error during import", e)
                if (e.isAuthError) clearStoredCredentials()
                ImportEventBus.emitError(e.message ?: "Import failed. Try again.")
            } catch (e: Exception) {
                Log.e("StatsUp", "Error during import", e)
                ImportEventBus.emitError(e.message ?: "Import failed. Try again.")
            } finally {
                ImportEventBus.resetProgress()
                releaseWakeLock()
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private suspend fun resolveToken(
        initialToken: String,
        api: IntervalsIcuTrainingApi,
        settingRepository: SharedPreferencesSettingRepository
    ): String {
        val stored = settingRepository.loadApiToken()?.takeIf { it.isNotBlank() } ?: return initialToken
        val expiry = settingRepository.loadApiTokenExpiry()
        val nowSecs = System.currentTimeMillis() / 1000
        if (expiry == 0L || nowSecs < expiry - 60) return stored

        val refreshToken = settingRepository.loadApiRefreshToken() ?: return stored
        return try {
            val newToken = api.refreshToken(refreshToken)
            settingRepository.saveApiToken(newToken.accessToken)
            settingRepository.saveApiRefreshToken(newToken.refreshToken)
            settingRepository.saveApiTokenExpiry(newToken.expiresAt)
            newToken.accessToken
        } catch (e: Exception) {
            Log.w("StatsUp", "Token refresh failed in service, using stored token", e)
            stored
        }
    }

    private fun clearStoredCredentials() {
        SharedPreferencesSettingRepository(applicationContext).apply {
            saveApiToken("")
            saveApiRefreshToken("")
            saveApiTokenExpiry(0L)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        releaseWakeLock()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val WAKE_LOCK_TIMEOUT_MILLIS = 30 * 60 * 1000L
        const val CHANNEL_ID = "import_channel"
        private const val EXTRA_TOKEN = "token"
        private const val EXTRA_FULL_IMPORT = "full_import"
        private const val EXTRA_REIMPORT_TRAINING_ID = "reimport_training_id"

        fun intent(context: Context, token: String, fullImport: Boolean): Intent =
            Intent(context, ImportForegroundService::class.java).apply {
                putExtra(EXTRA_TOKEN, token)
                putExtra(EXTRA_FULL_IMPORT, fullImport)
            }

        fun reimportIntent(context: Context, token: String, trainingId: String): Intent =
            Intent(context, ImportForegroundService::class.java).apply {
                putExtra(EXTRA_TOKEN, token)
                putExtra(EXTRA_REIMPORT_TRAINING_ID, trainingId)
            }

        fun buildNotification(context: Context) =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.import_notification_title))
                .setContentText(context.getString(R.string.import_notification_text))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

        fun buildProgressNotification(context: Context, current: Int, total: Int) =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.import_notification_title))
                .setContentText(context.getString(R.string.import_notification_progress, current, total))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(total, current, false)
                .build()

        fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.import_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}
