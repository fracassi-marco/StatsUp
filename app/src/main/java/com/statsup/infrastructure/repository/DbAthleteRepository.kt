package com.statsup.infrastructure.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.statsup.domain.Athlete
import com.statsup.domain.repository.AthleteRepository

@Dao
interface DbAthleteRepository : AthleteRepository {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun update(athlete: Athlete)

    @Query("SELECT * FROM athlete LIMIT 1")
    override fun load(): Athlete?
}