package com.statsup.ui

import android.content.Context
import com.statsup.R
import com.statsup.domain.BadgeStringSet

fun buildBadgeStringMap(
    context: Context,
    monthlyDistanceGoalKm: Int,
    monthlyTrainingGoal: Int
): Map<String, BadgeStringSet> {
    val km = context.getString(R.string.badge_unit_km)
    val m = context.getString(R.string.badge_unit_m)
    val activities = context.getString(R.string.badge_unit_activities)
    val consecutiveDays = context.getString(R.string.badge_unit_consecutive_days)
    val activeMonths = context.getString(R.string.badge_unit_active_months)
    val kmBest = context.getString(R.string.badge_unit_km_best_activity)
    val mBest = context.getString(R.string.badge_unit_m_best_activity)
    val mMaxAlt = context.getString(R.string.badge_unit_m_max_altitude)

    return mapOf(
        "monthly_first" to BadgeStringSet(
            context.getString(R.string.badge_monthly_first_name),
            context.getString(R.string.badge_monthly_first_desc),
            activities
        ),
        "monthly_bronze" to BadgeStringSet(
            context.getString(R.string.badge_monthly_bronze_name),
            context.getString(R.string.badge_monthly_bronze_desc),
            km
        ),
        "monthly_silver" to BadgeStringSet(
            context.getString(R.string.badge_monthly_silver_name),
            context.getString(R.string.badge_monthly_silver_desc),
            km
        ),
        "monthly_gold" to BadgeStringSet(
            context.getString(R.string.badge_monthly_gold_name),
            context.getString(R.string.badge_monthly_gold_desc),
            km
        ),
        "monthly_diamond" to BadgeStringSet(
            context.getString(R.string.badge_monthly_diamond_name),
            context.getString(R.string.badge_monthly_diamond_desc),
            km
        ),
        "monthly_goal_dist" to BadgeStringSet(
            context.getString(R.string.badge_monthly_goal_dist_name, monthlyDistanceGoalKm),
            context.getString(R.string.badge_monthly_goal_dist_desc, monthlyDistanceGoalKm),
            km
        ),
        "monthly_goal_freq" to BadgeStringSet(
            context.getString(R.string.badge_monthly_goal_freq_name, monthlyTrainingGoal),
            context.getString(R.string.badge_monthly_goal_freq_desc, monthlyTrainingGoal),
            activities
        ),
        "monthly_streak_week" to BadgeStringSet(
            context.getString(R.string.badge_monthly_streak_week_name),
            context.getString(R.string.badge_monthly_streak_week_desc),
            consecutiveDays
        ),
        "monthly_elevation_2k" to BadgeStringSet(
            context.getString(R.string.badge_monthly_elevation_2k_name),
            context.getString(R.string.badge_monthly_elevation_2k_desc),
            m
        ),
        "monthly_elevation_5k" to BadgeStringSet(
            context.getString(R.string.badge_monthly_elevation_5k_name),
            context.getString(R.string.badge_monthly_elevation_5k_desc),
            m
        ),
        "yearly_500km" to BadgeStringSet(
            context.getString(R.string.badge_yearly_500km_name),
            context.getString(R.string.badge_yearly_500km_desc),
            km
        ),
        "yearly_1000km" to BadgeStringSet(
            context.getString(R.string.badge_yearly_1000km_name),
            context.getString(R.string.badge_yearly_1000km_desc),
            km
        ),
        "yearly_2000km" to BadgeStringSet(
            context.getString(R.string.badge_yearly_2000km_name),
            context.getString(R.string.badge_yearly_2000km_desc),
            km
        ),
        "yearly_50act" to BadgeStringSet(
            context.getString(R.string.badge_yearly_50act_name),
            context.getString(R.string.badge_yearly_50act_desc),
            activities
        ),
        "yearly_100act" to BadgeStringSet(
            context.getString(R.string.badge_yearly_100act_name),
            context.getString(R.string.badge_yearly_100act_desc),
            activities
        ),
        "yearly_streak30" to BadgeStringSet(
            context.getString(R.string.badge_yearly_streak30_name),
            context.getString(R.string.badge_yearly_streak30_desc),
            consecutiveDays
        ),
        "yearly_all_months" to BadgeStringSet(
            context.getString(R.string.badge_yearly_all_months_name),
            context.getString(R.string.badge_yearly_all_months_desc),
            activeMonths
        ),
        "yearly_everest" to BadgeStringSet(
            context.getString(R.string.badge_yearly_everest_name),
            context.getString(R.string.badge_yearly_everest_desc),
            m
        ),
        "yearly_elevation_20k" to BadgeStringSet(
            context.getString(R.string.badge_yearly_elevation_20k_name),
            context.getString(R.string.badge_yearly_elevation_20k_desc),
            m
        ),
        "alltime_first" to BadgeStringSet(
            context.getString(R.string.badge_alltime_first_name),
            context.getString(R.string.badge_alltime_first_desc)
        ),
        "alltime_halfmarathon" to BadgeStringSet(
            context.getString(R.string.badge_alltime_halfmarathon_name),
            context.getString(R.string.badge_alltime_halfmarathon_desc),
            kmBest
        ),
        "alltime_marathon" to BadgeStringSet(
            context.getString(R.string.badge_alltime_marathon_name),
            context.getString(R.string.badge_alltime_marathon_desc),
            kmBest
        ),
        "alltime_elevation1k" to BadgeStringSet(
            context.getString(R.string.badge_alltime_elevation1k_name),
            context.getString(R.string.badge_alltime_elevation1k_desc),
            mBest
        ),
        "alltime_elevation2k" to BadgeStringSet(
            context.getString(R.string.badge_alltime_elevation2k_name),
            context.getString(R.string.badge_alltime_elevation2k_desc),
            mBest
        ),
        "alltime_altitude2k" to BadgeStringSet(
            context.getString(R.string.badge_alltime_altitude2k_name),
            context.getString(R.string.badge_alltime_altitude2k_desc),
            mMaxAlt
        ),
        "alltime_altitude3k" to BadgeStringSet(
            context.getString(R.string.badge_alltime_altitude3k_name),
            context.getString(R.string.badge_alltime_altitude3k_desc),
            mMaxAlt
        ),
        "alltime_altitude4k" to BadgeStringSet(
            context.getString(R.string.badge_alltime_altitude4k_name),
            context.getString(R.string.badge_alltime_altitude4k_desc),
            mMaxAlt
        ),
        "alltime_100act" to BadgeStringSet(
            context.getString(R.string.badge_alltime_100act_name),
            context.getString(R.string.badge_alltime_100act_desc),
            activities
        ),
        "alltime_500act" to BadgeStringSet(
            context.getString(R.string.badge_alltime_500act_name),
            context.getString(R.string.badge_alltime_500act_desc),
            activities
        ),
        "alltime_best_streak" to BadgeStringSet(
            context.getString(R.string.badge_alltime_best_streak_name),
            context.getString(R.string.badge_alltime_best_streak_desc),
            consecutiveDays
        )
    )
}

fun buildPersonalRecordLabels(context: Context) = com.statsup.domain.PersonalRecordLabels(
    maxDistance = context.getString(R.string.record_max_distance),
    maxElevationGain = context.getString(R.string.record_max_elevation_gain),
    maxAltitude = context.getString(R.string.record_max_altitude),
    longestActivity = context.getString(R.string.record_longest_activity),
    topSpeed = context.getString(R.string.record_top_speed),
    maxHeartRate = context.getString(R.string.record_max_heart_rate),
    bestPace = context.getString(R.string.record_best_pace)
)
