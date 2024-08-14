package com.statsup.domain.repository

import com.statsup.domain.Athlete

interface AthleteRepository {
    suspend fun update(athlete: Athlete)
    fun load(): Athlete?
}