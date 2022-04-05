package com.statsup

import android.graphics.Color.parseColor


enum class Stats(val title: String, val unit: String, val color: Int, val provider: (List<Activity>) -> Double) {
    FREQUENCY("Frequenza", "Numero di allenamenti", parseColor("#4CAF50"), { it.size.toDouble() }),
    DURATION("Durata", "Ore di allenamento", parseColor("#2196F3"), { it.sumOf { activity -> activity.durationInHours() }}),
    DISTANCE("Distanza", "Chilometri percorsi", parseColor("#FF9800"), { it.sumOf { activity -> activity.distanceInKilometers() } });

    companion object {
        fun at(position: Int) = when(position) {
            0 -> FREQUENCY
            1 -> DURATION
            else -> DISTANCE
        }
    }
}
