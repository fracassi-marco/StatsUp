package com.statsup

import android.graphics.Color.rgb


enum class Stats(val title: String, val unit: String, val color: Int, val provider: (List<Activity>) -> Double) {
    FREQUENCY("Frequenza", "Numero di allenamenti", rgb(76, 175, 80), { it.size.toDouble() }),
    DURATION("Durata", "Ore di allenamento", rgb(33, 150, 243), { it.sumOf { activity -> activity.durationInHours() }}),
    DISTANCE("Distanza", "Chilometri percorsi", rgb(244, 67, 54), { it.sumOf { activity -> activity.distanceInKilometers() } });

    companion object {
        fun at(position: Int) = when(position) {
            0 -> FREQUENCY
            1 -> DURATION
            else -> DISTANCE
        }
    }
}
