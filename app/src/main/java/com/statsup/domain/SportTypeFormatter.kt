package com.statsup.domain

/**
 * Utility per formattare i nomi dei tipi di sport con emoji
 */
object SportTypeFormatter {

    /**
     * Restituisce l'emoji appropriata per il tipo di sport
     */
    fun getEmojiForSportType(sportType: String?): String {
        return when (sportType?.lowercase()) {
            "run" -> "🏃"
            "ride" -> "🚴"
            "swim" -> "🏊"
            "walk" -> "🚶"
            "hike" -> "🥾"
            "virtualride" -> "🚴"
            "virtualrun" -> "🏃"
            "workout" -> "💪"
            "yoga" -> "🧘"
            "ski" -> "⛷️"
            "snowboard" -> "🏂"
            "alpineski" -> "⛷️"
            "nordicski" -> "⛷️"
            "backcountryski" -> "⛷️"
            "iceskate" -> "⛸️"
            "inlineskate" -> "🛼"
            "soccer" -> "⚽"
            "tennis" -> "🎾"
            "golf" -> "⛳"
            "rowing" -> "🚣"
            "kayaking" -> "🛶"
            "canoeing" -> "🛶"
            "mountainbikingride" -> "🚵"
            "elliptical" -> "🏋️"
            "stairstepper" -> "🪜"
            "weighttraining" -> "🏋️"
            "rockclimbing" -> "🧗"
            else -> "🏃" // Default emoji
        }
    }

    /**
     * Restituisce il nome formattato del tipo di sport
     */
    fun getNameForSportType(sportType: String?): String {
        return when (sportType?.lowercase()) {
            "run" -> "Running"
            "ride" -> "Cycling"
            "swim" -> "Swimming"
            "walk" -> "Walking"
            "hike" -> "Hiking"
            "virtualride" -> "Virtual Ride"
            "virtualrun" -> "Virtual Run"
            "workout" -> "Workout"
            "yoga" -> "Yoga"
            "ski" -> "Skiing"
            "snowboard" -> "Snowboard"
            "alpineski" -> "Alpine Ski"
            "nordicski" -> "Nordic Ski"
            "backcountryski" -> "Backcountry Ski"
            "iceskate" -> "Ice Skate"
            "inlineskate" -> "Inline Skate"
            "soccer" -> "Soccer"
            "tennis" -> "Tennis"
            "golf" -> "Golf"
            "rowing" -> "Rowing"
            "kayaking" -> "Kayaking"
            "canoeing" -> "Canoeing"
            "mountainbikingride" -> "MTB"
            "elliptical" -> "Elliptical"
            "stairstepper" -> "Stair Stepper"
            "weighttraining" -> "Weight Training"
            "rockclimbing" -> "Rock Climbing"
            else -> sportType?.replaceFirstChar { it.uppercase() } ?: "Activity"
        }
    }

    /**
     * Restituisce il nome formattato con emoji
     */
    fun getFormattedNameWithEmoji(sportType: String?): String {
        return "${getEmojiForSportType(sportType)} ${getNameForSportType(sportType)}"
    }
}

