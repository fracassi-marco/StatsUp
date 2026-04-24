package com.statsup.domain

data class StravaToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long
)

interface TrainingApi {
    suspend fun download(token: String, latest: Training?): List<Training>
    suspend fun athlete(token: String): Athlete
    suspend fun laps(token: String, activityId: Long): List<Lap>
    suspend fun refreshToken(refreshToken: String): StravaToken
}
