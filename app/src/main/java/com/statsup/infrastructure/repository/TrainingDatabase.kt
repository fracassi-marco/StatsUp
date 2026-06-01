package com.statsup.infrastructure.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.statsup.domain.Athlete
import com.statsup.domain.BookmarkedTraining
import com.statsup.domain.Training
import com.statsup.domain.WeightEntry

@Database(
    entities = [Training::class, Athlete::class, BookmarkedTraining::class, WeightEntry::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TrainingDatabase: RoomDatabase() {
    abstract val trainingRepository: DbTrainingRepository
    abstract val athleteRepository: DbAthleteRepository
    abstract val bookmarkedTrainingRepository: DbBookmarkedTrainingRepository
    abstract val weightRepository: WeightEntryDao

    companion object {
        private const val DATABASE_NAME = "training_db"

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE training ADD COLUMN lapsJson TEXT")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE training ADD COLUMN centerLat REAL")
                db.execSQL("ALTER TABLE training ADD COLUMN centerLng REAL")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS weight_entry (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "date INTEGER NOT NULL, " +
                    "weightKg REAL NOT NULL)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_weight_entry_date ON weight_entry (date)"
                )
            }
        }

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
                    )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}