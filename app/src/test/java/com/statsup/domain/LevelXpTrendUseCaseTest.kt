package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for [LevelXpTrendUseCase].
 * No mocking needed — it's a pure computation class, like [FitnessScoreTrendUseCase].
 * Fixed `now = 2026-03-15` is used throughout.
 */
class LevelXpTrendUseCaseTest {

    private val now = LocalDate.of(2026, 3, 15)
    private val useCase = LevelXpTrendUseCase()

    @Test
    fun `returns one point per day covering the last 30 days, oldest first`() {
        val result = useCase(emptyList(), now = now)

        assertEquals(30, result.size)
        assertEquals(now.minusDays(29), result.first().date)
        assertEquals(now, result.last().date)
    }

    @Test
    fun `each point only sees trainings up to that day`() {
        val futureTraining = listOf(training("1", now.atStartOfDay(java.time.ZoneOffset.UTC)))

        val resultBeforeTraining = useCase(futureTraining, now = now.minusDays(1))
        val resultOnTrainingDay = useCase(futureTraining, now = now)

        assertEquals(0, resultBeforeTraining.last().xp)
        assertTrue(resultOnTrainingDay.last().xp > 0)
    }

    @Test
    fun `matches a direct EvaluateLevelUseCase call for the most recent day`() {
        val trainings = listOf(
            training("1", now.minusDays(2).atStartOfDay(java.time.ZoneOffset.UTC)),
            training("2", now.atStartOfDay(java.time.ZoneOffset.UTC))
        )
        val trend = useCase(trainings, now = now)
        val direct = EvaluateLevelUseCase()(trainings, now)

        assertEquals(direct.totalXp, trend.last().xp)
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private fun training(id: String, date: java.time.ZonedDateTime) = Training(
        id = id,
        name = "Training $id",
        distance = 10_000.0,
        movingTime = 3600,
        elapsedTime = 3600,
        totalElevationGain = 50.0,
        sportType = "Run",
        startDate = date.toString(),
        maxSpeed = 4.0,
        averageCadence = 0.0,
        averageWatts = 0.0,
        weightedAverageWatts = 0,
        kilojoules = 0.0,
        deviceWatts = false,
        hasHeartrate = false,
        averageHeartrate = null,
        maxHeartrate = 0.0,
        elevHigh = 0.0,
        elevLow = 0.0,
        map = null,
        uploadId = 0L,
        sufferScore = null
    )
}
