package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class SuggestAutoTargetsUseCaseTest {

    private val useCase = SuggestAutoTargetsUseCase()

    // Fixed "now" so tests are deterministic regardless of when they run
    private val now = ZonedDateTime.of(2026, 3, 15, 12, 0, 0, 0, ZoneOffset.UTC)

    // -------------------------------------------------------------------------
    // No historical data → fallback equals current goals → no suggestion
    // -------------------------------------------------------------------------

    @Test
    fun `returns null when no historical data and fallback matches current goals`() {
        // autoDistanceTarget(fallback=100) = 100 (no data), same as currentGoal → null
        val result = useCase(
            trainings = emptyList(),
            currentDistanceGoalKm = 100,
            currentTrainingGoal = 12,
            now = now
        )
        assertNull(result)
    }

    // -------------------------------------------------------------------------
    // Historical data differs from current goals → suggestion returned
    // -------------------------------------------------------------------------

    @Test
    fun `returns suggestion when historical data yields a different distance target`() {
        // Past 2 months: 100km each → median = 100 → autoTarget = (100*1.05).toInt() = 105
        val trainings = listOf(
            run("1", now.minusMonths(1), distanceM = 100_000.0),
            run("2", now.minusMonths(2), distanceM = 100_000.0)
        )
        val result = useCase(
            trainings = trainings,
            currentDistanceGoalKm = 50,   // different from suggested 105
            currentTrainingGoal = 2,       // = suggested (1 run/month * 1.05 = still 1, which equals 2? no...)
            now = now
        )
        assertNotNull(result)
        assertEquals(105, result!!.distanceKm)
    }

    @Test
    fun `suggestion is returned even when only one metric differs`() {
        // 1 run per month in past 2 months → autoTrainingTarget = (1*1.05).toInt() = 1
        // currentTrainingGoal = 1 → same → no training difference
        // 100km/month → autoDistanceTarget = 105, currentDistanceGoal = 50 → differs
        val trainings = listOf(
            run("1", now.minusMonths(1), distanceM = 100_000.0),
            run("2", now.minusMonths(2), distanceM = 100_000.0)
        )
        val result = useCase(
            trainings = trainings,
            currentDistanceGoalKm = 50,
            currentTrainingGoal = 1,
            now = now
        )
        assertNotNull(result)
    }

    @Test
    fun `returns null when suggestion equals current goals for both metrics`() {
        // autoDistanceTarget with 100km history → 105; currentGoal = 105 → same
        // autoTrainingTarget with 1 run/month → 1; currentTrainingGoal = 1 → same
        val trainings = listOf(
            run("1", now.minusMonths(1), distanceM = 100_000.0),
            run("2", now.minusMonths(2), distanceM = 100_000.0)
        )
        val result = useCase(
            trainings = trainings,
            currentDistanceGoalKm = 105,
            currentTrainingGoal = 1,
            now = now
        )
        assertNull(result)
    }

    // -------------------------------------------------------------------------
    // Suggestion content correctness
    // -------------------------------------------------------------------------

    @Test
    fun `suggestion distanceKm is median of past months plus 5 percent`() {
        // 2 past months: 100km and 200km → median = 150 → (150*1.05).toInt() = 157
        val trainings = listOf(
            run("1", now.minusMonths(1), distanceM = 100_000.0),
            run("2", now.minusMonths(2), distanceM = 200_000.0)
        )
        val result = useCase(
            trainings = trainings,
            currentDistanceGoalKm = 0,
            currentTrainingGoal = 0,
            now = now
        )
        assertEquals(157, result!!.distanceKm)
    }

    @Test
    fun `suggestion trainingCount is median of sessions per month plus 5 percent`() {
        // 4 sessions one month, 4 sessions another → median = 4 → (4*1.05).toInt() = 4
        val trainings = buildList {
            repeat(4) { i -> add(run("1" + i.toString(), now.minusMonths(1).withDayOfMonth(i + 1))) }
            repeat(4) { i -> add(run("10" + i.toString(), now.minusMonths(2).withDayOfMonth(i + 1))) }
        }
        val result = useCase(
            trainings = trainings,
            currentDistanceGoalKm = 0,
            currentTrainingGoal = 0,
            now = now
        )
        assertEquals(4, result!!.trainingCount)
    }

    @Test
    fun `current month trainings are excluded from the suggestion calculation`() {
        // Huge distance this month should not skew the suggestion
        val currentMonthRun = run("99", now, distanceM = 9_999_999.0)
        val pastRun = run("1", now.minusMonths(1), distanceM = 100_000.0)
        val result = useCase(
            trainings = listOf(currentMonthRun, pastRun),
            currentDistanceGoalKm = 0,
            currentTrainingGoal = 0,
            now = now
        )
        // Should be based on 100km only → (100*1.05).toInt() = 105
        assertEquals(105, result!!.distanceKm)
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private fun run(id: String, date: ZonedDateTime, distanceM: Double = 10_000.0) = Training(
        id = id,
        name = "Run $id",
        distance = distanceM,
        movingTime = 3600,
        elapsedTime = 3600,
        totalElevationGain = 0.0,
        sportType = "Run",
        startDate = date.toString(),
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
}
