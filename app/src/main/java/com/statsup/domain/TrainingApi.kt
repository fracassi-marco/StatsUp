package com.statsup.domain

interface TrainingApi {
    suspend fun download(token: String, latest: Training?): List<Training>
    suspend fun athlete(token: String): Athlete
}
