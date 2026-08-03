package com.statsup.domain

import com.google.maps.android.PolyUtil
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PeakEstimationTest {

    private fun tripOf(points: List<LatLng>) = Trip(PolyUtil.encode(points))

    // PolyUtil round-trips through a fixed-precision encoding, so compare with a small tolerance.
    private fun assertLatLngEquals(expected: LatLng, actual: LatLng?) {
        requireNotNull(actual)
        assertEquals(expected.latitude, actual.latitude, 1e-4)
        assertEquals(expected.longitude, actual.longitude, 1e-4)
    }

    @Test
    fun `maps the fractional position of the elevation peak onto the route`() {
        val points = listOf(
            LatLng(45.0, 7.0),
            LatLng(45.5, 7.5),
            LatLng(46.0, 8.0),
            LatLng(46.5, 8.5)
        )
        // 4 altitude samples, max at index 2 (fraction 2/3) -> point index round(2/3 * 3) = 2
        val altitudes = listOf(1000.0, 2000.0, 3000.0, 1500.0)

        val result = estimateSummitLatLng(tripOf(points), altitudes)

        assertLatLngEquals(points[2], result)
    }

    @Test
    fun `returns null when the trip is null`() {
        assertNull(estimateSummitLatLng(null, listOf(1.0, 2.0)))
    }

    @Test
    fun `returns null when the altitude list is empty`() {
        assertNull(estimateSummitLatLng(tripOf(listOf(LatLng(45.0, 7.0))), emptyList()))
    }

    @Test
    fun `returns null when the route has no points`() {
        assertNull(estimateSummitLatLng(Trip(""), listOf(1.0, 2.0)))
    }

    @Test
    fun `handles a single altitude sample without dividing by zero`() {
        val points = listOf(LatLng(45.0, 7.0), LatLng(46.0, 8.0))
        val result = estimateSummitLatLng(tripOf(points), listOf(2500.0))
        assertLatLngEquals(points[0], result)
    }

    @Test
    fun `clamps the estimated index within route bounds`() {
        val points = listOf(LatLng(45.0, 7.0), LatLng(46.0, 8.0))
        // peak is the very last altitude sample -> should map to the last route point
        val altitudes = listOf(1000.0, 1200.0, 2500.0)
        val result = estimateSummitLatLng(tripOf(points), altitudes)
        assertLatLngEquals(points.last(), result)
    }
}
