package com.statsup.infrastructure.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.statsup.MainActivity
import com.statsup.R
import com.statsup.domain.EvaluateRemindersUseCase
import com.statsup.domain.Provider
import com.statsup.domain.ReminderEvent
import com.statsup.domain.Trainings
import com.statsup.domain.repository.SettingRepository
import com.statsup.domain.repository.TrainingRepository
import com.statsup.domain.repository.WeightRepository
import com.statsup.infrastructure.repository.DbWeightRepository
import com.statsup.infrastructure.repository.SharedPreferencesSettingRepository
import com.statsup.infrastructure.repository.TrainingDatabase
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Runs once a day in the background (works even if the app hasn't been opened, as long as
 * Android hasn't restricted the app's battery usage) to check for situations worth nudging the
 * user about: inactivity, a streak about to break, a monthly goal falling behind schedule or
 * close to completion, or a stale weight log. See [EvaluateRemindersUseCase] for the pure logic.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val trainingRepository: TrainingRepository? = null,
    private val weightRepository: WeightRepository? = null,
    private val settingRepository: SettingRepository? = null,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = settingRepository ?: SharedPreferencesSettingRepository(applicationContext)
        if (!settings.loadRemindersEnabled()) return Result.success()

        val db = TrainingDatabase.getInstance(applicationContext)
        val trainings = (trainingRepository ?: db.trainingRepository).getAllTrainings()
        val weightEntries = (weightRepository ?: DbWeightRepository(db.weightRepository)).getAllSync()

        val today = LocalDate.now()
        val autoTargets = settings.loadAutoTargets()
        val effectiveDistanceGoal = if (autoTargets) {
            Trainings(trainings, provider = Provider.Distance).autoDistanceTarget(fallbackKm = settings.loadMonthlyGoal())
        } else {
            settings.loadMonthlyGoal()
        }
        val effectiveTrainingGoal = if (autoTargets) {
            Trainings(trainings, provider = Provider.Frequency).autoTrainingTarget(fallbackCount = settings.loadMonthlyTrainingGoal())
        } else {
            settings.loadMonthlyTrainingGoal()
        }
        val lastWeightDate = weightEntries.maxByOrNull { it.date }
            ?.let { Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate() }

        val events = EvaluateRemindersUseCase()(
            trainings = trainings,
            lastWeightEntryDate = lastWeightDate,
            today = today,
            monthlyDistanceGoalKm = effectiveDistanceGoal,
            monthlyTrainingGoal = effectiveTrainingGoal,
        )

        events.forEach { event -> notifyIfNotAlreadyFired(settings, event, today) }

        return Result.success()
    }

    private fun notifyIfNotAlreadyFired(settings: SettingRepository, event: ReminderEvent, today: LocalDate) {
        val isMonthly = event is ReminderEvent.DistanceGoalAtRisk || event is ReminderEvent.TrainingGoalAtRisk ||
            event is ReminderEvent.DistanceGoalNearCompletion || event is ReminderEvent.TrainingGoalNearCompletion
        val periodMarker = if (isMonthly) YearMonth.from(today).toString() else today.toString()

        if (settings.loadReminderLastFired(event.dedupeKey) == periodMarker) return

        val (notificationId, title, text) = contentFor(event)
        notify(notificationId, title, text)
        settings.saveReminderLastFired(event.dedupeKey, periodMarker)
    }

    private fun contentFor(event: ReminderEvent): Triple<Int, String, String> {
        val context = applicationContext
        return when (event) {
            is ReminderEvent.Inactivity -> Triple(
                NOTIFICATION_ID_INACTIVITY,
                context.getString(R.string.reminder_inactivity_title),
                context.getString(R.string.reminder_inactivity_text, event.daysSinceLastTraining)
            )
            is ReminderEvent.StreakAtRisk -> Triple(
                NOTIFICATION_ID_STREAK_RISK,
                context.getString(R.string.reminder_streak_risk_title),
                context.getString(R.string.reminder_streak_risk_text, event.currentStreak)
            )
            is ReminderEvent.DistanceGoalAtRisk -> Triple(
                NOTIFICATION_ID_DISTANCE_GOAL_RISK,
                context.getString(R.string.reminder_distance_goal_risk_title),
                context.getString(R.string.reminder_distance_goal_risk_text, event.percentage)
            )
            is ReminderEvent.TrainingGoalAtRisk -> Triple(
                NOTIFICATION_ID_TRAINING_GOAL_RISK,
                context.getString(R.string.reminder_training_goal_risk_title),
                context.getString(R.string.reminder_training_goal_risk_text, event.percentage)
            )
            is ReminderEvent.DistanceGoalNearCompletion -> Triple(
                NOTIFICATION_ID_DISTANCE_GOAL_NEAR,
                context.getString(R.string.reminder_goal_near_title),
                context.getString(R.string.reminder_distance_goal_near_text, event.percentage)
            )
            is ReminderEvent.TrainingGoalNearCompletion -> Triple(
                NOTIFICATION_ID_TRAINING_GOAL_NEAR,
                context.getString(R.string.reminder_goal_near_title),
                context.getString(R.string.reminder_training_goal_near_text, event.percentage)
            )
            is ReminderEvent.WeightReminder -> Triple(
                NOTIFICATION_ID_WEIGHT,
                context.getString(R.string.reminder_weight_title),
                context.getString(R.string.reminder_weight_text, event.daysSinceLastEntry)
            )
        }
    }

    private fun notify(notificationId: Int, title: String, text: String) {
        val context = applicationContext
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "reminders_channel"
        private const val UNIQUE_WORK_NAME = "reminder_worker"
        private const val NOTIFICATION_ID_INACTIVITY = 2001
        private const val NOTIFICATION_ID_STREAK_RISK = 2002
        private const val NOTIFICATION_ID_DISTANCE_GOAL_RISK = 2003
        private const val NOTIFICATION_ID_TRAINING_GOAL_RISK = 2004
        private const val NOTIFICATION_ID_DISTANCE_GOAL_NEAR = 2005
        private const val NOTIFICATION_ID_TRAINING_GOAL_NEAR = 2006
        private const val NOTIFICATION_ID_WEIGHT = 2007

        fun createChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.reminder_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        /** Schedules the daily check, run once around 19:00 local time so a same-day training can still save a streak. */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(minutesUntilNextCheck(), TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }

        private fun minutesUntilNextCheck(checkHour: Int = 19): Long {
            val now = ZonedDateTime.now()
            var next = now.withHour(checkHour).withMinute(0).withSecond(0).withNano(0)
            if (!next.isAfter(now)) next = next.plusDays(1)
            return ChronoUnit.MINUTES.between(now, next).coerceAtLeast(1)
        }
    }
}
