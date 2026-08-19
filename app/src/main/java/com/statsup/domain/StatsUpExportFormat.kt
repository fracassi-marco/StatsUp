package com.statsup.domain

/**
 * Compact, plain-text serialization of the whole app's data ([ExportData]), used for the
 * export/import file. Deliberately not JSON: JSON would repeat every field name on every
 * training row, which dominates file size once there are hundreds of trainings. Instead this
 * is a small tab-separated format, one row per record, with a single header describing the
 * column order for that section — closer to a CSV dump than a document format.
 *
 * Layout:
 * ```
 * STATSUP-EXPORT	<version>	<exportDate epoch millis>
 * #SETTINGS	1
 * <field...>
 * #ATHLETE	<0 or 1>
 * <field...>?
 * #WEIGHT	<n>
 * <field...>
 * ...
 * #BOOKMARKS	<n>
 * ...
 * #TRAININGS	<n>
 * ...
 * ```
 *
 * Every column is separated by a literal tab; every row by a newline. Free-text fields are
 * escaped so they can never introduce a stray tab/newline ([encodeText]/[decodeText]); a
 * dedicated sentinel distinguishes `null` strings from empty ones.
 */
object StatsUpExportFormat {

    const val MAGIC = "STATSUP-EXPORT"
    const val VERSION = 2

    private const val NULL_MARKER = "\u0000"
    private const val SEP = "\t"

    // ----- generic field encoding -----------------------------------------------------------

    private fun encodeText(value: String?): String {
        if (value == null) return NULL_MARKER
        val sb = StringBuilder(value.length)
        for (c in value) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '\t' -> sb.append("\\t")
                '\n' -> sb.append("\\n")
                '\r' -> Unit // dropped, never meaningful on its own
                // A literal NUL is otherwise indistinguishable from NULL_MARKER and would be
                // read back as `null` instead of a NUL character; escape it like the other
                // control characters above.
                '\u0000' -> sb.append("\\0")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private fun decodeText(raw: String): String? {
        if (raw == NULL_MARKER) return null
        val sb = StringBuilder(raw.length)
        var i = 0
        while (i < raw.length) {
            val c = raw[i]
            if (c == '\\' && i + 1 < raw.length) {
                when (raw[i + 1]) {
                    '\\' -> {
                        sb.append('\\'); i += 2
                    }

                    't' -> {
                        sb.append('\t'); i += 2
                    }

                    'n' -> {
                        sb.append('\n'); i += 2
                    }

                    '0' -> {
                        sb.append('\u0000'); i += 2
                    }

                    else -> {
                        sb.append(c); i += 1
                    }
                }
            } else {
                sb.append(c); i += 1
            }
        }
        return sb.toString()
    }

    private fun encodeBool(value: Boolean?): String = when (value) {
        null -> ""
        true -> "1"
        false -> "0"
    }

    private fun decodeBool(raw: String): Boolean? = when (raw) {
        "1" -> true
        "0" -> false
        else -> null
    }

    private fun encodeNum(value: Number?): String = value?.toString() ?: ""

    private fun decodeInt(raw: String): Int? = raw.ifEmpty { null }?.toIntOrNull()
    private fun decodeLong(raw: String): Long? = raw.ifEmpty { null }?.toLongOrNull()
    private fun decodeDouble(raw: String): Double? = raw.ifEmpty { null }?.toDoubleOrNull()

    private fun encodeIntList(value: List<Int>?): String =
        if (value == null) NULL_MARKER else value.joinToString(",")

    private fun decodeIntList(raw: String): List<Int>? {
        if (raw == NULL_MARKER) return null
        if (raw.isEmpty()) return emptyList()
        return raw.split(",").mapNotNull { it.toIntOrNull() }
    }

    private class RowWriter {
        private val fields = ArrayList<String>()
        fun text(v: String?): RowWriter {
            fields.add(encodeText(v)); return this
        }

        fun num(v: Number?): RowWriter {
            fields.add(encodeNum(v)); return this
        }

        fun bool(v: Boolean?): RowWriter {
            fields.add(encodeBool(v)); return this
        }

        fun intList(v: List<Int>?): RowWriter {
            fields.add(encodeIntList(v)); return this
        }

        fun build(): String = fields.joinToString(SEP)
    }

    private class RowReader(row: String) {
        private val fields = row.split(SEP)
        private var i = 0
        private fun next(): String = if (i < fields.size) fields[i++] else ""
        fun text(): String? = decodeText(next())
        fun textNonNull(): String = decodeText(next()) ?: ""
        fun int(): Int? = decodeInt(next())
        fun intNonNull(): Int = decodeInt(next()) ?: 0
        fun long(): Long? = decodeLong(next())
        fun longNonNull(): Long = decodeLong(next()) ?: 0L
        fun double(): Double? = decodeDouble(next())
        fun doubleNonNull(): Double = decodeDouble(next()) ?: 0.0
        fun bool(): Boolean? = decodeBool(next())
        fun boolNonNull(): Boolean = decodeBool(next()) ?: false
        fun intList(): List<Int>? = decodeIntList(next())
    }

    // ----- serialize -------------------------------------------------------------------------

    fun serialize(data: ExportData): String {
        val sb = StringBuilder()
        sb.append(MAGIC).append(SEP).append(VERSION).append(SEP).append(data.exportDate).append('\n')

        sb.append("#SETTINGS").append(SEP).append(1).append('\n')
        sb.append(
            RowWriter()
                .num(data.settings.theme)
                .num(data.settings.monthlyGoal)
                .num(data.settings.monthlyTrainingGoal)
                .bool(data.settings.autoTargets)
                .bool(data.settings.remindersEnabled)
                .num(data.settings.heightCm)
                .num(data.settings.weightTargetKg)
                .build()
        ).append('\n')

        val athlete = data.athlete
        sb.append("#ATHLETE").append(SEP).append(if (athlete != null) 1 else 0).append('\n')
        if (athlete != null) {
            sb.append(
                RowWriter()
                    .num(athlete.id)
                    .text(athlete.username)
                    .num(athlete.resourceState)
                    .text(athlete.profileMedium)
                    .text(athlete.profile)
                    .build()
            ).append('\n')
        }

        sb.append("#WEIGHT").append(SEP).append(data.weightEntries.size).append('\n')
        data.weightEntries.forEach { w ->
            sb.append(
                RowWriter()
                    .num(w.id)
                    .num(w.date)
                    .num(w.weightKg)
                    .build()
            ).append('\n')
        }

        sb.append("#BOOKMARKS").append(SEP).append(data.bookmarkedTrainings.size).append('\n')
        data.bookmarkedTrainings.forEach { b ->
            sb.append(
                RowWriter()
                    .num(b.id)
                    .text(b.trainingId)
                    .text(b.note)
                    .text(b.customTitle)
                    .text(b.difficulty)
                    .num(b.bookmarkedAt)
                    .build()
            ).append('\n')
        }

        sb.append("#TRAININGS").append(SEP).append(data.trainings.size).append('\n')
        data.trainings.forEach { t ->
            sb.append(
                RowWriter()
                    .text(t.id)
                    .num(t.resourceState)
                    .text(t.name)
                    .num(t.distance)
                    .num(t.movingTime)
                    .num(t.elapsedTime)
                    .num(t.totalElevationGain)
                    .text(t.type)
                    .text(t.sportType)
                    .text(t.workoutType)
                    .text(t.startDate)
                    .text(t.startDateLocal)
                    .text(t.timezone)
                    .num(t.utcOffset)
                    .text(t.locationCity)
                    .text(t.locationState)
                    .text(t.locationCountry)
                    .num(t.achievementCount)
                    .num(t.kudosCount)
                    .num(t.commentCount)
                    .num(t.athleteCount)
                    .num(t.photoCount)
                    .text(t.map?.id)
                    .text(t.map?.summaryPolyline)
                    .num(t.map?.resourceState)
                    .bool(t.trainer)
                    .bool(t.commute)
                    .bool(t.manual)
                    .bool(t.private)
                    .text(t.visibility)
                    .bool(t.flagged)
                    .text(t.gearId)
                    .num(t.averageSpeed)
                    .num(t.maxSpeed)
                    .num(t.averageCadence)
                    .num(t.averageWatts)
                    .num(t.maxWatts)
                    .num(t.weightedAverageWatts)
                    .num(t.kilojoules)
                    .bool(t.deviceWatts)
                    .bool(t.hasHeartrate)
                    .num(t.averageHeartrate)
                    .num(t.maxHeartrate)
                    .bool(t.heartrateOptOut)
                    .bool(t.displayHideHeartrateOption)
                    .num(t.elevHigh)
                    .num(t.elevLow)
                    .num(t.uploadId)
                    .text(t.uploadIdStr)
                    .text(t.externalId)
                    .bool(t.fromAcceptedTag)
                    .num(t.prCount)
                    .num(t.totalPhotoCount)
                    .bool(t.hasKudoed)
                    .num(t.sufferScore)
                    .text(t.lapsJson)
                    .num(t.centerLat)
                    .num(t.centerLng)
                    .text(t.source)
                    .text(t.middleware)
                    .text(t.middlewareId)
                    .text(t.sourceId)
                    .text(t.deviceName)
                    .num(t.calories)
                    .intList(t.hrZoneTimes)
                    .intList(t.hrZones)
                    .text(t.elevationPointsJson)
                    .text(t.startLocationLabel)
                    .text(t.endLocationLabel)
                    .text(t.peakName)
                    .num(t.peakElevation)
                    .build()
            ).append('\n')
        }

        return sb.toString()
    }

    // ----- parse -----------------------------------------------------------------------------

    class ParseException(message: String) : Exception(message)

    fun parse(text: String): ExportData {
        // Note: String.split always returns at least one element (even for an empty string),
        // so `lines` can never be empty here; the magic-header check below is what actually
        // rejects malformed/empty input.
        val lines = text.split('\n')

        val header = lines[0].split(SEP)
        if (header.isEmpty() || header[0] != MAGIC) {
            throw ParseException("Not a StatsUp export file")
        }
        val fileVersion = header.getOrNull(1)?.toIntOrNull()
            ?: throw ParseException("Missing format version")
        if (fileVersion > VERSION) {
            throw ParseException(
                "Unsupported export format version $fileVersion (this app supports up to $VERSION); " +
                    "please update the app before importing this file"
            )
        }
        val exportDate = header.getOrNull(2)?.toLongOrNull() ?: System.currentTimeMillis()

        var idx = 1
        fun nextNonEmpty(): String {
            while (idx < lines.size && lines[idx].isEmpty()) idx++
            if (idx >= lines.size) throw ParseException("Unexpected end of file")
            return lines[idx++]
        }

        fun expectSection(name: String): Int {
            val parts = nextNonEmpty().split(SEP)
            if (parts.getOrNull(0) != name) {
                throw ParseException("Expected section $name, found ${parts.getOrNull(0)}")
            }
            return parts.getOrNull(1)?.toIntOrNull() ?: 0
        }

        expectSection("#SETTINGS")
        val settingsRow = RowReader(nextNonEmpty())
        val settings = ExportSettings(
            theme = settingsRow.intNonNull(),
            monthlyGoal = settingsRow.intNonNull(),
            monthlyTrainingGoal = settingsRow.intNonNull(),
            autoTargets = settingsRow.boolNonNull(),
            remindersEnabled = settingsRow.boolNonNull(),
            heightCm = settingsRow.intNonNull(),
            weightTargetKg = settingsRow.doubleNonNull()
        )

        val athleteCount = expectSection("#ATHLETE")
        val athlete: Athlete? = if (athleteCount > 0) {
            val row = RowReader(nextNonEmpty())
            Athlete(
                id = row.longNonNull(),
                username = row.textNonNull(),
                resourceState = row.int(),
                profileMedium = row.text(),
                profile = row.text()
            )
        } else null

        val weightCount = expectSection("#WEIGHT")
        val weightEntries = ArrayList<WeightEntry>(weightCount)
        repeat(weightCount) {
            val row = RowReader(nextNonEmpty())
            weightEntries.add(
                WeightEntry(
                    id = row.longNonNull(),
                    date = row.longNonNull(),
                    weightKg = row.doubleNonNull()
                )
            )
        }

        val bookmarkCount = expectSection("#BOOKMARKS")
        val bookmarks = ArrayList<BookmarkedTraining>(bookmarkCount)
        repeat(bookmarkCount) {
            val row = RowReader(nextNonEmpty())
            bookmarks.add(
                BookmarkedTraining(
                    id = row.longNonNull(),
                    trainingId = row.textNonNull(),
                    note = row.textNonNull(),
                    customTitle = row.textNonNull(),
                    difficulty = row.textNonNull(),
                    bookmarkedAt = row.longNonNull()
                )
            )
        }

        val trainingCount = expectSection("#TRAININGS")
        val trainings = ArrayList<Training>(trainingCount)
        repeat(trainingCount) {
            val row = RowReader(nextNonEmpty())
            // Fields must be read in exactly the order they were written in serialize() —
            // Kotlin evaluates constructor arguments in call-site order, which does not
            // necessarily match declaration order, so each column is read into a local val
            // first and the object is only built once every column has been consumed.
            val id = row.textNonNull()
            val resourceState = row.int()
            val name = row.textNonNull()
            val distance = row.doubleNonNull()
            val movingTime = row.intNonNull()
            val elapsedTime = row.intNonNull()
            val totalElevationGain = row.doubleNonNull()
            val type = row.text()
            val sportType = row.text()
            val workoutType = row.text()
            val startDate = row.textNonNull()
            val startDateLocal = row.text()
            val timezone = row.text()
            val utcOffset = row.double()
            val locationCity = row.text()
            val locationState = row.text()
            val locationCountry = row.text()
            val achievementCount = row.int()
            val kudosCount = row.int()
            val commentCount = row.int()
            val athleteCount = row.int()
            val photoCount = row.int()
            val mapId = row.text()
            val mapPolyline = row.text()
            val mapResourceState = row.int()
            val trainer = row.bool()
            val commute = row.bool()
            val manual = row.bool()
            val privateFlag = row.bool()
            val visibility = row.text()
            val flagged = row.bool()
            val gearId = row.text()
            val averageSpeed = row.double()
            val maxSpeed = row.doubleNonNull()
            val averageCadence = row.doubleNonNull()
            val averageWatts = row.doubleNonNull()
            val maxWatts = row.int()
            val weightedAverageWatts = row.intNonNull()
            val kilojoules = row.doubleNonNull()
            val deviceWatts = row.boolNonNull()
            val hasHeartrate = row.bool()
            val averageHeartrate = row.double()
            val maxHeartrate = row.doubleNonNull()
            val heartrateOptOut = row.bool()
            val displayHideHeartrateOption = row.bool()
            val elevHigh = row.doubleNonNull()
            val elevLow = row.doubleNonNull()
            val uploadId = row.longNonNull()
            val uploadIdStr = row.text()
            val externalId = row.text()
            val fromAcceptedTag = row.bool()
            val prCount = row.int()
            val totalPhotoCount = row.int()
            val hasKudoed = row.bool()
            val sufferScore = row.double()
            val lapsJson = row.text()
            val centerLat = row.double()
            val centerLng = row.double()
            val source = row.text()
            val middleware = row.text()
            val middlewareId = row.text()
            val sourceId = row.text()
            val deviceName = row.text()
            val calories = row.int()
            val hrZoneTimes = row.intList()
            val hrZones = row.intList()
            val elevationPointsJson = row.text()
            val startLocationLabel = row.text()
            val endLocationLabel = row.text()
            val peakName = row.text()
            val peakElevation = row.double()

            trainings.add(
                Training(
                    id = id,
                    resourceState = resourceState,
                    name = name,
                    distance = distance,
                    movingTime = movingTime,
                    elapsedTime = elapsedTime,
                    totalElevationGain = totalElevationGain,
                    type = type,
                    sportType = sportType,
                    workoutType = workoutType,
                    startDate = startDate,
                    startDateLocal = startDateLocal,
                    timezone = timezone,
                    utcOffset = utcOffset,
                    locationCity = locationCity,
                    locationState = locationState,
                    locationCountry = locationCountry,
                    achievementCount = achievementCount,
                    kudosCount = kudosCount,
                    commentCount = commentCount,
                    athleteCount = athleteCount,
                    photoCount = photoCount,
                    map = if (mapId == null && mapPolyline == null && mapResourceState == null) null
                    else Route(id = mapId, summaryPolyline = mapPolyline, resourceState = mapResourceState),
                    trainer = trainer,
                    commute = commute,
                    manual = manual,
                    private = privateFlag,
                    visibility = visibility,
                    flagged = flagged,
                    gearId = gearId,
                    averageSpeed = averageSpeed,
                    maxSpeed = maxSpeed,
                    averageCadence = averageCadence,
                    averageWatts = averageWatts,
                    maxWatts = maxWatts,
                    weightedAverageWatts = weightedAverageWatts,
                    kilojoules = kilojoules,
                    deviceWatts = deviceWatts,
                    hasHeartrate = hasHeartrate,
                    averageHeartrate = averageHeartrate,
                    maxHeartrate = maxHeartrate,
                    heartrateOptOut = heartrateOptOut,
                    displayHideHeartrateOption = displayHideHeartrateOption,
                    elevHigh = elevHigh,
                    elevLow = elevLow,
                    uploadId = uploadId,
                    uploadIdStr = uploadIdStr,
                    externalId = externalId,
                    fromAcceptedTag = fromAcceptedTag,
                    prCount = prCount,
                    totalPhotoCount = totalPhotoCount,
                    hasKudoed = hasKudoed,
                    sufferScore = sufferScore,
                    lapsJson = lapsJson,
                    centerLat = centerLat,
                    centerLng = centerLng,
                    source = source,
                    middleware = middleware,
                    middlewareId = middlewareId,
                    sourceId = sourceId,
                    deviceName = deviceName,
                    calories = calories,
                    hrZoneTimes = hrZoneTimes,
                    hrZones = hrZones,
                    elevationPointsJson = elevationPointsJson,
                    startLocationLabel = startLocationLabel,
                    endLocationLabel = endLocationLabel,
                    peakName = peakName,
                    peakElevation = peakElevation
                )
            )
        }

        return ExportData(
            exportDate = exportDate,
            trainings = trainings,
            bookmarkedTrainings = bookmarks,
            athlete = athlete,
            settings = settings,
            weightEntries = weightEntries
        )
    }
}
