package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

class EvaluateRemindersUseCaseTest {

    private val useCase = EvaluateRemindersUseCase()

    // Wednesday, well past the midpoint of a 30-day month (day 20/30 -> monthProgress ~66%)
    private val today = LocalDate.of(2026, 4, 20)

    private fun training(id: String, date: LocalDate, distanceKm: Double = 5.0) = Training(
        id = id,
        name = "Training $id",
        distance = distanceKm * 1000.0,
        movingTime = 0,
        elapsedTime = 0,
        totalElevationGain = 0.0,
        sportType = "Run",
        startDate = date.atStartOfDay(ZoneOffset.UTC).toString(),
        maxSpeed = 0.0,
        averageCadence = 0.0,
        averageWatts = 0.0,
        weightedAverageWatts = 0,
        kilojoules = 0.0,
        deviceWatts = false,
        maxHeartrate = 0.0,
        elevHigh = 0.0,
        elevLow = 0.0,
        map = null,
        uploadId = 0L,
        sufferScore = null
    )

    // -------------------------------------------------------------------------
    // Inactivity
    // -------------------------------------------------------------------------

    @Test
    fun `no inactivity event when trained today`() {
        val trainings = listOf(training("1", today))
        val result = useCase(trainings, null, today, 0, 0)
        assertTrue(result.none { it is ReminderEvent.Inactivity })
    }

    @Test
    fun `no inactivity event when history is empty`() {
        val result = useCase(emptyList(), null, today, 0, 0)
        assertTrue(result.none { it is ReminderEvent.Inactivity })
    }

    @Test
    fun `no inactivity event below the default threshold`() {
        val trainings = listOf(training("1", today.minusDays(3)))
        val result = useCase(trainings, null, today, 0, 0)
        assertTrue(result.none { it is ReminderEvent.Inactivity })
    }

    @Test
    fun `inactivity event fires once threshold is reached`() {
        val trainings = listOf(training("1", today.minusDays(4)))
        val result = useCase(trainings, null, today, 0, 0)
        val inactivity = result.filterIsInstance<ReminderEvent.Inactivity>().single()
        assertEquals(4, inactivity.daysSinceLastTraining)
    }

    // -------------------------------------------------------------------------
    // Streak at risk
    // -------------------------------------------------------------------------

    @Test
    fun `no streak-at-risk event when already trained today`() {
        val trainings = listOf(training("1", today), training("2", today.minusDays(1)))
        val result = useCase(trainings, null, today, 0, 0)
        assertTrue(result.none { it is ReminderEvent.StreakAtRisk })
    }

    @Test
    fun `no streak-at-risk event for a single-day streak`() {
        val trainings = listOf(training("1", today.minusDays(1)))
        val result = useCase(trainings, null, today, 0, 0)
        assertTrue(result.none { it is ReminderEvent.StreakAtRisk })
    }

    @Test
    fun `streak-at-risk event fires for an ongoing multi-day streak`() {
        val trainings = listOf(
            training("1", today.minusDays(1)),
            training("2", today.minusDays(2)),
            training("3", today.minusDays(3)),
        )
        val result = useCase(trainings, null, today, 0, 0)
        val risk = result.filterIsInstance<ReminderEvent.StreakAtRisk>().single()
        assertEquals(3, risk.currentStreak)
    }

    // -------------------------------------------------------------------------
    // Distance / training goal at risk
    // -------------------------------------------------------------------------

    @Test
    fun `no goal events before the midpoint of the month`() {
        val early = LocalDate.of(2026, 4, 5) // day 5/30 -> 16% elapsed
        val trainings = listOf(training("1", early, distanceKm = 1.0))
        val result = useCase(trainings, null, early, monthlyDistanceGoalKm = 100, monthlyTrainingGoal = 20)
        assertTrue(result.none { it is ReminderEvent.DistanceGoalAtRisk || it is ReminderEvent.TrainingGoalAtRisk })
    }

    @Test
    fun `distance goal at risk when trailing behind elapsed month progress`() {
        // day 20/30 -> ~66% of month elapsed, only 10% of distance goal done
        val trainings = listOf(training("1", today, distanceKm = 10.0))
        val result = useCase(trainings, null, today, monthlyDistanceGoalKm = 100, monthlyTrainingGoal = 0)
        val risk = result.filterIsInstance<ReminderEvent.DistanceGoalAtRisk>().single()
        assertEquals(10, risk.percentage)
    }

    @Test
    fun `no distance risk event when on pace with the month`() {
        val trainings = listOf(training("1", today, distanceKm = 65.0))
        val result = useCase(trainings, null, today, monthlyDistanceGoalKm = 100, monthlyTrainingGoal = 0)
        assertTrue(result.none { it is ReminderEvent.DistanceGoalAtRisk })
    }

    @Test
    fun `training goal at risk mirrors the distance goal logic`() {
        val trainings = listOf(training("1", today))
        val result = useCase(trainings, null, today, monthlyDistanceGoalKm = 0, monthlyTrainingGoal = 20)
        val risk = result.filterIsInstance<ReminderEvent.TrainingGoalAtRisk>().single()
        assertEquals(5, risk.percentage)
    }

    @Test
    fun `goal near completion between 90 and 99 percent`() {
        val trainings = listOf(training("1", today, distanceKm = 95.0))
        val result = useCase(trainings, null, today, monthlyDistanceGoalKm = 100, monthlyTrainingGoal = 0)
        val near = result.filterIsInstance<ReminderEvent.DistanceGoalNearCompletion>().single()
        assertEquals(95, near.percentage)
    }

    @Test
    fun `no goal event once the goal is fully reached`() {
        val trainings = listOf(training("1", today, distanceKm = 100.0))
        val result = useCase(trainings, null, today, monthlyDistanceGoalKm = 100, monthlyTrainingGoal = 0)
        assertTrue(result.none { it is ReminderEvent.DistanceGoalAtRisk || it is ReminderEvent.DistanceGoalNearCompletion })
    }

    @Test
    fun `goals with a zero target never produce events`() {
        val result = useCase(emptyList(), null, today, monthlyDistanceGoalKm = 0, monthlyTrainingGoal = 0)
        assertTrue(result.none {
            it is ReminderEvent.DistanceGoalAtRisk || it is ReminderEvent.TrainingGoalAtRisk ||
                it is ReminderEvent.DistanceGoalNearCompletion || it is ReminderEvent.TrainingGoalNearCompletion
        })
    }

    // -------------------------------------------------------------------------
    // Weight reminder
    // -------------------------------------------------------------------------

    @Test
    fun `no weight reminder when weight tracking was never used`() {
        val result = useCase(emptyList(), null, today, 0, 0)
        assertTrue(result.none { it is ReminderEvent.WeightReminder })
    }

    @Test
    fun `no weight reminder below the default threshold`() {
        val result = useCase(emptyList(), today.minusDays(6), today, 0, 0)
        assertTrue(result.none { it is ReminderEvent.WeightReminder })
    }

    @Test
    fun `weight reminder fires once threshold is reached`() {
        val result = useCase(emptyList(), today.minusDays(7), today, 0, 0)
        val reminder = result.filterIsInstance<ReminderEvent.WeightReminder>().single()
        assertEquals(7, reminder.daysSinceLastEntry)
    }
}
