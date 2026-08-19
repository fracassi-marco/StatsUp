package com.statsup.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsUpExportFormatTest {

    private fun sampleTraining(id: String = "i123", name: String = "Morning ride\twith\ntabs") = Training(
        id = id,
        resourceState = 2,
        name = name,
        distance = 12345.6,
        movingTime = 3600,
        elapsedTime = 3700,
        totalElevationGain = 250.5,
        type = "Ride",
        sportType = "MountainBikeRide",
        workoutType = null,
        startDate = "2024-05-01T10:00:00Z",
        startDateLocal = "2024-05-01T12:00:00Z",
        timezone = "(GMT+02:00) Europe/Rome",
        utcOffset = 7200.0,
        locationCity = null,
        locationState = null,
        locationCountry = "Italy",
        achievementCount = 3,
        kudosCount = 10,
        commentCount = 1,
        athleteCount = 1,
        photoCount = 0,
        map = Route(id = "a1", summaryPolyline = "abc\\def", resourceState = 2),
        trainer = false,
        commute = false,
        manual = false,
        private = false,
        visibility = "everyone",
        flagged = false,
        gearId = "b123",
        averageSpeed = 5.5,
        maxSpeed = 12.3,
        averageCadence = 80.0,
        averageWatts = 150.0,
        maxWatts = 400,
        weightedAverageWatts = 160,
        kilojoules = 540.0,
        deviceWatts = true,
        hasHeartrate = true,
        averageHeartrate = 140.0,
        maxHeartrate = 175.0,
        heartrateOptOut = false,
        displayHideHeartrateOption = false,
        elevHigh = 500.0,
        elevLow = 100.0,
        uploadId = 999L,
        uploadIdStr = "999",
        externalId = "ext-1",
        fromAcceptedTag = false,
        prCount = 0,
        totalPhotoCount = 0,
        hasKudoed = false,
        sufferScore = 42.0,
        lapsJson = "[{\"split\":1}]",
        centerLat = 45.1,
        centerLng = 9.2,
        source = "strava",
        middleware = null,
        middlewareId = null,
        sourceId = null,
        deviceName = "Garmin Edge",
        calories = 800,
        hrZoneTimes = listOf(100, 200, 300),
        hrZones = null,
        elevationPointsJson = null,
        startLocationLabel = "Milan",
        endLocationLabel = null,
        peakName = "Monte Rosa",
        peakElevation = 4634.0
    )

    @Test
    fun `roundtrip preserves a full training record`() {
        val original = sampleTraining()
        val data = ExportData(
            exportDate = 1234L,
            trainings = listOf(original),
            bookmarkedTrainings = emptyList(),
            athlete = null,
            settings = ExportSettings(theme = 1, monthlyGoal = 200, monthlyTrainingGoal = 12)
        )

        val text = StatsUpExportFormat.serialize(data)
        val parsed = StatsUpExportFormat.parse(text)

        assertEquals(1, parsed.trainings.size)
        val roundTripped = parsed.trainings[0]
        // Full data-class equality is the real guardrail against serialize()/parse() column
        // order drifting apart (see class-level KDoc): any field written but not read (or vice
        // versa) shifts every subsequent column and this assertion catches it immediately.
        assertEquals(original, roundTripped)
        assertEquals(original.id, roundTripped.id)
        assertEquals(original.name, roundTripped.name)
        assertEquals(original.distance, roundTripped.distance, 0.0001)
        assertEquals(original.map?.summaryPolyline, roundTripped.map?.summaryPolyline)
        assertEquals(original.hrZoneTimes, roundTripped.hrZoneTimes)
        assertNull(roundTripped.hrZones)
        assertEquals(original.locationCity, roundTripped.locationCity)
        assertNull(roundTripped.locationCity)
        assertEquals(original.peakName, roundTripped.peakName)
        assertEquals(original.peakElevation, roundTripped.peakElevation)
    }

    @Test
    fun `roundtrip preserves settings, athlete, weight and bookmarks`() {
        val data = ExportData(
            exportDate = 42L,
            trainings = emptyList(),
            bookmarkedTrainings = listOf(
                BookmarkedTraining(
                    id = 1,
                    trainingId = "i1",
                    note = "steep climb\nwatch out",
                    customTitle = "My favorite",
                    difficulty = "hard",
                    bookmarkedAt = 555L
                )
            ),
            athlete = Athlete(id = 7, username = "marco", resourceState = 2, profileMedium = null, profile = "http://x"),
            settings = ExportSettings(
                theme = 2,
                monthlyGoal = 300,
                monthlyTrainingGoal = 20,
                autoTargets = true,
                remindersEnabled = false,
                heightCm = 180,
                weightTargetKg = 72.5
            ),
            weightEntries = listOf(WeightEntry(id = 1, date = 1000L, weightKg = 73.2))
        )

        val parsed = StatsUpExportFormat.parse(StatsUpExportFormat.serialize(data))

        assertEquals(data.settings, parsed.settings)
        assertEquals(data.athlete, parsed.athlete)
        assertEquals(data.weightEntries, parsed.weightEntries)
        assertEquals(data.bookmarkedTrainings, parsed.bookmarkedTrainings)
        assertEquals(42L, parsed.exportDate)
    }

    @Test
    fun `empty database produces empty sections that parse back`() {
        val data = ExportData(
            trainings = emptyList(),
            bookmarkedTrainings = emptyList(),
            athlete = null,
            settings = ExportSettings(theme = 0, monthlyGoal = 100, monthlyTrainingGoal = 8)
        )

        val parsed = StatsUpExportFormat.parse(StatsUpExportFormat.serialize(data))

        assertTrue(parsed.trainings.isEmpty())
        assertTrue(parsed.bookmarkedTrainings.isEmpty())
        assertTrue(parsed.weightEntries.isEmpty())
        assertNull(parsed.athlete)
    }

    @Test(expected = StatsUpExportFormat.ParseException::class)
    fun `rejects a file without the magic header`() {
        StatsUpExportFormat.parse("not a statsup export\nrandom content")
    }

    @Test
    fun `output is plain compact text, not JSON`() {
        val data = ExportData(
            trainings = listOf(sampleTraining()),
            bookmarkedTrainings = emptyList(),
            athlete = null,
            settings = ExportSettings(theme = 0, monthlyGoal = 100, monthlyTrainingGoal = 8)
        )
        val text = StatsUpExportFormat.serialize(data)
        assertTrue(text.startsWith(StatsUpExportFormat.MAGIC))
        assertTrue(!text.trimStart().startsWith("{"))
        assertTrue(!text.trimStart().startsWith("["))
    }
}
