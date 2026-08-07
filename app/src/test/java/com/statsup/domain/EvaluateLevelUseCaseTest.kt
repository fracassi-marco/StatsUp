package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class EvaluateLevelUseCaseTest {

    private val useCase = EvaluateLevelUseCase()
    private val today = LocalDate.of(2026, 3, 15)

    // -------------------------------------------------------------------------
    // Empty input
    // -------------------------------------------------------------------------

    @Test
    fun `no trainings gives level 1 with zero xp and no decay`() {
        val level = useCase(emptyList(), now = today)
        assertEquals(1, level.number)
        assertEquals(0, level.totalXp)
        assertEquals(0, level.currentLevelXp)
        assertEquals(200, level.nextLevelXp)
        assertEquals(0, level.daysSinceLastActivity)
        assertFalse(level.isDecaying)
        assertEquals(0, level.dailyDecayRate)
    }

    // -------------------------------------------------------------------------
    // XP accumulation and level thresholds
    // -------------------------------------------------------------------------

    @Test
    fun `exact threshold xp reaches level 2 with no decay when trained today`() {
        val trainings = listOf(trainingWithXp(200, today.atStartOfDay()))
        val level = useCase(trainings, now = today)
        assertEquals(2, level.number)
        assertEquals(200, level.totalXp)
        assertEquals(0, level.currentLevelXp)
        assertEquals(300, level.nextLevelXp) // 500 - 200
        assertEquals(0, level.daysSinceLastActivity)
        assertFalse(level.isDecaying)
    }

    @Test
    fun `xp sums across multiple trainings using the most recent activity date`() {
        val trainings = listOf(
            trainingWithXp(100, today.minusDays(5).atStartOfDay()),
            trainingWithXp(150, today.minusDays(1).atStartOfDay())
        )
        val level = useCase(trainings, now = today)
        assertEquals(1, level.daysSinceLastActivity)
        assertEquals(250, level.totalXp)
    }

    @Test
    fun `reaching the max level leaves no next level`() {
        val trainings = listOf(trainingWithXp(600_000, today.atStartOfDay()))
        val level = useCase(trainings, now = today)
        assertEquals(20, level.number)
        assertEquals(50_000, level.currentLevelXp) // 600000 - 550000
        assertEquals(0, level.nextLevelXp)
    }

    // -------------------------------------------------------------------------
    // Decay logic
    // -------------------------------------------------------------------------

    @Test
    fun `no decay within 3 days of last activity`() {
        val trainings = listOf(trainingWithXp(200, today.minusDays(3).atStartOfDay()))
        val level = useCase(trainings, now = today)
        assertEquals(200, level.totalXp)
        assertFalse(level.isDecaying)
        assertEquals(0, level.dailyDecayRate)
    }

    @Test
    fun `decays at 3 xp per day between 4 and 30 days of inactivity`() {
        val trainings = listOf(trainingWithXp(200, today.minusDays(10).atStartOfDay()))
        val level = useCase(trainings, now = today)
        // decay = (10 - 3) * 3 = 21
        assertEquals(179, level.totalXp)
        assertTrue(level.isDecaying)
        assertEquals(3, level.dailyDecayRate)
    }

    @Test
    fun `decays at 5 xp per day beyond 30 days of inactivity`() {
        val trainings = listOf(trainingWithXp(300, today.minusDays(40).atStartOfDay()))
        val level = useCase(trainings, now = today)
        // decay = 27 * 3 + (40 - 30) * 5 = 81 + 50 = 131
        assertEquals(169, level.totalXp)
        assertTrue(level.isDecaying)
        assertEquals(5, level.dailyDecayRate)
    }

    @Test
    fun `total xp never goes negative when decay exceeds earned xp`() {
        val trainings = listOf(trainingWithXp(10, today.minusDays(100).atStartOfDay()))
        val level = useCase(trainings, now = today)
        assertEquals(0, level.totalXp)
        assertEquals(1, level.number)
        assertEquals(0, level.currentLevelXp)
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun trainingWithXp(xp: Int, date: LocalDateTime) = Training(
        id = "t-$xp-$date",
        name = "Training",
        distance = xp * 1000.0,
        movingTime = 0,
        elapsedTime = 0,
        totalElevationGain = 0.0,
        startDate = date.atZone(ZoneOffset.UTC).toString(),
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
