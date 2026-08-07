package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class EvaluateBadgesUseCaseTest {

    private val useCase = EvaluateBadgesUseCase()
    private val now = ZonedDateTime.of(2026, 3, 15, 12, 0, 0, 0, ZoneOffset.UTC)

    private fun badge(badges: List<Badge>, id: String) = badges.first { it.id == id }

    // -------------------------------------------------------------------------
    // Empty input
    // -------------------------------------------------------------------------

    @Test
    fun `no badges earned for an empty history`() {
        val badges = useCase(emptyList(), 0, 0, emptyMap(), now)
        assertTrue(badges.none { it.earned })
    }

    @Test
    fun `zero goals do not crash and never count as earned`() {
        val trainings = listOf(training("1", now, distanceKm = 5.0))
        val badges = useCase(trainings, monthlyDistanceGoalKm = 0, monthlyTrainingGoal = 0, strings = emptyMap(), now = now)
        assertFalse(badge(badges, "monthly_goal_dist").earned)
        assertFalse(badge(badges, "monthly_goal_freq").earned)
    }

    // -------------------------------------------------------------------------
    // Monthly badges
    // -------------------------------------------------------------------------

    @Test
    fun `monthly distance badges are earned at exact thresholds`() {
        val trainings = listOf(training("1", now, distanceKm = 100.0))
        val badges = useCase(trainings, 0, 0, emptyMap(), now)
        assertTrue(badge(badges, "monthly_bronze").earned)
        assertTrue(badge(badges, "monthly_silver").earned)
        assertFalse(badge(badges, "monthly_gold").earned)
        assertFalse(badge(badges, "monthly_diamond").earned)
    }

    @Test
    fun `monthly goal badges reflect percentage progress`() {
        val trainings = listOf(training("1", now, distanceKm = 12.0))
        val badges = useCase(trainings, monthlyDistanceGoalKm = 10, monthlyTrainingGoal = 2, strings = emptyMap(), now = now)
        val goalDist = badge(badges, "monthly_goal_dist")
        assertTrue(goalDist.earned)
        assertEquals(12.0, goalDist.currentValue!!, 0.0001)
        assertEquals(10.0, goalDist.targetValue!!, 0.0001)
        assertFalse(badge(badges, "monthly_goal_freq").earned) // only 1 training vs goal of 2
    }

    @Test
    fun `monthly streak badge counts consecutive calendar days in the month`() {
        val trainings = (1..7).map { day -> training("d$day", now.withDayOfMonth(day)) }
        val badges = useCase(trainings, 0, 0, emptyMap(), now)
        assertTrue(badge(badges, "monthly_streak_week").earned)
    }

    @Test
    fun `monthly streak badge not earned when days are not consecutive`() {
        val trainings = listOf(
            training("1", now.withDayOfMonth(1)),
            training("2", now.withDayOfMonth(3)),
            training("3", now.withDayOfMonth(5))
        )
        val badges = useCase(trainings, 0, 0, emptyMap(), now)
        assertFalse(badge(badges, "monthly_streak_week").earned)
    }

    @Test
    fun `trainings from other months do not count toward monthly badges`() {
        val trainings = listOf(training("1", now.minusMonths(1), distanceKm = 200.0))
        val badges = useCase(trainings, 0, 0, emptyMap(), now)
        assertFalse(badge(badges, "monthly_first").earned)
        assertFalse(badge(badges, "monthly_bronze").earned)
    }

    // -------------------------------------------------------------------------
    // Annual badges
    // -------------------------------------------------------------------------

    @Test
    fun `yearly distance badge earned when year total crosses threshold across months`() {
        val trainings = listOf(
            training("1", now.withMonth(1).withDayOfMonth(10), distanceKm = 300.0),
            training("2", now.withMonth(2).withDayOfMonth(10), distanceKm = 250.0)
        )
        val badges = useCase(trainings, 0, 0, emptyMap(), now)
        assertTrue(badge(badges, "yearly_500km").earned)
        assertFalse(badge(badges, "yearly_1000km").earned)
    }

    @Test
    fun `yearly all months badge requires activity in every month up to now`() {
        val trainings = listOf(
            training("jan", now.withMonth(1).withDayOfMonth(5)),
            training("feb", now.withMonth(2).withDayOfMonth(5)),
            training("mar", now.withMonth(3).withDayOfMonth(5))
        )
        val badges = useCase(trainings, 0, 0, emptyMap(), now)
        assertTrue(badge(badges, "yearly_all_months").earned)
    }

    @Test
    fun `yearly all months badge not earned when a month is skipped`() {
        val trainings = listOf(
            training("jan", now.withMonth(1).withDayOfMonth(5)),
            training("mar", now.withMonth(3).withDayOfMonth(5))
            // February skipped
        )
        val badges = useCase(trainings, 0, 0, emptyMap(), now)
        assertFalse(badge(badges, "yearly_all_months").earned)
    }

    // -------------------------------------------------------------------------
    // All-time badges
    // -------------------------------------------------------------------------

    @Test
    fun `all-time distance record badges use the single best training`() {
        val trainings = listOf(
            training("1", now.minusYears(2), distanceKm = 21.5),
            training("2", now, distanceKm = 10.0)
        )
        val badges = useCase(trainings, 0, 0, emptyMap(), now)
        assertTrue(badge(badges, "alltime_halfmarathon").earned)
        assertFalse(badge(badges, "alltime_marathon").earned)
    }

    @Test
    fun `all-time altitude badges use the highest elevation ever reached`() {
        val trainings = listOf(training("1", now.minusYears(1), elevHigh = 2500.0))
        val badges = useCase(trainings, 0, 0, emptyMap(), now)
        assertTrue(badge(badges, "alltime_altitude2k").earned)
        assertFalse(badge(badges, "alltime_altitude3k").earned)
    }

    @Test
    fun `all-time best streak spans across months`() {
        // 5 consecutive days straddling a month boundary
        val trainings = listOf(
            training("1", now.withMonth(2).withDayOfMonth(27)),
            training("2", now.withMonth(2).withDayOfMonth(28)),
            training("3", now.withMonth(3).withDayOfMonth(1)),
            training("4", now.withMonth(3).withDayOfMonth(2)),
            training("5", now.withMonth(3).withDayOfMonth(3))
        )
        val badges = useCase(trainings, 0, 0, emptyMap(), now)
        // Not enough for the 60-day all-time streak badge, but exercises bestStreakOf across months
        assertFalse(badge(badges, "alltime_best_streak").earned)
    }

    // -------------------------------------------------------------------------
    // String resolution
    // -------------------------------------------------------------------------

    @Test
    fun `badge uses provided localized strings when available`() {
        val trainings = listOf(training("1", now, distanceKm = 1.0))
        val strings = mapOf("monthly_first" to BadgeStringSet("First steps", "Complete your first training"))
        val badges = useCase(trainings, 0, 0, strings, now)
        val first = badge(badges, "monthly_first")
        assertEquals("First steps", first.name)
        assertEquals("Complete your first training", first.description)
    }

    @Test
    fun `badge falls back to its id as name and empty description when not provided`() {
        val trainings = listOf(training("1", now, distanceKm = 1.0))
        val badges = useCase(trainings, 0, 0, emptyMap(), now)
        val first = badge(badges, "monthly_first")
        assertEquals("monthly_first", first.name)
        assertEquals("", first.description)
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun training(
        id: String,
        date: ZonedDateTime,
        distanceKm: Double = 0.0,
        elevHigh: Double = 0.0,
        elevationGain: Double = 0.0,
        sportType: String = "Run"
    ) = Training(
        id = id,
        name = "Training $id",
        distance = distanceKm * 1000.0,
        movingTime = 0,
        elapsedTime = 0,
        totalElevationGain = elevationGain,
        sportType = sportType,
        startDate = date.toString(),
        maxSpeed = 0.0,
        averageCadence = 0.0,
        averageWatts = 0.0,
        weightedAverageWatts = 0,
        kilojoules = 0.0,
        deviceWatts = false,
        maxHeartrate = 0.0,
        elevHigh = elevHigh,
        elevLow = 0.0,
        map = null,
        uploadId = 0L,
        sufferScore = null
    )
}
