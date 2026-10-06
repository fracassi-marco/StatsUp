package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class WeightPlanUseCaseTest {

    private val useCase = WeightPlanUseCase()
    private val utc = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 10, 6)

    private fun entry(date: LocalDate, kg: Double) =
        WeightEntry(date = date.atStartOfDay(utc).toInstant().toEpochMilli(), weightKg = kg)

    private fun plan(
        entries: List<WeightEntry>,
        targetKg: Double = 80.0,
        targetDate: LocalDate? = today.plusDays(100),
        planStart: LocalDate? = today.minusDays(50)
    ) = useCase(entries, targetKg, targetDate, planStart, today = today, zone = utc)

    @Test
    fun `returns null without target date, target weight or entries`() {
        val entries = listOf(entry(today, 90.0))
        assertNull(plan(entries, targetDate = null))
        assertNull(plan(entries, targetKg = 0.0))
        assertNull(plan(emptyList()))
    }

    @Test
    fun `planned line goes linearly from start weight to target`() {
        val start = today.minusDays(50)
        val entries = listOf(entry(start, 95.0), entry(today, 88.0))
        val result = plan(entries)!!

        // 150 days from 95 to 80 → -0.1 kg/day, 50 days elapsed → 90 kg today
        assertEquals(90.0, result.plannedToday, 1e-9)
        // The planned line extends all the way to the target date (100 days out),
        // not just to today, so the goal is always visible on the chart.
        assertEquals(today.minusDays(29), result.plannedPoints.first().first)
        assertEquals(today.plusDays(100), result.plannedPoints.last().first)
        assertEquals(130, result.plannedPoints.size)
        assertEquals(today.plusDays(100), result.windowEnd)
        assertEquals(92.9, result.plannedPoints.first().second, 1e-9)
        assertEquals(80.0, result.plannedPoints.last().second, 1e-9)
        assertEquals(-2.0, result.deltaFromPlan, 1e-9)
        assertTrue(result.isLossGoal)
    }

    @Test
    fun `start weight uses last measurement on or before plan start`() {
        val start = today.minusDays(50)
        val entries = listOf(entry(start.minusDays(3), 95.0), entry(start.plusDays(1), 99.0))
        val result = plan(entries)!!
        assertEquals(90.0, result.plannedToday, 1e-9)
    }

    @Test
    fun `actual points keep only the window and the last value per day`() {
        val entries = listOf(
            entry(today.minusDays(40), 92.0),
            entry(today.minusDays(10), 91.0),
            WeightEntry(date = today.minusDays(10).atStartOfDay(utc).toInstant().toEpochMilli() + 3600_000, weightKg = 90.5),
            entry(today, 89.0)
        )
        val result = plan(entries)!!
        assertEquals(listOf(today.minusDays(10) to 90.5, today to 89.0), result.actualPoints)
    }

    @Test
    fun `required weekly rate is computed from latest weight`() {
        val entries = listOf(entry(today, 87.0))
        val result = plan(entries, targetDate = today.plusDays(70), planStart = today)!!
        assertEquals(-0.7, result.requiredWeeklyRate!!, 1e-9)
        assertEquals(87.0, result.plannedToday, 1e-9)
    }

    @Test
    fun `after target date the plan stays at target`() {
        val entries = listOf(entry(today.minusDays(60), 90.0), entry(today, 82.0))
        val result = plan(entries, targetDate = today.minusDays(5), planStart = today.minusDays(60))
        assertNotNull(result)
        assertEquals(80.0, result!!.plannedToday, 1e-9)
        assertNull(result.requiredWeeklyRate)
    }
}
