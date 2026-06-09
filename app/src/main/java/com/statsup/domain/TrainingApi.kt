package com.statsup.domain

data class OAuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val athleteId: String? = null
)

interface TrainingApi {
    suspend fun download(token: String, latest: Training?): List<Training>
    suspend fun athlete(token: String): Athlete
    suspend fun laps(token: String, activityId: String): List<Lap>
    suspend fun refreshToken(refreshToken: String): OAuthToken
    suspend fun exchangeCode(code: String): OAuthToken
}
