package com.statsup.domain

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity(
    indices = [
        Index(value = ["startDate"], name = "idx_training_start_date"),
        Index(value = ["sportType"], name = "idx_training_sport_type")
    ]
)
data class Training(
    @PrimaryKey var id: String,
    var resourceState: Int? = null,
    var name: String,
    var distance: Double,
    var movingTime: Int,
    var elapsedTime: Int,
    var totalElevationGain: Double,
    var type: String? = null,
    var sportType: String? = null,
    var workoutType: String? = null,
    var startDate: String,
    var startDateLocal: String? = null,
    var timezone: String? = null,
    var utcOffset: Double? = null,
    var locationCity: String? = null,
    var locationState: String? = null,
    var locationCountry: String? = null,
    var achievementCount: Int? = null,
    var kudosCount: Int? = null,
    var commentCount: Int? = null,
    var athleteCount: Int? = null,
    var photoCount: Int? = null,
    var map: Route? = Route(),
    var trainer: Boolean? = null,
    var commute: Boolean? = null,
    var manual: Boolean? = null,
    var private: Boolean? = null,
    var visibility: String? = null,
    var flagged: Boolean? = null,
    var gearId: String? = null,
    var averageSpeed: Double? = null,
    var maxSpeed: Double,
    var averageCadence: Double,
    var averageWatts: Double,
    var maxWatts: Int? = null,
    var weightedAverageWatts: Int,
    var kilojoules: Double,
    var deviceWatts: Boolean,
    var hasHeartrate: Boolean? = null,
    var averageHeartrate: Double? = null,
    var maxHeartrate: Double,
    var heartrateOptOut: Boolean? = null,
    var displayHideHeartrateOption: Boolean? = null,
    var elevHigh: Double,
    var elevLow: Double,
    var uploadId: Long,
    var uploadIdStr: String? = null,
    var externalId: String? = null,
    var fromAcceptedTag: Boolean? = null,
    var prCount: Int? = null,
    var totalPhotoCount: Int? = null,
    var hasKudoed: Boolean? = null,
    var sufferScore: Double?,
    var lapsJson: String? = null,
    var centerLat: Double? = null,
    var centerLng: Double? = null,
    var source: String? = null,
    var middleware: String? = null,
    var middlewareId: String? = null,
    var sourceId: String? = null,
    var deviceName: String? = null,
    var calories: Int? = null,
    var hrZoneTimes: List<Int>? = null,
    var hrZones: List<Int>? = null,
    var elevationPointsJson: String? = null
) {
    val date: ZonedDateTime by lazy { ZonedDateTime.parse(startDate) }

    fun distanceInKilometers() = distance / 1000.0

    fun durationInHours() = elapsedTime / 3600.0

    fun elevationPerKm(): Double {
        val distKm = distanceInKilometers()
        return if (distKm > 0) totalElevationGain / distKm else 0.0
    }

    fun vam(): Double {
        val hours = movingTime / 3600.0
        return if (hours > 0) totalElevationGain / hours else 0.0
    }


    fun averagePace(): Double {
        val distKm = distanceInKilometers()
        return if (distKm > 0) movingTime / distKm / 60.0 else 0.0 // min/km
    }

    fun averageSpeedKmh(): Double {
        return averageSpeed?.times(3.6) ?: 0.0 // converti da m/s a km/h
    }

    val trip: Trip? by lazy {
        if(map != null && !map!!.summaryPolyline.isNullOrBlank()) Trip(map!!.summaryPolyline!!) else null
    }
}


