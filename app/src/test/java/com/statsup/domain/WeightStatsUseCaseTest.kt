package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class WeightStatsUseCaseTest {

    private val useCase = WeightStatsUseCase()

    private fun epochMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

    private fun entry(date: LocalDate, kg: Double) =
        WeightEntry(date = epochMillis(date), weightKg = kg)

    // -------------------------------------------------------------------------
    // Empty input
    // -------------------------------------------------------------------------

    @Test
    fun `returns empty stats for no entries`() {
        val stats = useCase(emptyList(), 175, 90.0)
        assertNull(stats.latestWeight)
        assertNull(stats.bmi)
        assertNull(stats.personalBest)
    }

    // -------------------------------------------------------------------------
    // BMI calculation
    // -------------------------------------------------------------------------

    @Test
    fun `calculates correct bmi for normal range`() {
        val entries = listOf(entry(LocalDate.now(), 70.0))
        val stats = useCase(entries, 175, 0.0)
        val expected = 70.0 / (1.75 * 1.75)
        assertEquals(expected, stats.bmi!!, 0.01)
        assertEquals(BmiCategory.NORMAL, stats.bmiCategory)
    }

    @Test
    fun `returns null bmi when height is zero`() {
        val entries = listOf(entry(LocalDate.now(), 70.0))
        val stats = useCase(entries, 0, 0.0)
        assertNull(stats.bmi)
    }

    @Test
    fun `classifies obese correctly`() {
        // 120 / (1.75^2) ≈ 39.2 → OBESE_2 (35-40)
        val entries = listOf(entry(LocalDate.now(), 120.0))
        val stats = useCase(entries, 175, 0.0)
        assertTrue(stats.bmi!! > 35.0)
        assertEquals(BmiCategory.OBESE_2, stats.bmiCategory)
    }

    @Test
    fun `classifies underweight correctly`() {
        val entries = listOf(entry(LocalDate.now(), 50.0))
        val stats = useCase(entries, 175, 0.0)
        assertEquals(BmiCategory.UNDERWEIGHT, stats.bmiCategory)
    }

    // -------------------------------------------------------------------------
    // Personal best and weight lost
    // -------------------------------------------------------------------------

    @Test
    fun `personal best is minimum weight ever`() {
        val entries = listOf(
            entry(LocalDate.now().minusDays(10), 100.0),
            entry(LocalDate.now().minusDays(5), 95.0),
            entry(LocalDate.now(), 98.0)
        )
        val stats = useCase(entries, 175, 0.0)
        assertEquals(95.0, stats.personalBest!!, 0.01)
    }

    @Test
    fun `weightLostFromMax equals max minus latest`() {
        val entries = listOf(
            entry(LocalDate.now().minusDays(5), 100.0),
            entry(LocalDate.now(), 90.0)
        )
        val stats = useCase(entries, 175, 0.0)
        assertEquals(10.0, stats.weightLostFromMax, 0.01)
    }

    // -------------------------------------------------------------------------
    // Target prediction
    // -------------------------------------------------------------------------

    @Test
    fun `predicts target date for steadily decreasing weight`() {
        val today = LocalDate.now()
        val entries = (0 until 30).map { i ->
            entry(today.minusDays(29L - i), 100.0 - i * 0.2)
        }
        val stats = useCase(entries, 175, 94.0)
        assertTrue(stats.canReachTarget)
        assertNotNull(stats.predictedTargetDate)
        assertTrue(stats.predictedTargetDate!!.isAfter(today))
    }

    @Test
    fun `returns canReachTarget=false when trend is gaining and target is lower`() {
        val today = LocalDate.now()
        val entries = (0 until 15).map { i ->
            entry(today.minusDays(14L - i), 90.0 + i * 0.3)
        }
        val stats = useCase(entries, 175, 85.0)
        assertTrue(!stats.canReachTarget)
        assertNull(stats.predictedTargetDate)
    }

    @Test
    fun `returns no prediction with only one entry`() {
        val entries = listOf(entry(LocalDate.now(), 90.0))
        val stats = useCase(entries, 175, 80.0)
        assertTrue(!stats.canReachTarget || stats.predictedTargetDate == null)
    }

    // -------------------------------------------------------------------------
    // Measurement streak
    // -------------------------------------------------------------------------

    @Test
    fun `streak is 1 for single entry`() {
        val entries = listOf(entry(LocalDate.now(), 90.0))
        val stats = useCase(entries, 175, 0.0)
        assertEquals(1, stats.measurementStreak)
    }

    @Test
    fun `streak counts consecutive weeks`() {
        val today = LocalDate.now()
        val entries = (0 until 3).map { w ->
            entry(today.minusWeeks(w.toLong()), 90.0)
        }
        val stats = useCase(entries, 175, 0.0)
        assertEquals(3, stats.measurementStreak)
    }

    // -------------------------------------------------------------------------
    // Ideal weight range
    // -------------------------------------------------------------------------

    @Test
    fun `calculates ideal weight range from height`() {
        val entries = listOf(entry(LocalDate.now(), 80.0))
        val stats = useCase(entries, 175, 0.0)
        // 18.5 * 1.75^2 = 56.66, 24.9 * 1.75^2 = 76.26
        assertEquals(56.66, stats.idealWeightMin!!, 0.1)
        assertEquals(76.26, stats.idealWeightMax!!, 0.1)
    }

    @Test
    fun `ideal weight range is null when height is zero`() {
        val entries = listOf(entry(LocalDate.now(), 80.0))
        val stats = useCase(entries, 0, 0.0)
        assertNull(stats.idealWeightMin)
        assertNull(stats.idealWeightMax)
    }

    // -------------------------------------------------------------------------
    // Chart points ordering
    // -------------------------------------------------------------------------

    @Test
    fun `chart points are sorted by date ascending`() {
        val today = LocalDate.now()
        val entries = listOf(
            entry(today, 90.0),
            entry(today.minusDays(5), 95.0),
            entry(today.minusDays(10), 100.0)
        )
        val stats = useCase(entries, 175, 0.0)
        val dates = stats.chartPoints.map { it.first }
        assertEquals(dates.sorted(), dates)
    }

    @Test
    fun `chart points include one entry per day with interpolated values`() {
        val today = LocalDate.now()
        val entries = listOf(
            entry(today.minusDays(4), 100.0),
            entry(today, 80.0)
        )
        val stats = useCase(entries, 175, 0.0)
        // 4 days span → 5 daily points (day 0..4)
        assertEquals(5, stats.chartPoints.size)
        // First and last match actual measurements
        assertEquals(100.0, stats.chartPoints.first().second, 0.01)
        assertEquals(80.0, stats.chartPoints.last().second, 0.01)
        // Midpoint (day 2 of 4) should be interpolated: 100 + (80-100)*(2/4) = 90
        assertEquals(90.0, stats.chartPoints[2].second, 0.01)
    }
}
