package com.statsup.infrastructure.repository

import com.google.android.gms.maps.model.LatLng
import com.statsup.domain.repository.Peak
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeoDistanceTest {

    private val summit = LatLng(45.9, 7.6)

    @Test
    fun `choosePeak prefers a farther candidate whose elevation matches the hint`() {
        val closeButWrongElevation = Peak(
            name = "False Summit",
            latLng = LatLng(45.9005, 7.6005), // ~65m away
            elevation = 2500.0
        )
        val fartherButRightElevation = Peak(
            name = "True Summit",
            latLng = LatLng(45.903, 7.603), // ~330m away
            elevation = 3980.0
        )
        val chosen = choosePeak(listOf(closeButWrongElevation, fartherButRightElevation), summit, elevationHint = 3985.0)
        assertEquals("True Summit", chosen?.name)
    }

    @Test
    fun `choosePeak falls back to distance when elevations are unknown`() {
        val near = Peak(name = "Near", latLng = LatLng(45.9005, 7.6005), elevation = null)
        val far = Peak(name = "Far", latLng = LatLng(45.95, 7.65), elevation = null)
        val chosen = choosePeak(listOf(near, far), summit, elevationHint = 3000.0)
        assertEquals("Near", chosen?.name)
    }

    @Test
    fun `choosePeak returns null for an empty candidate list`() {
        assertNull(choosePeak(emptyList(), summit, elevationHint = 3000.0))
    }
}
