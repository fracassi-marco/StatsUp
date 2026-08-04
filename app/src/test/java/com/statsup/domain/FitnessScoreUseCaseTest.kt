package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Unit tests for [FitnessScoreUseCase].
 * No mocking needed — it's a pure computation class, like [Trainings] and [WeightStatsUseCase].
 * Fixed `now = 2026-03-15T12:00:00Z` is used throughout.
 */
class FitnessScoreUseCaseTest {

    private val now = ZonedDateTime.of(2026, 3, 15, 12, 0, 0, 0, ZoneOffset.UTC)
    private val useCase = FitnessScoreUseCase()

    @Test
    fun `returns low score with no trainings and no weight entries, all data-dependent factors excluded and redistributed`() {
        val result = useCase(emptyList(), emptyList(), weightTargetKg = 0.0, now = now)

        assertEquals(100.0, factor(result, FitnessFactorType.RECOVERY).score, 0.001)
        assertEquals(0.0, factor(result, FitnessFactorType.TRAINING_LOAD).score, 0.001)
        assertEquals(0.0, factor(result, FitnessFactorType.CONSISTENCY).score, 0.001)
        assertEquals(false, factor(result, FitnessFactorType.LOAD_BALANCE).included)
        assertEquals(false, factor(result, FitnessFactorType.INTENSITY_BALANCE).included)
        assertEquals(false, factor(result, FitnessFactorType.SPORT_VARIETY).included)
        assertEquals(false, factor(result, FitnessFactorType.PERFORMANCE_TREND).included)
        assertEquals(false, factor(result, FitnessFactorType.MEDIUM_TERM_TREND).included)
        assertEquals(false, factor(result, FitnessFactorType.WEIGHT_TREND).included)

        val includedWeights = result.factors.filter { it.included }.sumOf { it.weightPercent }
        assertEquals(100.0, includedWeights, 0.001)

        // Only TRAINING_LOAD(20), RECOVERY(15), CONSISTENCY(15) survive -> redistributed to 40%/30%/30%.
        // score = 0*40% + 100*30% + 0*30% = 30
        assertEquals(30, result.score)
    }

    @Test
    fun `includes weight factor with a single measurement as neutral in maintenance mode`() {
        val entries = listOf(entry(now, 70.0))
        val result = useCase(emptyList(), entries, weightTargetKg = 0.0, now = now)

        val weightFactor = factor(result, FitnessFactorType.WEIGHT_TREND)
        assertEquals(true, weightFactor.included)
        assertEquals(100.0, weightFactor.score, 0.001) // stable (weeklyRate=0) in maintenance mode
        assertEquals(0.0, result.weeklyWeightRateKg)

        // Only TRAINING_LOAD(20), RECOVERY(15), CONSISTENCY(15), WEIGHT_TREND(10) survive (sum 60).
        assertEquals(10.0 / 60.0 * 100.0, weightFactor.weightPercent, 0.001)
        // score = 0*(20/60) + 100*(15/60) + 0*(15/60) + 100*(10/60) = 41.667 -> rounds to 42
        assertEquals(42, result.score)
    }

    @Test
    fun `training load score saturates instead of growing linearly for a huge outlier session`() {
        val moderate = useCase(listOf(training("1", now, sufferScore = 5_000.0)), emptyList(), 0.0, now)
        val huge = useCase(listOf(training("1", now, sufferScore = 50_000.0)), emptyList(), 0.0, now)

        val moderateScore = factor(moderate, FitnessFactorType.TRAINING_LOAD).score
        val hugeScore = factor(huge, FitnessFactorType.TRAINING_LOAD).score

        assertTrue("expected saturation close to 100, got $moderateScore", moderateScore > 90.0 && moderateScore < 100.0)
        assertTrue("10x the load should barely move the score once saturated", hugeScore - moderateScore < 1.0)
    }

    @Test
    fun `consistency score caps at 100 for frequent training over 28 days`() {
        val trainings = (0 until 20).map { daysAgo -> training(daysAgo.toString(), now.minusDays(daysAgo.toLong())) }
        val result = useCase(trainings, emptyList(), 0.0, now)

        assertEquals(20, result.activeDays28)
        assertEquals(100.0, factor(result, FitnessFactorType.CONSISTENCY).score, 0.001)
    }

    @Test
    fun `consistency counts multiple same-day trainings as a single active day`() {
        val trainings = listOf(
            training("1", now.withHour(7)),
            training("2", now.withHour(18)),
        )
        val result = useCase(trainings, emptyList(), 0.0, now)

        assertEquals(1, result.activeDays28)
        assertEquals(1.0 / 16.0 * 100.0, factor(result, FitnessFactorType.CONSISTENCY).score, 0.001)
    }

    @Test
    fun `load balance rewards a steady weekly rhythm and penalizes a load spike after a long lull`() {
        // 8-day spacing (not 7) so only the most recent session falls inside the last-7-days window,
        // keeping the acute and chronic daily averages equal (ratio = 1.0).
        val steady = (0..3).map { training(it.toString(), now.minusDays((it * 8).toLong()), sufferScore = 100.0) }
        val steadyResult = useCase(steady, emptyList(), 0.0, now)
        assertEquals(100.0, factor(steadyResult, FitnessFactorType.LOAD_BALANCE).score, 0.001)

        val spike = (0..6).map { training(it.toString(), now.minusDays(it.toLong()), sufferScore = 100.0) }
        val spikeResult = useCase(spike, emptyList(), 0.0, now)
        assertEquals(0.0, factor(spikeResult, FitnessFactorType.LOAD_BALANCE).score, 0.001)
    }

    @Test
    fun `load balance is excluded when there is no training in the last 28 days`() {
        val result = useCase(emptyList(), emptyList(), 0.0, now)
        assertEquals(false, factor(result, FitnessFactorType.LOAD_BALANCE).included)
    }

    @Test
    fun `intensity balance rewards a polarized mix of easy and hard sessions over an all-hard week`() {
        val polarized = listOf(
            training("1", now, hasHeartrate = true, averageHeartrate = 100.0),
            training("2", now.minusDays(1), hasHeartrate = true, averageHeartrate = 100.0),
            training("3", now.minusDays(2), hasHeartrate = true, averageHeartrate = 100.0),
            training("4", now.minusDays(3), hasHeartrate = true, averageHeartrate = 100.0),
            training("5", now.minusDays(4), hasHeartrate = true, averageHeartrate = 180.0)
        )
        val result = useCase(polarized, emptyList(), 0.0, now)
        assertEquals(100.0, factor(result, FitnessFactorType.INTENSITY_BALANCE).score, 0.001)

        val allHard = polarized.map { it.copy(averageHeartrate = 180.0) }
        val allHardResult = useCase(allHard, emptyList(), 0.0, now)
        assertEquals(0.0, factor(allHardResult, FitnessFactorType.INTENSITY_BALANCE).score, 0.001)
    }

    @Test
    fun `intensity balance is excluded when no recent training has heart rate data`() {
        val result = useCase(listOf(training("1", now)), emptyList(), 0.0, now)
        assertEquals(false, factor(result, FitnessFactorType.INTENSITY_BALANCE).included)
    }

    @Test
    fun `sport variety rewards mixing disciplines over doing a single sport`() {
        val oneSport = listOf(training("1", now, sportType = "Run"), training("2", now.minusDays(1), sportType = "Run"))
        assertEquals(70.0, factor(useCase(oneSport, emptyList(), 0.0, now), FitnessFactorType.SPORT_VARIETY).score, 0.001)

        val twoSports = listOf(training("1", now, sportType = "Run"), training("2", now.minusDays(1), sportType = "Ride"))
        assertEquals(90.0, factor(useCase(twoSports, emptyList(), 0.0, now), FitnessFactorType.SPORT_VARIETY).score, 0.001)

        val threeSports = listOf(
            training("1", now, sportType = "Run"),
            training("2", now.minusDays(1), sportType = "Ride"),
            training("3", now.minusDays(2), sportType = "Swim")
        )
        assertEquals(100.0, factor(useCase(threeSports, emptyList(), 0.0, now), FitnessFactorType.SPORT_VARIETY).score, 0.001)
    }

    @Test
    fun `sport variety is excluded with no recent trainings`() {
        val result = useCase(emptyList(), emptyList(), 0.0, now)
        assertEquals(false, factor(result, FitnessFactorType.SPORT_VARIETY).included)
    }

    @Test
    fun `performance trend rewards a faster recent best pace than the prior 90-day period`() {
        val trainings = listOf(
            training("baseline", now.minusDays(100), distanceM = 5_000.0, movingTime = 1500),
            training("recent", now.minusDays(10), distanceM = 5_000.0, movingTime = 1400)
        )
        val result = useCase(trainings, emptyList(), 0.0, now)
        val expected = 50.0 + (1500.0 - 1400.0) / 1500.0 * 500.0
        assertEquals(expected, factor(result, FitnessFactorType.PERFORMANCE_TREND).score, 0.01)
    }

    @Test
    fun `performance trend is excluded without a comparable run in both periods`() {
        val onlyRecent = listOf(training("1", now.minusDays(10), distanceM = 5_000.0, movingTime = 1400))
        val result = useCase(onlyRecent, emptyList(), 0.0, now)
        assertEquals(false, factor(result, FitnessFactorType.PERFORMANCE_TREND).included)
    }

    @Test
    fun `medium term trend reflects recent monthly volume relative to the historical median`() {
        val recentMonths = listOf(1L, 2L, 3L).map { training("recent-$it", now.minusMonths(it), distanceM = 5_000.0) }
        val baselineMonths = listOf(4L, 5L, 6L).map { training("baseline-$it", now.minusMonths(it), distanceM = 10_000.0) }
        val result = useCase(recentMonths + baselineMonths, emptyList(), 0.0, now)

        assertEquals(50.0, factor(result, FitnessFactorType.MEDIUM_TERM_TREND).score, 0.01)
    }

    @Test
    fun `medium term trend is excluded without at least 3 historical months of data`() {
        val onlyRecent = listOf(training("1", now.minusMonths(1), distanceM = 5_000.0))
        val result = useCase(onlyRecent, emptyList(), 0.0, now)
        assertEquals(false, factor(result, FitnessFactorType.MEDIUM_TERM_TREND).included)
    }

    @Test
    fun `weight trend rewards movement toward the target and penalizes movement away from it`() {
        val losingWeight = listOf(entry(now.minusDays(7), 80.0), entry(now, 79.0))

        val towardTarget = useCase(emptyList(), losingWeight, weightTargetKg = 70.0, now = now)
        assertEquals(100.0, factor(towardTarget, FitnessFactorType.WEIGHT_TREND).score, 0.001)

        val awayFromTarget = useCase(emptyList(), losingWeight, weightTargetKg = 90.0, now = now)
        assertEquals(0.0, factor(awayFromTarget, FitnessFactorType.WEIGHT_TREND).score, 0.001)
    }

    @Test
    fun `weight trend rewards stability when no target is set`() {
        val stable = listOf(entry(now.minusDays(7), 75.0), entry(now, 75.0))
        val oscillating = listOf(entry(now.minusDays(7), 75.0), entry(now, 75.5))

        val stableResult = useCase(emptyList(), stable, weightTargetKg = 0.0, now = now)
        val oscillatingResult = useCase(emptyList(), oscillating, weightTargetKg = 0.0, now = now)

        assertEquals(100.0, factor(stableResult, FitnessFactorType.WEIGHT_TREND).score, 0.001)
        assertEquals(75.0, factor(oscillatingResult, FitnessFactorType.WEIGHT_TREND).score, 0.001)
    }

    @Test
    fun `recovery factor mirrors Trainings#recoveryTime()`() {
        val trainings = listOf(
            training("1", now.minusHours(2), sufferScore = 150.0),
            training("2", now.minusDays(1), sufferScore = 80.0),
        )
        val expectedRecoveryHours = Trainings(trainings, now, Provider.None).recoveryTime()

        val result = useCase(trainings, emptyList(), 0.0, now)

        assertEquals(expectedRecoveryHours, result.recoveryHours, 0.001)
        assertEquals(100.0 * (1.0 - expectedRecoveryHours / 72.0), factor(result, FitnessFactorType.RECOVERY).score, 0.001)
    }

    @Test
    fun `final score is always clamped between 0 and 100 and included factor weights always sum to 100 percent`() {
        val scenarios = listOf(
            useCase(emptyList(), emptyList(), 0.0, now),
            useCase(listOf(training("1", now, sufferScore = 100_000.0)), emptyList(), 0.0, now),
            useCase((0 until 28).map { training(it.toString(), now.minusDays(it.toLong())) }, listOf(entry(now, 70.0)), 65.0, now),
        )

        scenarios.forEach { result ->
            assertTrue(result.score in 0..100)
            val includedWeights = result.factors.filter { it.included }.sumOf { it.weightPercent }
            assertEquals(100.0, includedWeights, 0.01)
        }
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private fun factor(result: FitnessScore, type: FitnessFactorType) =
        result.factors.first { it.type == type }

    private fun entry(date: ZonedDateTime, weightKg: Double) =
        WeightEntry(date = date.toInstant().toEpochMilli(), weightKg = weightKg)

    private fun training(
        id: String,
        date: ZonedDateTime,
        sufferScore: Double? = null,
        movingTime: Int = 3600,
        distanceM: Double = 10_000.0,
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
        uploadId = 0L,
        sufferScore = sufferScore
    )
}
