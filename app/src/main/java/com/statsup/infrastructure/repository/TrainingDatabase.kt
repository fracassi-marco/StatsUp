package com.statsup.infrastructure.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.statsup.domain.Athlete
import com.statsup.domain.Training

@Database(
    entities = [Training::class, Athlete::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class TrainingDatabase: RoomDatabase() {
    abstract val trainingRepository: DbTrainingRepository
    abstract val athleteRepository: DbAthleteRepository

    companion object {
        private const val DATABASE_NAME = "training_db"

        @Volatile
        private var INSTANCE: TrainingDatabase? = null

        fun getInstance(context: Context): TrainingDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TrainingDatabase::class.java,
                        DATABASE_NAME
                    ).build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}