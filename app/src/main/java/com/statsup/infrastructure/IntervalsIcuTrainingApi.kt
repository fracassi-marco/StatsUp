package com.statsup.infrastructure

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.statsup.BuildConfig
import com.statsup.domain.ApiException
import com.statsup.domain.Athlete
import com.statsup.domain.Lap
import com.statsup.domain.OAuthToken
import com.statsup.domain.Route
import com.statsup.domain.Training
import com.statsup.domain.TrainingApi
import com.statsup.domain.repository.SettingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import topinambur.Bearer
import topinambur.Http
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

class IntervalsIcuTrainingApi(private val settingRepository: SettingRepository) : TrainingApi {

    private val jsonMapper = jsonMapper { addModule(kotlinModule()) }.apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private fun checkStatus(statusCode: Int) {
        if (statusCode !in 200..299) throw ApiException(statusCode)
    }

    private fun athleteId(): String {
        val id = settingRepository.loadAthleteId()
        return id?.takeIf { it.isNotBlank() } ?: "0"
    }

    override suspend fun download(token: String, latest: Training?): List<Training> {
        val id = athleteId()
        val oldest = if (latest != null) {
            latest.startDateLocal?.take(10) ?: latest.date.toLocalDate().toString()
        } else {
            "2000-01-01"
        }
        val params = mutableMapOf(
            "oldest" to oldest,
            "newest" to LocalDate.now().toString()
        )
        val response = Http().get(
            url = "https://intervals.icu/api/v1/athlete/$id/activities",
            params = params,
            auth = Bearer(token),
            headers = mapOf("Accept" to "application/json")
        )
        checkStatus(response.statusCode)
        val listType = jsonMapper.typeFactory.constructCollectionType(List::class.java, ActivityDto::class.java)

        val dtos: List<ActivityDto> = jsonMapper.readValue(response.body, listType)
        val latestId = latest?.id
        return dtos
            .filter { it.id != latestId }
            .map { it.toTraining() }
    }

    override suspend fun athlete(token: String): Athlete {
        val id = athleteId()
        val response = Http().get(
            url = "https://intervals.icu/api/v1/athlete/$id/profile",
            auth = Bearer(token),
            headers = mapOf("Accept" to "application/json")
        )
        checkStatus(response.statusCode)
        val dto = jsonMapper.readValue(response.body, AthleteProfileResponseDto::class.java)
        return dto.athlete.toAthlete()
    }

    override suspend fun laps(token: String, activityId: String): List<Lap> {
        val response = Http().get(
            url = "https://intervals.icu/api/v1/activity/$activityId/intervals",
            auth = Bearer(token),
            headers = mapOf("Accept" to "application/json")
        )
        if (response.statusCode !in 200..299) return emptyList()
        return try {
            val dto = jsonMapper.readValue(response.body, IntervalsResponseDto::class.java)
            dto.icuIntervals.mapIndexedNotNull { index, interval -> interval.toLap(index + 1) }
        } catch (e: Exception) {
            android.util.Log.e("IntervalsIcu", "laps parse error", e)
            emptyList()
        }
    }

    override suspend fun fetchPolyline(token: String, activityId: String): String? {
        val response = Http().get(
            url = "https://intervals.icu/api/v1/activity/$activityId/streams",
            params = mapOf("types" to "latlng"),
            auth = Bearer(token),
            headers = mapOf("Accept" to "application/json")
        )
        if (response.statusCode !in 200..299) return null
        return try {
            val listType = jsonMapper.typeFactory.constructCollectionType(List::class.java, StreamDto::class.java)
            val streams: List<StreamDto> = jsonMapper.readValue(response.body, listType)
            val data = streams.firstOrNull { it.type == "latlng" }?.data
            if (data == null) return null
            // Format: [lat0, lng0, lat1, lng1, ...] (interleaved pairs)
            val points = (0 until data.size / 2).mapNotNull { i ->
                val lat = data.getOrNull(2 * i) ?: return@mapNotNull null
                val lng = data.getOrNull(2 * i + 1) ?: return@mapNotNull null
                LatLng(lat, lng)
            }
            if (points.isEmpty()) null else PolyUtil.encode(points)
        } catch (e: Exception) {
            android.util.Log.e("IntervalsIcu", "fetchPolyline $activityId parse error", e)
            null
        }
    }

    override suspend fun refreshToken(refreshToken: String): OAuthToken {
        val response = Http().post(
            url = "https://intervals.icu/api/oauth/token",
            body = mapOf(
                "client_id" to BuildConfig.INTERVALS_ICU_CLIENT_ID,
                "client_secret" to BuildConfig.INTERVALS_ICU_CLIENT_SECRET,
                "refresh_token" to refreshToken,
                "grant_type" to "refresh_token"
            ),
            headers = mapOf("Accept" to "application/json")
        )
        checkStatus(response.statusCode)
        val result = jsonMapper.readValue(response.body, TokenRefreshDto::class.java)
        return OAuthToken(
            accessToken = result.accessToken,
            refreshToken = result.refreshToken ?: refreshToken,
            expiresAt = result.expiresAt ?: (System.currentTimeMillis() / 1000 + (result.expiresIn ?: 3600)),
            athleteId = result.athleteId
        )
    }

    override suspend fun exchangeCode(code: String): OAuthToken {
        val params = mapOf(
            "client_id" to BuildConfig.INTERVALS_ICU_CLIENT_ID,
            "client_secret" to BuildConfig.INTERVALS_ICU_CLIENT_SECRET,
            "code" to code,
            "grant_type" to "authorization_code"
        )
        val body = params.entries.joinToString("&") {
            "${java.net.URLEncoder.encode(it.key, "UTF-8")}=${java.net.URLEncoder.encode(it.value, "UTF-8")}"
        }
        val url = java.net.URL("https://intervals.icu/api/oauth/token")
        val conn = (withContext(Dispatchers.IO) {
            url.openConnection()
        } as java.net.HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("Accept", "application/json")
            doOutput = true
            outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        }
        val statusCode = conn.responseCode
        val responseBody = try {
            if (statusCode in 200..299) conn.inputStream else conn.errorStream
        } catch (_: Exception) { null }?.bufferedReader()?.readText() ?: ""
        conn.disconnect()
        if (statusCode !in 200..299) throw ApiException(statusCode)
        val result = jsonMapper.readValue(responseBody, TokenExchangeDto::class.java)
        // Prefer the top-level athlete_id; fall back to the nested athlete object.
        // Ensure the ID always carries the "i" prefix expected by the REST API.
        val athleteId = (result.athleteId ?: result.athlete?.id)
            ?.let { id -> if (id.startsWith("i")) id else "i$id" }
        return OAuthToken(
            accessToken = result.accessToken,
            refreshToken = "",
            expiresAt = Long.MAX_VALUE / 1000L, // tokens don't expire per intervals.icu docs
            athleteId = athleteId
        )
    }

    // DTOs

    private data class ActivityDto(
        val id: String = "",
        val name: String = "",
        // Standard Strava-synced fields (may be 0 for native Intervals.icu activities)
        val distance: Double = 0.0,
        val movingTime: Int = 0,
        val elapsedTime: Int = 0,
        val totalElevationGain: Double = 0.0,
        val type: String? = null,
        val sportType: String? = null,
        val startDateLocal: String? = null,
        val timezone: String? = null,
        val averageSpeed: Double? = null,
        val maxSpeed: Double? = null,
        val averageCadence: Double? = null,
        val averageWatts: Double? = null,
        val maxWatts: Int? = null,
        val weightedAverageWatts: Int? = null,
        val kilojoules: Double? = null,
        val deviceWatts: Boolean? = null,
        val averageHeartrate: Double? = null,
        val maxHeartrate: Double? = null,
        // Strava-synced altitude fields (fallback for activities imported via Strava)
        val elevHigh: Double? = null,
        val elevLow: Double? = null,
        val polyline: String? = null,
        // Intervals.icu own calculated fields — preferred over the Strava-synced ones
        // because they match what the intervals.icu website displays.
        val icuDistance: Double? = null,
        val icuMovingTime: Int? = null,
        val icuElapsedTime: Int? = null,
        val icuTotalElevationGain: Double? = null,
        val icuAverageSpeed: Double? = null,
        val icuAverageHeartrate: Double? = null,
        val icuMaxHeartrate: Double? = null,
        // Native intervals.icu altitude fields (preferred — added June 2024)
        val maxAltitude: Double? = null,
        val minAltitude: Double? = null,
        val source: String? = null,
        val externalId: String? = null,
        val deviceName: String? = null,
        val calories: Int? = null,
        val icuHrZoneTimes: List<Int>? = null,
        val icuHrZones: List<Int>? = null
    ) {
        fun toTraining(): Training {
            val localDateStr = startDateLocal ?: ""
            val startDate = localDateStr.toZonedIso()
            // Prefer icu_* values (what intervals.icu website shows); fall back to
            // Strava-synced standard fields for backwards compatibility.
            val resolvedDistance = icuDistance?.takeIf { it > 0 } ?: distance
            val resolvedMovingTime = icuMovingTime?.takeIf { it > 0 } ?: movingTime
            val resolvedElapsedTime = icuElapsedTime?.takeIf { it > 0 } ?: elapsedTime
            val resolvedElevationGain = icuTotalElevationGain?.takeIf { it > 0 } ?: totalElevationGain
            val resolvedAverageSpeed = icuAverageSpeed?.takeIf { it > 0 } ?: averageSpeed
            val resolvedAverageHr = icuAverageHeartrate?.takeIf { it > 0 } ?: averageHeartrate
            val resolvedMaxHr = icuMaxHeartrate?.takeIf { it > 0 } ?: maxHeartrate
            val resolvedElevHigh = maxAltitude?.takeIf { it > 0 } ?: elevHigh
            val resolvedElevLow = minAltitude?.takeIf { it > 0 } ?: elevLow
            return Training(
                id = id,
                name = name,
                distance = resolvedDistance,
                movingTime = resolvedMovingTime,
                elapsedTime = resolvedElapsedTime,
                totalElevationGain = resolvedElevationGain,
                type = type,
                sportType = sportType ?: type,
                startDate = startDate,
                startDateLocal = localDateStr,
                timezone = timezone,
                averageSpeed = resolvedAverageSpeed,
                maxSpeed = maxSpeed ?: 0.0,
                averageCadence = averageCadence ?: 0.0,
                averageWatts = averageWatts ?: 0.0,
                maxWatts = maxWatts,
                weightedAverageWatts = weightedAverageWatts ?: 0,
                kilojoules = kilojoules ?: 0.0,
                deviceWatts = deviceWatts ?: false,
                averageHeartrate = resolvedAverageHr,
                maxHeartrate = resolvedMaxHr ?: 0.0,
                elevHigh = resolvedElevHigh ?: 0.0,
                elevLow = resolvedElevLow ?: 0.0,
                map = if (polyline != null) Route(summaryPolyline = polyline) else Route(),
                uploadId = 0L,
                sufferScore = null,
                source = source,
                middleware = "INTERVALS",
                middlewareId = id,
                sourceId = externalId,
                deviceName = deviceName?.trim()?.takeIf { it.isNotEmpty() },
                calories = calories,
                hrZoneTimes = icuHrZoneTimes,
                hrZones = icuHrZones
            )
        }
    }

    private data class AthleteProfileResponseDto(
        val athlete: AthleteDto = AthleteDto()
    )

    private data class AthleteDto(
        val id: String = "",
        val name: String? = null,
        val email: String? = null,
        val profileMedium: String? = null
    ) {
        fun toAthlete(): Athlete {
            val numericId = id.removePrefix("i").toLongOrNull() ?: 0L
            return Athlete(
                id = numericId,
                username = name ?: id,
                profileMedium = profileMedium,
                profile = profileMedium
            )
        }
    }

    private data class IntervalsResponseDto(
        val icuIntervals: List<IntervalDto> = emptyList()
    )

    private data class IntervalDto(
        val label: String? = null,
        val distance: Double? = null,
        val movingTime: Int? = null,
        val elapsedTime: Int? = null,
        val totalElevationGain: Double? = null,
        val averageSpeed: Double? = null,
        val averageHeartrate: Double? = null
    ) {
        fun toLap(index: Int): Lap? {
            val dist = distance ?: return null
            return Lap(
                split = index,
                distance = dist,
                movingTime = movingTime ?: elapsedTime ?: 0,
                elapsedTime = elapsedTime ?: movingTime ?: 0,
                elevationDifference = totalElevationGain,
                averageSpeed = averageSpeed,
                averageHeartrate = averageHeartrate
            )
        }
    }

    private data class StreamDto(
        val type: String = "",
        val data: List<Double?> = emptyList()
    )

    private data class TokenRefreshDto(
        val accessToken: String = "",
        val refreshToken: String? = null,
        val expiresAt: Long? = null,
        val expiresIn: Long? = null,
        val athleteId: String? = null
    )

    private data class TokenExchangeDto(
        val accessToken: String = "",
        val scope: String = "",
        // top-level athlete_id returned by intervals.icu (may already include "i" prefix)
        val athleteId: String? = null,
        // fallback: nested athlete object (some OAuth server versions)
        val athlete: AthleteTokenDto? = null
    )

    private data class AthleteTokenDto(
        val id: String = "",
        val name: String = ""
    )
}

private fun String.toZonedIso(): String {
    if (isEmpty()) return "1970-01-01T00:00:00Z"
    if (endsWith("Z") || contains("+") || matches(Regex(".*[+-]\\d{2}:\\d{2}$"))) return this
    return try {
        LocalDateTime.parse(this)
        "${this}Z"
    } catch (_: DateTimeParseException) {
        "1970-01-01T00:00:00Z"
    }
}
