package com.statsup

import android.app.Application
import android.content.Intent
import com.statsup.infrastructure.service.ImportForegroundService
import com.statsup.ui.components.MapSnapshotCache
import kotlin.system.exitProcess
import androidx.core.content.edit

class StatsUpApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ImportForegroundService.createChannel(this)
        clearMapSnapshotCacheIfNeeded()
        installCrashHandler()
    }

    private fun clearMapSnapshotCacheIfNeeded() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val clearedVersion = prefs.getInt("map_cache_cleared_for_db_version", 0)
        if (clearedVersion < 16) {
            MapSnapshotCache.clearAll(this)
            prefs.edit { putInt("map_cache_cleared_for_db_version", 16) }
        }
    }

    private fun installCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val intent = Intent(this, CrashActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(CrashActivity.EXTRA_CRASH_MESSAGE, throwable.toString())
                    putExtra(CrashActivity.EXTRA_STACK_TRACE, throwable.stackTraceToString())
                }
                startActivity(intent)
            } catch (_: Exception) {
                // If we fail to show the crash screen, fall back to default handler
                defaultHandler?.uncaughtException(thread, throwable)
            }
            exitProcess(1)
        }
    }
}

