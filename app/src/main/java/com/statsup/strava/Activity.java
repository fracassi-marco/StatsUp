package com.statsup.strava;

import com.google.gson.annotations.SerializedName;
import com.statsup.Sports;

import java.util.Date;

public class Activity {
    @SerializedName("id") private long ID;
    @SerializedName("external_id") private String externalID;
    @SerializedName("upload_id") private long uploadID;
    @SerializedName("name") private String name;
    @SerializedName("description") private String description;
    @SerializedName("distance") private Distance distance;
    @SerializedName("moving_time") private Time movingTime;
    @SerializedName("elapsed_time") private Time elapsedTime;
    @SerializedName("total_elevation_gain") private Distance totalElevationGain;
    @SerializedName("elev_high") private Distance elevationHigh;
    @SerializedName("elev_low") private Distance elevationLow;
    @SerializedName("type") private Sports type;
    @SerializedName("start_date") private Date startDate;
    @SerializedName("start_date_local") private Date startDateLocal;
    @SerializedName("timezone") private String timezone;
    @SerializedName("start_latlng") private Coordinates startCoordinates;
    @SerializedName("end_latlng") private Coordinates endCoordinates;
    @SerializedName("achievement_count") private long achievementCount;
    @SerializedName("kudos_count") private long kudosCount;
    @SerializedName("comment_count") private long commentCount;
    @SerializedName("athlete_count") private long athleteCount;
    @SerializedName("photo_count") private long photoCount;
    @SerializedName("total_photo_count") private long totalPhotoCount;
    @SerializedName("trainer") private boolean trainer;
    @SerializedName("commute") private boolean commute;
    @SerializedName("manual") private boolean manual;
    @SerializedName("private") private boolean isPrivate;
    @SerializedName("device_name") private String deviceName;
    @SerializedName("embed_token") private String embedToken;
    @SerializedName("flagged") private boolean flagged;
    @SerializedName("gear_id") private String gearID;
    @SerializedName("average_speed") private Speed averageSpeed;
    @SerializedName("max_speed") private Speed maxSpeed;
    @SerializedName("average_cadence") private float averageCadence;
    @SerializedName("average_watts") private float averageWatts;
    @SerializedName("max_watts") private long maxWatts;
    @SerializedName("weighted_average_watts") private long weightedAverageWatts;
    @SerializedName("kilojoules") private float kilojoules;
    @SerializedName("device_watts") private boolean deviceWatts;
    @SerializedName("has_heartrate") private boolean hasHeartRate;
    @SerializedName("average_heartrate") private float averageHeartRate;
    @SerializedName("max_heartrate") private long maxHeartRate;
    @SerializedName("calories") private float calories;
    @SerializedName("suffer_score") private long sufferScore;
    @SerializedName("has_kudoed") private boolean hasKudoed;

    public long getID() {
        return ID;
    }

    public String getExternalID() {
        return externalID;
    }

    public long getUploadID() {
        return uploadID;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Distance getDistance() {
        return distance;
    }

    public Time getMovingTime() {
        return movingTime;
    }

    public Time getElapsedTime() {
        return elapsedTime;
    }

    public Distance getTotalElevationGain() {
        return totalElevationGain;
    }

    public Distance getElevationHigh() {
        return elevationHigh;
    }

    public Distance getElevationLow() {
        return elevationLow;
    }

    public Sports getType() {
        return type;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getStartDateLocal() {
        return startDateLocal;
    }

    public String getTimezone() {
        return timezone;
    }

    public Coordinates getStartCoordinates() {
        return startCoordinates;
    }

    public Coordinates getEndCoordinates() {
        return endCoordinates;
    }

    public long getAchievementCount() {
        return achievementCount;
    }

    public long getKudosCount() {
        return kudosCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public long getAthleteCount() {
        return athleteCount;
    }

    public long getPhotoCount() {
        return photoCount;
    }

    public long getTotalPhotoCount() {
        return totalPhotoCount;
    }

    public boolean isTrainer() {
        return trainer;
    }

    public boolean isCommute() {
        return commute;
    }

    public boolean isManual() {
        return manual;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getEmbedToken() {
        return embedToken;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public String getGearID() {
        return gearID;
    }

    public Speed getAverageSpeed() {
        return averageSpeed;
    }

    public Speed getMaxSpeed() {
        return maxSpeed;
    }

    public float getAverageCadence() {
        return averageCadence;
    }

    public float getAverageWatts() {
        return averageWatts;
    }

    public long getMaxWatts() {
        return maxWatts;
    }

    public long getWeightedAverageWatts() {
        return weightedAverageWatts;
    }

    public float getKilojoules() {
        return kilojoules;
    }

    public boolean isDeviceWatts() {
        return deviceWatts;
    }

    public boolean isHasHeartRate() {
        return hasHeartRate;
    }

    public float getAverageHeartRate() {
        return averageHeartRate;
    }

    public long getMaxHeartRate() {
        return maxHeartRate;
    }

    public float getCalories() {
        return calories;
    }

    public long getSufferScore() {
        return sufferScore;
    }

    public boolean hasKudoed() {
        return hasKudoed;
    }
}
