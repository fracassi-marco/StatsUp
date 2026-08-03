package com.statsup.infrastructure.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverpassPeakRepositoryTest {

    private val sampleResponse = """
        {
          "version": 0.6,
          "elements": [
            {
              "type": "node",
              "id": 1,
              "lat": 45.9763,
              "lon": 7.6586,
              "tags": { "natural": "peak", "name": "Monte Rosa", "ele": "4634" }
            },
            {
              "type": "node",
              "id": 2,
              "lat": 45.9,
              "lon": 7.7,
              "tags": { "natural": "peak", "name": "Unnamed", "ele": "not-a-number" }
            },
            {
              "type": "node",
              "id": 3,
              "lat": 45.8,
              "lon": 7.5
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `parseOverpassPeaks extracts named peaks with lat, lon and elevation`() {
        val peaks = parseOverpassPeaks(sampleResponse)
        assertEquals(2, peaks.size)
        val monteRosa = peaks.first { it.name == "Monte Rosa" }
        assertEquals(45.9763, monteRosa.latLng.latitude, 0.0001)
        assertEquals(7.6586, monteRosa.latLng.longitude, 0.0001)
        assertEquals(4634.0, monteRosa.elevation!!, 0.0)
    }

    @Test
    fun `parseOverpassPeaks tolerates a missing or invalid ele tag`() {
        val peaks = parseOverpassPeaks(sampleResponse)
        val unnamed = peaks.first { it.name == "Unnamed" }
        assertTrue(unnamed.elevation == null)
    }

    @Test
    fun `parseOverpassPeaks skips nodes without tags`() {
        val peaks = parseOverpassPeaks(sampleResponse)
        assertTrue(peaks.none { it.latLng.latitude == 45.8 })
    }

    @Test
    fun `parseOverpassPeaks returns empty list when there are no elements`() {
        val peaks = parseOverpassPeaks("""{"version": 0.6, "elements": []}""")
        assertTrue(peaks.isEmpty())
    }
}
