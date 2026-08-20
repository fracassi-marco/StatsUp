package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GradeBreakdownTest {

    @Test
    fun `returns null when fewer than two elevation points`() {
        assertNull(computeGradeBreakdown(listOf(100.0), totalDistanceMeters = 1000.0))
        assertNull(computeGradeBreakdown(emptyList(), totalDistanceMeters = 1000.0))
    }

    @Test
    fun `returns null when total distance is zero or negative`() {
        assertNull(computeGradeBreakdown(listOf(100.0, 110.0), totalDistanceMeters = 0.0))
        assertNull(computeGradeBreakdown(listOf(100.0, 110.0), totalDistanceMeters = -5.0))
    }

    @Test
    fun `all uphill segments are reported as 100 percent uphill`() {
        val points = listOf(100.0, 110.0, 120.0, 130.0)
        val result = computeGradeBreakdown(points, totalDistanceMeters = 3000.0)

        assertEquals(100.0, result!!.uphillPercent, 0.001)
        assertEquals(0.0, result.downhillPercent, 0.001)
        assertEquals(0.0, result.flatPercent, 0.001)
    }

    @Test
    fun `all downhill segments are reported as 100 percent downhill`() {
        val points = listOf(130.0, 120.0, 110.0, 100.0)
        val result = computeGradeBreakdown(points, totalDistanceMeters = 3000.0)

        assertEquals(0.0, result!!.uphillPercent, 0.001)
        assertEquals(100.0, result.downhillPercent, 0.001)
        assertEquals(0.0, result.flatPercent, 0.001)
    }

    @Test
    fun `flat segments below the noise threshold are not counted as climbing or descending`() {
        val points = listOf(100.0, 100.2, 99.9, 100.1)
        val result = computeGradeBreakdown(points, totalDistanceMeters = 3000.0, noiseThresholdMeters = 0.5)

        assertEquals(0.0, result!!.uphillPercent, 0.001)
        assertEquals(0.0, result.downhillPercent, 0.001)
        assertEquals(100.0, result.flatPercent, 0.001)
    }

    @Test
    fun `gradual real climb sampled densely is not flattened out by sample-to-sample noise`() {
        // Simulates a ~1500m climb over a 10km hike, sampled once per "second" (3000 raw
        // points) with +-0.3m of sensor jitter on top of the real trend — well above what a
        // naive adjacent-sample comparison against a 1m threshold could ever detect, but a
        // very real and substantial climb over the whole distance.
        val totalDistance = 10_000.0
        val totalClimb = 1500.0
        val sampleCount = 3000
        val jitter = doubleArrayOf(0.3, -0.2, 0.1, -0.3, 0.2)
        val points = (0 until sampleCount).map { i ->
            val trend = totalClimb * i / (sampleCount - 1)
            trend + jitter[i % jitter.size]
        }

        val result = computeGradeBreakdown(points, totalDistanceMeters = totalDistance)

        assertEquals(100.0, result!!.uphillPercent, 1.0)
        assertEquals(0.0, result.downhillPercent, 1.0)
    }

    @Test
    fun `mixed profile splits distance proportionally across the three segments`() {
        // 4 equal segments over 4000m (1000m each): up, down, flat, up
        val points = listOf(100.0, 110.0, 100.0, 100.0, 110.0)
        val result = computeGradeBreakdown(points, totalDistanceMeters = 4000.0)

        assertEquals(50.0, result!!.uphillPercent, 0.001)
        assertEquals(25.0, result.downhillPercent, 0.001)
        assertEquals(25.0, result.flatPercent, 0.001)
    }
}
