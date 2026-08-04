package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Unit tests for [FitnessScoreTrendUseCase].
 * No mocking needed — it's a pure computation class, like [FitnessScoreUseCase].
 * Fixed `now = 2026-03-15T12:00:00Z` is used throughout.
 */
class FitnessScoreTrendUseCaseTest {

    private val now = ZonedDateTime.of(2026, 3, 15, 12, 0, 0, 0, ZoneOffset.UTC)
    private val useCase = FitnessScoreTrendUseCase()

    @Test
    fun `returns one point per day covering the last 30 days, oldest first`() {
        val result = useCase(emptyList(), emptyList(), weightTargetKg = 0.0, now = now)

        assertEquals(30, result.size)
        assertEquals(now.minusDays(29).toLocalDate(), result.first().date.toLocalDate())
        assertEquals(now.toLocalDate(), result.last().date.toLocalDate())
    }

    @Test
    fun `each point only sees trainings up to that day`() {
        val futureTraining = listOf(training("1", now))
        val baselineScore = FitnessScoreUseCase()(emptyList(), emptyList(), 0.0, now.minusDays(1)).score

        val resultBeforeTraining = useCase(futureTraining, emptyList(), 0.0, now = now.minusDays(1))
        val resultOnTrainingDay = useCase(futureTraining, emptyList(), 0.0, now = now)

        assertEquals(baselineScore, resultBeforeTraining.last().score)
        assertTrue(resultOnTrainingDay.last().score != baselineScore)
    }

    @Test
    fun `matches a direct FitnessScoreUseCase call for the most recent day`() {
        val trainings = listOf(training("1", now.minusDays(2)), training("2", now))
        val trend = useCase(trainings, emptyList(), weightTargetKg = 0.0, now = now)
        val direct = FitnessScoreUseCase()(trainings, emptyList(), weightTargetKg = 0.0, now = now)

        assertEquals(direct.score, trend.last().score)
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private fun training(id: String, date: ZonedDateTime) = Training(
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
