package com.statsup.infrastructure.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.statsup.R
import com.statsup.domain.ApiException
import com.statsup.domain.FullImportUseCase
import com.statsup.domain.UpdateTrainingsUseCase
import com.statsup.infrastructure.IntervalsIcuTrainingApi
import com.statsup.infrastructure.repository.SharedPreferencesSettingRepository
import com.statsup.infrastructure.repository.TrainingDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ImportForegroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val token = intent?.getStringExtra(EXTRA_TOKEN) ?: run { stopSelf(); return START_NOT_STICKY }
        val fullImport = intent.getBooleanExtra(EXTRA_FULL_IMPORT, false)

        startForeground(NOTIFICATION_ID, buildNotification(this))

        scope.launch {
            try {
                val db = TrainingDatabase.getInstance(applicationContext)
                val settingRepository = SharedPreferencesSettingRepository(applicationContext)
                val api = IntervalsIcuTrainingApi(settingRepository)
                val activeToken = resolveToken(token, api, settingRepository)

                val count = if (fullImport) {
                    FullImportUseCase(
                        db.trainingRepository,
                        db.athleteRepository,
                        db.bookmarkedTrainingRepository,
                        api
                    )(activeToken).count()
                } else {
                    UpdateTrainingsUseCase(db.trainingRepository, db.athleteRepository, api)(activeToken).count()
                }
                ImportEventBus.emitSuccess(count)
            } catch (e: ApiException) {
                Log.e("StatsUp", "API error during import", e)
                if (e.isAuthError) clearStoredCredentials()
                ImportEventBus.emitError(e.message ?: "Import failed. Try again.")
            } catch (e: Exception) {
                Log.e("StatsUp", "Error during import", e)
                ImportEventBus.emitError(e.message ?: "Import failed. Try again.")
            } finally {
                stopSelf()
            }
        }

        return START_NOT_STICKY
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
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "import_channel"
        private const val EXTRA_TOKEN = "token"
        private const val EXTRA_FULL_IMPORT = "full_import"

        fun intent(context: Context, token: String, fullImport: Boolean): Intent =
            Intent(context, ImportForegroundService::class.java).apply {
                putExtra(EXTRA_TOKEN, token)
                putExtra(EXTRA_FULL_IMPORT, fullImport)
            }

        fun buildNotification(context: Context) =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.import_notification_title))
                .setContentText(context.getString(R.string.import_notification_text))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
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
