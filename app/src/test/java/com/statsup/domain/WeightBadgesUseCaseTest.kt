package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeightBadgesUseCaseTest {

    private val useCase = WeightBadgesUseCase()

    private fun entry(daysAgo: Int, kg: Double, now: Long = System.currentTimeMillis()) =
        WeightEntry(date = now - daysAgo * 86_400_000L, weightKg = kg)

    private fun find(badges: List<Badge>, id: String) = badges.firstOrNull { it.id == id }

    // -------------------------------------------------------------------------
    // Empty input
    // -------------------------------------------------------------------------

    @Test
    fun `returns no badges for empty history`() {
        val badges = useCase(emptyList(), latestWeight = 0.0, targetKg = 0.0, heightCm = 0, streak = 0, bmi = null)
        assertTrue(badges.isEmpty())
    }

    @Test
    fun `weight_first is always earned when there is at least one entry`() {
        val entries = listOf(entry(0, 80.0))
        val badges = useCase(entries, latestWeight = 80.0, targetKg = 0.0, heightCm = 0, streak = 0, bmi = null)
        assertTrue(find(badges, "weight_first")!!.earned)
    }

    // -------------------------------------------------------------------------
    // Loss-from-max badges
    // -------------------------------------------------------------------------

    @Test
    fun `loss badges use the historical maximum weight, not just the first entry`() {
        val entries = listOf(entry(60, 95.0), entry(30, 100.0), entry(1, 90.0)) // max is the middle entry
        val badges = useCase(entries, latestWeight = 90.0, targetKg = 0.0, heightCm = 0, streak = 0, bmi = null)
        // lostFromMax = 100 - 90 = 10
        assertTrue(find(badges, "weight_minus1")!!.earned)
        assertTrue(find(badges, "weight_minus5")!!.earned)
        assertTrue(find(badges, "weight_minus10")!!.earned)
        val minus20 = find(badges, "weight_minus20")!!
        assertFalse(minus20.earned)
        assertEquals(10.0, minus20.currentValue!!, 0.0001)
    }

    // -------------------------------------------------------------------------
    // Target badge
    // -------------------------------------------------------------------------

    @Test
    fun `weight_target is earned when latest weight is at or below the target`() {
        val entries = listOf(entry(1, 85.0))
        val badges = useCase(entries, latestWeight = 85.0, targetKg = 85.0, heightCm = 0, streak = 0, bmi = null)
        assertTrue(find(badges, "weight_target")!!.earned)
    }

    @Test
    fun `weight_target is absent when no target is set`() {
        val entries = listOf(entry(1, 85.0))
        val badges = useCase(entries, latestWeight = 85.0, targetKg = 0.0, heightCm = 0, streak = 0, bmi = null)
        assertNull(find(badges, "weight_target"))
    }

    // -------------------------------------------------------------------------
    // Period loss badges (weekly / monthly / quarterly)
    // -------------------------------------------------------------------------

    @Test
    fun `weekly loss badge compares against the entry from at least 7 days ago`() {
        val entries = listOf(entry(10, 90.0), entry(1, 88.0))
        val badges = useCase(entries, latestWeight = 88.0, targetKg = 0.0, heightCm = 0, streak = 0, bmi = null)
        val weekly = find(badges, "weight_week_1kg")!!
        assertTrue(weekly.earned)
        assertEquals(2.0, weekly.currentValue!!, 0.0001)
    }

    @Test
    fun `monthly loss badge is absent when there is no entry old enough to compare against`() {
        // Oldest entry is only 10 days old — no reference point 30 days back
        val entries = listOf(entry(10, 90.0), entry(1, 88.0))
        val badges = useCase(entries, latestWeight = 88.0, targetKg = 0.0, heightCm = 0, streak = 0, bmi = null)
        assertNull(find(badges, "weight_month_2kg"))
    }

    // -------------------------------------------------------------------------
    // Streak badges
    // -------------------------------------------------------------------------

    @Test
    fun `streak badges compare against the weekly streak count`() {
        val entries = listOf(entry(1, 80.0))
        val badges = useCase(entries, latestWeight = 80.0, targetKg = 0.0, heightCm = 0, streak = 10, bmi = null)
        assertTrue(find(badges, "weight_streak3")!!.earned)
        assertTrue(find(badges, "weight_streak7")!!.earned)
        val streak30 = find(badges, "weight_streak30")!!
        assertFalse(streak30.earned)
        assertEquals(10.0, streak30.currentValue!!, 0.0001)
    }

    // -------------------------------------------------------------------------
    // Longevity badges
    // -------------------------------------------------------------------------

    @Test
    fun `6 month badge is earned once tracking spans at least 180 days`() {
        val entries = listOf(entry(200, 90.0), entry(1, 85.0))
        val badges = useCase(entries, latestWeight = 85.0, targetKg = 0.0, heightCm = 0, streak = 0, bmi = null)
        assertTrue(find(badges, "weight_6months")!!.earned)
        assertFalse(find(badges, "weight_1year")!!.earned)
    }

    // -------------------------------------------------------------------------
    // BMI badges
    // -------------------------------------------------------------------------

    @Test
    fun `exiting obesity badge compares current bmi against the historical max bmi`() {
        // height 175cm: 100kg -> bmi ~32.65 (obese), current bmi 28 (overweight, not obese)
        val entries = listOf(entry(90, 100.0), entry(1, 85.0))
        val badges = useCase(entries, latestWeight = 85.0, targetKg = 0.0, heightCm = 175, streak = 0, bmi = 28.0)
        assertTrue(find(badges, "weight_bmi_below30")!!.earned)
        assertNull(find(badges, "weight_bmi_normal")) // 28 is not < 25, and this badge has no progress value
    }

    @Test
    fun `normal bmi badge requires a known height`() {
        val entries = listOf(entry(1, 70.0))
        val badges = useCase(entries, latestWeight = 70.0, targetKg = 0.0, heightCm = 0, streak = 0, bmi = 22.0)
        assertNull(find(badges, "weight_bmi_normal")) // height unknown, badge has no progress value to keep it visible
    }

    // -------------------------------------------------------------------------
    // Stability badge
    // -------------------------------------------------------------------------

    @Test
    fun `stable weight badge requires at least 4 recent entries within a 1kg range`() {
        val entries = listOf(
            entry(90, 95.0), // old entry, outside the 30-day window
            entry(20, 80.0),
            entry(15, 80.3),
            entry(10, 80.6),
            entry(5, 80.2)
        )
        val badges = useCase(entries, latestWeight = 80.2, targetKg = 0.0, heightCm = 0, streak = 0, bmi = null)
        assertTrue(find(badges, "weight_stable")!!.earned)
    }

    @Test
    fun `stable weight badge is absent when recent weight swings beyond 1kg`() {
        val entries = listOf(
            entry(20, 80.0),
            entry(15, 82.0),
            entry(10, 79.0),
            entry(5, 81.0)
        )
        val badges = useCase(entries, latestWeight = 81.0, targetKg = 0.0, heightCm = 0, streak = 0, bmi = null)
        assertNull(find(badges, "weight_stable"))
    }
}
