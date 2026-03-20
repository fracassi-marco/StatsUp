package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.pow

/**
 * Unit tests for [Trainings] domain class.
 * No mocking needed — Trainings is a pure computation class.
 * Fixed `now = 2026-03-15T12:00:00Z` is used throughout.
 */
class TrainingsTest {

    private val now = ZonedDateTime.of(2026, 3, 15, 12, 0, 0, 0, ZoneOffset.UTC)

    // -------------------------------------------------------------------------
    // currentStreak
    // -------------------------------------------------------------------------

    @Test
    fun `currentStreak returns 0 for empty list`() {
        assertEquals(0, trainings().currentStreak())
    }

    @Test
    fun `currentStreak returns 1 when only today has activity`() {
        val ts = listOf(run(1, now))
        assertEquals(1, trainings(ts).currentStreak())
    }

    @Test
    fun `currentStreak counts consecutive days ending today`() {
        val ts = listOf(
            run(1, now),
            run(2, now.minusDays(1)),
            run(3, now.minusDays(2))
        )
        assertEquals(3, trainings(ts).currentStreak())
    }

    @Test
    fun `currentStreak counts consecutive days ending yesterday when today is rest`() {
        // Today has no activity → algorithm starts from yesterday
        val ts = listOf(
            run(1, now.minusDays(1)),
            run(2, now.minusDays(2))
        )
        assertEquals(2, trainings(ts).currentStreak())
    }

    @Test
    fun `currentStreak resets at a gap`() {
        // Streak: today + yesterday, then a gap, then older training
        val ts = listOf(
            run(1, now),
            run(2, now.minusDays(1)),
            run(3, now.minusDays(3)) // gap on day -2
        )
        assertEquals(2, trainings(ts).currentStreak())
    }

    @Test
    fun `currentStreak is 0 when last activity was 2 or more days ago`() {
        val ts = listOf(run(1, now.minusDays(2)))
        assertEquals(0, trainings(ts).currentStreak())
    }

    @Test
    fun `currentStreak handles multiple trainings on the same day correctly`() {
        // Two trainings today should still count as 1 day
        val ts = listOf(
            run(1, now.withHour(8)),
            run(2, now.withHour(18)),
            run(3, now.minusDays(1))
        )
        assertEquals(2, trainings(ts).currentStreak())
    }

    // -------------------------------------------------------------------------
    // bestStreak
    // -------------------------------------------------------------------------

    @Test
    fun `bestStreak returns 0 for empty list`() {
        assertEquals(0, trainings().bestStreak())
    }

    @Test
    fun `bestStreak returns 1 for a single training`() {
        val ts = listOf(run(1, now.minusDays(10)))
        assertEquals(1, trainings(ts).bestStreak())
    }

    @Test
    fun `bestStreak counts consecutive days correctly`() {
        val ts = listOf(
            run(1, now.minusDays(5)),
            run(2, now.minusDays(4)),
            run(3, now.minusDays(3))
        )
        assertEquals(3, trainings(ts).bestStreak())
    }

    @Test
    fun `bestStreak returns the best over multiple streaks`() {
        val ts = listOf(
            // Streak of 1
            run(1, now.minusDays(20)),
            // Gap
            // Streak of 3
            run(2, now.minusDays(10)),
            run(3, now.minusDays(9)),
            run(4, now.minusDays(8)),
            // Gap
            // Streak of 2
            run(5, now.minusDays(3)),
            run(6, now.minusDays(2))
        )
        assertEquals(3, trainings(ts).bestStreak())
    }

    @Test
    fun `bestStreak handles multiple trainings on same day via distinct`() {
        // Two trainings on same day → only 1 unique day
        val ts = listOf(
            run(1, now.minusDays(2).withHour(8)),
            run(2, now.minusDays(2).withHour(18)),
            run(3, now.minusDays(1))
        )
        assertEquals(2, trainings(ts).bestStreak())
    }

    // -------------------------------------------------------------------------
    // bestEfforts
    // -------------------------------------------------------------------------

    @Test
    fun `bestEfforts returns empty when no runs`() {
        val ts = listOf(run(1, now, sportType = "Ride")) // cycling, not running
        assertTrue(trainings(ts).bestEfforts().isEmpty())
    }

    @Test
    fun `bestEfforts returns empty when all runs are shorter than 1km`() {
        val ts = listOf(run(1, now, distanceM = 500.0, movingTime = 120))
        assertTrue(trainings(ts).bestEfforts().isEmpty())
    }

    @Test
    fun `bestEfforts includes targets covered by the run distance`() {
        // 10km run → covers 1km, 5km, 10km targets
        val ts = listOf(run(1, now, distanceM = 10_000.0, movingTime = 3600))
        val efforts = trainings(ts).bestEfforts()
        val labels = efforts.map { it.label }
        assertTrue(labels.contains("1 km"))
        assertTrue(labels.contains("5 km"))
        assertTrue(labels.contains("10 km"))
        assertTrue(labels.none { it == "Half M." }) // run not long enough
        assertTrue(labels.none { it == "Marathon" })
    }

    @Test
    fun `bestEfforts picks the run with the best average pace`() {
        val fastRun = run(1, now, distanceM = 10_000.0, movingTime = 3000) // 5 min/km
        val slowRun = run(2, now.minusDays(1), distanceM = 10_000.0, movingTime = 3600) // 6 min/km
        val efforts = trainings(listOf(fastRun, slowRun)).bestEfforts()

        val effort5km = efforts.first { it.label == "5 km" }
        // Fast run: secs = 3000 / 10000 * 5000 = 1500s
        assertEquals(1500, effort5km.timeSeconds)
    }

    @Test
    fun `bestEfforts interpolates time correctly`() {
        // 20km run in 6000s (pace = 0.3 s/m = 5 min/km)
        // Best effort 5km: 6000 / 20000 * 5000 = 1500s
        val ts = listOf(run(1, now, distanceM = 20_000.0, movingTime = 6000))
        val effort5km = trainings(ts).bestEfforts().first { it.label == "5 km" }
        assertEquals(1500, effort5km.timeSeconds)
    }

    @Test
    fun `bestEfforts returns correct trainingId and date`() {
        val training = run(42L, now, distanceM = 10_000.0, movingTime = 3600)
        val efforts = trainings(listOf(training)).bestEfforts()
        val effort1km = efforts.first { it.label == "1 km" }
        assertEquals(42L, effort1km.trainingId)
        assertEquals(now.toLocalDate(), effort1km.date.toLocalDate())
    }

    @Test
    fun `bestEfforts paceMinPerKm is consistent with timeSeconds`() {
        val ts = listOf(run(1, now, distanceM = 10_000.0, movingTime = 3000))
        val effort = trainings(ts).bestEfforts().first { it.label == "5 km" }
        // secs = 3000/10000*5000 = 1500. pace = 1500 / 5.0 / 60 = 5.0 min/km
        assertEquals(5.0, effort.paceMinPerKm, 0.01)
    }

    // -------------------------------------------------------------------------
    // performancePredictions (Riegel formula: T2 = T1 × (D2/D1)^1.06)
    // -------------------------------------------------------------------------

    @Test
    fun `performancePredictions returns empty when no runs`() {
        assertTrue(trainings(emptyList()).performancePredictions().isEmpty())
    }

    @Test
    fun `performancePredictions returns empty when all runs are shorter than 1km`() {
        val ts = listOf(run(1, now, distanceM = 800.0, movingTime = 200))
        assertTrue(trainings(ts).performancePredictions().isEmpty())
    }

    @Test
    fun `performancePredictions produces 4 targets`() {
        val ts = listOf(run(1, now, distanceM = 10_000.0, movingTime = 3600))
        val predictions = trainings(ts).performancePredictions()
        assertEquals(4, predictions.size)
        assertEquals(listOf("5 km", "10 km", "Half M.", "Marathon"), predictions.map { it.label })
    }

    @Test
    fun `performancePredictions for same distance as reference equals reference time`() {
        // Reference: 10km in 3600s. Prediction for 10km: 3600 * (10000/10000)^1.06 = 3600
        val ts = listOf(run(1, now, distanceM = 10_000.0, movingTime = 3600))
        val pred10km = trainings(ts).performancePredictions().first { it.label == "10 km" }
        assertEquals(3600, pred10km.timeSeconds)
    }

    @Test
    fun `performancePredictions uses Riegel formula correctly for 5km`() {
        // Reference: 10km in 3600s
        // T2 = 3600 * (5000/10000)^1.06
        val ts = listOf(run(1, now, distanceM = 10_000.0, movingTime = 3600))
        val expected = (3600 * (5000.0 / 10000.0).pow(1.06)).toInt()
        val pred5km = trainings(ts).performancePredictions().first { it.label == "5 km" }
        assertEquals(expected, pred5km.timeSeconds)
    }

    @Test
    fun `performancePredictions picks the fastest pace run as reference`() {
        // Fast run: 5km in 1000s (best pace)
        // Slow run: 10km in 3600s
        val fastRun = run(1, now, distanceM = 5_000.0, movingTime = 1000)
        val slowRun = run(2, now.minusDays(1), distanceM = 10_000.0, movingTime = 3600)
        val predictions = trainings(listOf(fastRun, slowRun)).performancePredictions()
        // Reference should be fastRun (lower movingTime/distance = 0.2 vs 0.36)
        val pred5km = predictions.first { it.label == "5 km" }
        val expectedFromFast = (1000 * (5000.0 / 5000.0).pow(1.06)).toInt() // = 1000
        assertEquals(expectedFromFast, pred5km.timeSeconds)
    }

    @Test
    fun `performancePredictions paceMinPerKm is consistent with timeSeconds`() {
        val ts = listOf(run(1, now, distanceM = 10_000.0, movingTime = 3000))
        val pred = trainings(ts).performancePredictions().first { it.label == "5 km" }
        val expectedPace = pred.timeSeconds / (5000.0 / 1000.0) / 60.0
        assertEquals(expectedPace, pred.paceMinPerKm, 0.001)
    }

    // -------------------------------------------------------------------------
    // autoDistanceTarget / autoTrainingTarget
    // -------------------------------------------------------------------------

    @Test
    fun `autoDistanceTarget returns fallback when no historical data`() {
        val result = trainings(emptyList()).autoDistanceTarget(fallbackKm = 42)
        assertEquals(42, result)
    }

    @Test
    fun `autoDistanceTarget applies 5 percent progression to median`() {
        // Two past months, 100km each → median = 100, result = (100 * 1.05).toInt() = 105
        val ts = listOf(
            run(1, now.minusMonths(1), distanceM = 100_000.0),
            run(2, now.minusMonths(2), distanceM = 100_000.0)
        )
        assertEquals(105, trainings(ts).autoDistanceTarget())
    }

    @Test
    fun `autoDistanceTarget takes median of past months, not current month`() {
        // Current month training should NOT count
        val currentMonthRun = run(1, now, distanceM = 999_999.0) // huge distance
        val pastRun = run(2, now.minusMonths(1), distanceM = 100_000.0) // 100km
        val ts = listOf(currentMonthRun, pastRun)
        // Only pastRun is in the 12 completed months → median = 100 → target = 105
        assertEquals(105, trainings(ts).autoDistanceTarget())
    }

    @Test
    fun `autoDistanceTarget uses median correctly with even number of months`() {
        // Two months: 100km and 200km → median = 150 → (150 * 1.05).toInt() = 157
        val ts = listOf(
            run(1, now.minusMonths(1), distanceM = 100_000.0),
            run(2, now.minusMonths(2), distanceM = 200_000.0)
        )
        assertEquals(157, trainings(ts).autoDistanceTarget())
    }

    @Test
    fun `autoDistanceTarget result is at least 1`() {
        // Edge case: very short distances → ensure coerceAtLeast(1) works
        val ts = listOf(run(1, now.minusMonths(1), distanceM = 1.0)) // 0.001 km
        assertTrue(trainings(ts).autoDistanceTarget() >= 1)
    }

    @Test
    fun `autoTrainingTarget returns fallback when no historical data`() {
        assertEquals(8, trainings(emptyList()).autoTrainingTarget(fallbackCount = 8))
    }

    @Test
    fun `autoTrainingTarget counts sessions per month and applies progression`() {
        // 4 sessions in one past month → median = 4 → (4 * 1.05).toInt() = 4
        val ts = (1..4).map { i -> run(i.toLong(), now.minusMonths(1).withDayOfMonth(i)) }
        assertEquals(4, trainings(ts).autoTrainingTarget())
    }

    @Test
    fun `autoTrainingTarget ignores months with no trainings in median`() {
        // Only 1 month has data, other 11 are empty → only that month contributes
        val ts = listOf(
            run(1, now.minusMonths(3), distanceM = 10_000.0),
            run(2, now.minusMonths(3).withDayOfMonth(5), distanceM = 10_000.0)
        )
        // 2 sessions in 1 month → median = 2.0 → (2 * 1.05).toInt() = 2
        assertEquals(2, trainings(ts).autoTrainingTarget())
    }

    // -------------------------------------------------------------------------
    // hrZoneDistribution
    // -------------------------------------------------------------------------

    @Test
    fun `hrZoneDistribution returns all-zero map when no trainings with HR`() {
        val ts = listOf(run(1, now)) // hasHeartrate = false by default
        val zones = trainings(ts).hrZoneDistribution()
        assertEquals(mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0), zones)
    }

    @Test
    fun `hrZoneDistribution returns all-zero map for empty list`() {
        val zones = trainings().hrZoneDistribution()
        assertEquals(mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0), zones)
    }

    @Test
    fun `hrZoneDistribution assigns zone 1 when HR is below 60 percent of maxHR`() {
        // maxHr = 190.0 (hardcoded), zone 1 < 114 bpm
        val ts = listOf(run(1, now, hasHeartrate = true, averageHeartrate = 100.0))
        val zones = trainings(ts).hrZoneDistribution()
        assertEquals(1, zones[1])
        assertEquals(0, zones[2])
    }

    @Test
    fun `hrZoneDistribution assigns zone 2 for HR between 60 and 70 percent`() {
        // zone 2: 114 <= HR < 133 → use 120 bpm
        val ts = listOf(run(1, now, hasHeartrate = true, averageHeartrate = 120.0))
        val zones = trainings(ts).hrZoneDistribution()
        assertEquals(1, zones[2])
        assertEquals(0, zones[1])
    }

    @Test
    fun `hrZoneDistribution assigns zone 3 for HR between 70 and 80 percent`() {
        // zone 3: 133 <= HR < 152 → use 140 bpm
        val ts = listOf(run(1, now, hasHeartrate = true, averageHeartrate = 140.0))
        assertEquals(1, trainings(ts).hrZoneDistribution()[3])
    }

    @Test
    fun `hrZoneDistribution assigns zone 4 for HR between 80 and 90 percent`() {
        // zone 4: 152 <= HR < 171 → use 160 bpm
        val ts = listOf(run(1, now, hasHeartrate = true, averageHeartrate = 160.0))
        assertEquals(1, trainings(ts).hrZoneDistribution()[4])
    }

    @Test
    fun `hrZoneDistribution assigns zone 5 for HR at or above 90 percent`() {
        // zone 5: HR >= 171 → use 180 bpm
        val ts = listOf(run(1, now, hasHeartrate = true, averageHeartrate = 180.0))
        assertEquals(1, trainings(ts).hrZoneDistribution()[5])
    }

    @Test
    fun `hrZoneDistribution only counts trainings in current month`() {
        // One training this month (zone 5) and one last month (zone 1) — only current month counts
        val thisMonth = run(1, now, hasHeartrate = true, averageHeartrate = 180.0)
        val lastMonth = run(2, now.minusMonths(1), hasHeartrate = true, averageHeartrate = 100.0)
        val zones = trainings(listOf(thisMonth, lastMonth)).hrZoneDistribution()
        assertEquals(1, zones[5])
        assertEquals(0, zones[1]) // last month excluded
    }

    @Test
    fun `hrZoneDistribution ignores trainings without HR data`() {
        val noHr = run(1, now, hasHeartrate = false)
        val zones = trainings(listOf(noHr)).hrZoneDistribution()
        assertEquals(mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0), zones)
    }

    @Test
    fun `hrZoneDistribution distributes multiple trainings across zones`() {
        val ts = listOf(
            run(1, now, hasHeartrate = true, averageHeartrate = 100.0),  // zone 1
            run(2, now, hasHeartrate = true, averageHeartrate = 120.0),  // zone 2
            run(3, now, hasHeartrate = true, averageHeartrate = 140.0),  // zone 3
            run(4, now, hasHeartrate = true, averageHeartrate = 160.0),  // zone 4
            run(5, now, hasHeartrate = true, averageHeartrate = 180.0)   // zone 5
        )
        val zones = trainings(ts).hrZoneDistribution()
        (1..5).forEach { z -> assertEquals(1, zones[z]) }
    }

    // -------------------------------------------------------------------------
    // ofMonth
    // -------------------------------------------------------------------------

    @Test
    fun `ofMonth returns only trainings in the current month and year`() {
        val thisMonth = run(1, now)
        val lastMonth = run(2, now.minusMonths(1))
        val lastYear = run(3, now.minusYears(1))
        val result = trainings(listOf(thisMonth, lastMonth, lastYear)).ofMonth()
        assertEquals(listOf(thisMonth), result)
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun trainings(list: List<Training> = emptyList()) =
        Trainings(list, now = now, provider = Provider.None)

    private fun run(
        id: Long,
        date: ZonedDateTime,
        distanceM: Double = 10_000.0,
        movingTime: Int = 3600,
        sportType: String = "Run",
        hasHeartrate: Boolean = false,
        averageHeartrate: Double? = null
    ) = Training(
        id = id,
        name = "Training $id",
        distance = distanceM,
        movingTime = movingTime,
        elapsedTime = movingTime,
        totalElevationGain = 50.0,
        sportType = sportType,
        startDate = date.toString(),
        maxSpeed = 4.0,
        averageCadence = 0.0,
        averageWatts = 0.0,
        weightedAverageWatts = 0,
        kilojoules = 0.0,
        deviceWatts = false,
        hasHeartrate = hasHeartrate,
        averageHeartrate = averageHeartrate,
        maxHeartrate = 0.0,
        elevHigh = 0.0,
        elevLow = 0.0,
        map = null,
        uploadId = id * 10,
        sufferScore = null
    )
}
