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
        // 20m of climb per 1000m segment is a 2% grade, clearly above the flat threshold.
        val points = listOf(100.0, 120.0, 140.0, 160.0)
        val result = computeGradeBreakdown(points, totalDistanceMeters = 3000.0)

        assertEquals(100.0, result!!.uphillPercent, 0.001)
        assertEquals(0.0, result.downhillPercent, 0.001)
        assertEquals(0.0, result.flatPercent, 0.001)
    }

    @Test
    fun `all downhill segments are reported as 100 percent downhill`() {
        val points = listOf(160.0, 140.0, 120.0, 100.0)
        val result = computeGradeBreakdown(points, totalDistanceMeters = 3000.0)


        assertEquals(0.0, result!!.uphillPercent, 0.001)
        assertEquals(100.0, result.downhillPercent, 0.001)
        assertEquals(0.0, result.flatPercent, 0.001)
    }

    @Test
    fun `flat segments below the grade threshold are not counted as climbing or descending`() {
        val points = listOf(100.0, 100.2, 99.9, 100.1)
        val result = computeGradeBreakdown(points, totalDistanceMeters = 3000.0, flatGradeThresholdPercent = 0.5)

        assertEquals(0.0, result!!.uphillPercent, 0.001)
        assertEquals(0.0, result.downhillPercent, 0.001)
        assertEquals(100.0, result.flatPercent, 0.001)
    }

    @Test
    fun `a long, coarsely segmented flat training is not underestimated as sloped`() {
        // A 50km flat training downsampled into 100 segments of 500m each. A couple of meters
        // of elevation noise/drift over a single 500m segment is a grade well under 1%, so with
        // an absolute-elevation threshold it used to be wrongly counted as uphill/downhill,
        // underestimating the flat percentage. Using grade instead fixes this.
        val noise = doubleArrayOf(0.0, 2.0, -1.5, 1.0, -2.0)
        val points = (0..100).map { i -> 100.0 + noise[i % noise.size] }
        val result = computeGradeBreakdown(points, totalDistanceMeters = 50_000.0)

        assertEquals(100.0, result!!.flatPercent, 0.001)
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
        // 4 equal segments over 4000m (1000m each): up, down, flat, up. Each sloped segment
        // climbs/descends 20m over 1000m, a 2% grade, clearly above the flat threshold.
        val points = listOf(100.0, 120.0, 100.0, 100.0, 120.0)
        val result = computeGradeBreakdown(points, totalDistanceMeters = 4000.0)

        assertEquals(50.0, result!!.uphillPercent, 0.001)
        assertEquals(25.0, result.downhillPercent, 0.001)
        assertEquals(25.0, result.flatPercent, 0.001)
    }
}
