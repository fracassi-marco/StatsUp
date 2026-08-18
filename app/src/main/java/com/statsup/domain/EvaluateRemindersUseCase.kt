package com.statsup.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Detects situations worth nudging the user about proactively (inactivity, a streak about to
 * break, a monthly goal falling behind or about to be hit, a stale weight log) — evaluated once a
 * day by ReminderWorker. Pure function, no Android dependency, so it's fully unit-testable.
 */
class EvaluateRemindersUseCase {

    operator fun invoke(
        trainings: List<Training>,
        lastWeightEntryDate: LocalDate?,
        today: LocalDate,
        monthlyDistanceGoalKm: Int,
        monthlyTrainingGoal: Int,
        inactivityThresholdDays: Int = 4,
        weightReminderThresholdDays: Int = 7,
        goalRiskMarginPercent: Int = 15
    ): List<ReminderEvent> {
        val trainingDates = trainings.map { it.date.toLocalDate() }.toSet()

        return listOfNotNull(
            inactivityEvent(trainingDates, today, inactivityThresholdDays),
            streakAtRiskEvent(trainingDates, today),
        ) + goalEvents(trainings, today, monthlyDistanceGoalKm, monthlyTrainingGoal, goalRiskMarginPercent) +
            listOfNotNull(weightReminderEvent(lastWeightEntryDate, today, weightReminderThresholdDays))
    }

    private fun inactivityEvent(trainingDates: Set<LocalDate>, today: LocalDate, thresholdDays: Int): ReminderEvent? {
        if (trainingDates.isEmpty()) return null
        val lastDate = trainingDates.max()
        val daysSince = ChronoUnit.DAYS.between(lastDate, today).toInt()
        return if (daysSince >= thresholdDays) ReminderEvent.Inactivity(daysSince) else null
    }

    /**
     * Fires when there's an ongoing streak (ending yesterday) and today has no activity logged
     * yet — still salvageable if the user trains today.
     */
    private fun streakAtRiskEvent(trainingDates: Set<LocalDate>, today: LocalDate): ReminderEvent? {
        if (today in trainingDates) return null

        var streak = 0
        var day = today.minusDays(1)
        while (day in trainingDates) {
            streak++
            day = day.minusDays(1)
        }
        return if (streak >= 2) ReminderEvent.StreakAtRisk(streak) else null
    }

    private fun goalEvents(
        trainings: List<Training>,
        today: LocalDate,
        monthlyDistanceGoalKm: Int,
        monthlyTrainingGoal: Int,
        riskMarginPercent: Int
    ): List<ReminderEvent> {
        val monthTrainings = trainings.filter {
            val d = it.date.toLocalDate()
            d.month == today.month && d.year == today.year
        }
        val monthProgressPercent = today.dayOfMonth * 100 / today.lengthOfMonth()

        val distanceEvent = if (monthlyDistanceGoalKm > 0) {
            val distanceKm = monthTrainings.sumOf { it.distanceInKilometers() }
            val pct = (distanceKm / monthlyDistanceGoalKm * 100).toInt()
            goalEvent(pct, monthProgressPercent, riskMarginPercent,
                onRisk = { ReminderEvent.DistanceGoalAtRisk(pct) },
                onNear = { ReminderEvent.DistanceGoalNearCompletion(pct) })
        } else null

        val trainingEvent = if (monthlyTrainingGoal > 0) {
            val pct = (monthTrainings.size * 100 / monthlyTrainingGoal)
            goalEvent(pct, monthProgressPercent, riskMarginPercent,
                onRisk = { ReminderEvent.TrainingGoalAtRisk(pct) },
                onNear = { ReminderEvent.TrainingGoalNearCompletion(pct) })
        } else null

        return listOfNotNull(distanceEvent, trainingEvent)
    }

    /**
     * A goal is "at risk" once we're past the midpoint of the month and progress is trailing the
     * elapsed-time percentage by more than [riskMarginPercent] points. It's "near completion" once
     * it crosses 90% but hasn't been fully reached yet (crossing 100% is handled separately by the
     * existing celebration/achievement flow).
     */
    private fun goalEvent(
        pct: Int,
        monthProgressPercent: Int,
        riskMarginPercent: Int,
        onRisk: () -> ReminderEvent,
        onNear: () -> ReminderEvent
    ): ReminderEvent? = when {
        pct >= 100 -> null
        monthProgressPercent > 50 && pct < monthProgressPercent - riskMarginPercent -> onRisk()
        pct in 90..99 -> onNear()
        else -> null
    }

    private fun weightReminderEvent(lastEntryDate: LocalDate?, today: LocalDate, thresholdDays: Int): ReminderEvent? {
        if (lastEntryDate == null) return null
        val daysSince = ChronoUnit.DAYS.between(lastEntryDate, today).toInt()
        return if (daysSince >= thresholdDays) ReminderEvent.WeightReminder(daysSince) else null
    }
}
