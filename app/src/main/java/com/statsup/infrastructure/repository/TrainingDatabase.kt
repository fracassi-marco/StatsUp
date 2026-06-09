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
    version = 11,
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

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // --- training: change id from INTEGER to TEXT (prefix "i" to existing numeric ids) ---
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `training_new` (
                        `id` TEXT NOT NULL,
                        `resourceState` INTEGER,
                        `name` TEXT NOT NULL,
                        `distance` REAL NOT NULL,
                        `movingTime` INTEGER NOT NULL,
                        `elapsedTime` INTEGER NOT NULL,
                        `totalElevationGain` REAL NOT NULL,
                        `type` TEXT,
                        `sportType` TEXT,
                        `workoutType` TEXT,
                        `startDate` TEXT NOT NULL,
                        `startDateLocal` TEXT,
                        `timezone` TEXT,
                        `utcOffset` REAL,
                        `locationCity` TEXT,
                        `locationState` TEXT,
                        `locationCountry` TEXT,
                        `achievementCount` INTEGER,
                        `kudosCount` INTEGER,
                        `commentCount` INTEGER,
                        `athleteCount` INTEGER,
                        `photoCount` INTEGER,
                        `map` TEXT,
                        `trainer` INTEGER,
                        `commute` INTEGER,
                        `manual` INTEGER,
                        `private` INTEGER,
                        `visibility` TEXT,
                        `flagged` INTEGER,
                        `gearId` TEXT,
                        `averageSpeed` REAL,
                        `maxSpeed` REAL NOT NULL,
                        `averageCadence` REAL NOT NULL,
                        `averageWatts` REAL NOT NULL,
                        `maxWatts` INTEGER,
                        `weightedAverageWatts` INTEGER NOT NULL,
                        `kilojoules` REAL NOT NULL,
                        `deviceWatts` INTEGER NOT NULL,
                        `hasHeartrate` INTEGER,
                        `averageHeartrate` REAL,
                        `maxHeartrate` REAL NOT NULL,
                        `heartrateOptOut` INTEGER,
                        `displayHideHeartrateOption` INTEGER,
                        `elevHigh` REAL NOT NULL,
                        `elevLow` REAL NOT NULL,
                        `uploadId` INTEGER NOT NULL,
                        `uploadIdStr` TEXT,
                        `externalId` TEXT,
                        `fromAcceptedTag` INTEGER,
                        `prCount` INTEGER,
                        `totalPhotoCount` INTEGER,
                        `hasKudoed` INTEGER,
                        `sufferScore` REAL,
                        `lapsJson` TEXT,
                        `centerLat` REAL,
                        `centerLng` REAL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO `training_new`
                    SELECT 'i' || CAST(`id` AS TEXT), `resourceState`, `name`, `distance`, `movingTime`,
                        `elapsedTime`, `totalElevationGain`, `type`, `sportType`, `workoutType`,
                        `startDate`, `startDateLocal`, `timezone`, `utcOffset`, `locationCity`,
                        `locationState`, `locationCountry`, `achievementCount`, `kudosCount`,
                        `commentCount`, `athleteCount`, `photoCount`, `map`, `trainer`, `commute`,
                        `manual`, `private`, `visibility`, `flagged`, `gearId`, `averageSpeed`,
                        `maxSpeed`, `averageCadence`, `averageWatts`, `maxWatts`,
                        `weightedAverageWatts`, `kilojoules`, `deviceWatts`, `hasHeartrate`,
                        `averageHeartrate`, `maxHeartrate`, `heartrateOptOut`,
                        `displayHideHeartrateOption`, `elevHigh`, `elevLow`, `uploadId`,
                        `uploadIdStr`, `externalId`, `fromAcceptedTag`, `prCount`,
                        `totalPhotoCount`, `hasKudoed`, `sufferScore`, `lapsJson`,
                        `centerLat`, `centerLng`
                    FROM `training`
                """.trimIndent())
                db.execSQL("DROP TABLE `training`")
                db.execSQL("ALTER TABLE `training_new` RENAME TO `training`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_training_start_date` ON `training` (`startDate`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_training_sport_type` ON `training` (`sportType`)")

                // --- bookmarked_training: change trainingId from INTEGER to TEXT ---
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `bookmarked_training_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `trainingId` TEXT NOT NULL,
                        `note` TEXT NOT NULL,
                        `customTitle` TEXT NOT NULL,
                        `difficulty` TEXT NOT NULL,
                        `bookmarkedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO `bookmarked_training_new`
                    SELECT `id`, 'i' || CAST(`trainingId` AS TEXT), `note`, `customTitle`, `difficulty`, `bookmarkedAt`
                    FROM `bookmarked_training`
                """.trimIndent())
                db.execSQL("DROP TABLE `bookmarked_training`")
                db.execSQL("ALTER TABLE `bookmarked_training_new` RENAME TO `bookmarked_training`")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_bookmarked_training_trainingId` ON `bookmarked_training` (`trainingId`)")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE training ADD COLUMN source TEXT")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE training ADD COLUMN middleware TEXT")
                db.execSQL("ALTER TABLE training ADD COLUMN middlewareId TEXT")
                db.execSQL("ALTER TABLE training ADD COLUMN sourceId TEXT")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE training ADD COLUMN deviceName TEXT")
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
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                    .build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}