package com.statsup.infrastructure.repository

import com.statsup.domain.repository.PeakLookupException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class GeoNamesPeakRepositoryTest {

    private val sampleResponse = """
        {
          "geonames": [
            {
              "name": "Monte Rosa",
              "fcode": "PK",
              "lat": "45.9763",
              "lng": "7.6586",
              "elevation": "4634"
            },
            {
              "name": "Some Pass",
              "fcode": "PASS",
              "lat": "45.9",
              "lng": "7.7"
            },
            {
              "name": "Nearby Mountain",
              "fcode": "MT",
              "lat": "45.8",
              "lng": "7.5"
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `parseGeoNamesPeaks keeps only PK and MT feature codes`() {
        val peaks = parseGeoNamesPeaks(sampleResponse)
        assertEquals(2, peaks.size)
        assertTrue(peaks.none { it.name == "Some Pass" })
    }

    @Test
    fun `parseGeoNamesPeaks extracts name, lat, lng and elevation`() {
        val peaks = parseGeoNamesPeaks(sampleResponse)
        val monteRosa = peaks.first { it.name == "Monte Rosa" }
        assertEquals(45.9763, monteRosa.latLng.latitude, 0.0001)
        assertEquals(7.6586, monteRosa.latLng.longitude, 0.0001)
        assertEquals(4634.0, monteRosa.elevation!!, 0.0)
    }

    @Test
    fun `parseGeoNamesPeaks tolerates a missing elevation`() {
        val peaks = parseGeoNamesPeaks(sampleResponse)
        val mountain = peaks.first { it.name == "Nearby Mountain" }
        assertTrue(mountain.elevation == null)
    }

    @Test
    fun `parseGeoNamesPeaks returns empty list when there are no geonames`() {
        val peaks = parseGeoNamesPeaks("""{"geonames": []}""")
        assertTrue(peaks.isEmpty())
    }

    @Test
    fun `parseGeoNamesPeaks throws PeakLookupException on an application-level error`() {
        try {
            parseGeoNamesPeaks("""{"status": {"message": "the daily limit has been exceeded", "value": 18}}""")
            fail("expected PeakLookupException")
        } catch (e: PeakLookupException) {
            assertTrue(e.message!!.contains("daily limit"))
        }
    }
}
